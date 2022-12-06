package org.starcoin.indexer.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.starcoin.api.StateRPCClient;
import org.starcoin.bean.*;
import org.starcoin.constant.StarcoinNetwork;
import org.starcoin.indexer.service.OffsetService;
import org.starcoin.indexer.service.SwapTxnService;
import org.starcoin.types.*;
import org.starcoin.types.StructTag;
import org.starcoin.types.TransactionPayload;
import org.starcoin.types.TypeTag;
import org.starcoin.utils.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@DisallowConcurrentExecution
@Slf4j
public class SwapTransactionHandle extends QuartzJobBean {

    private static final String SWAP_TXN_OFFSET_KEY ="SWAP_TXN_";
    private static final String SWAP_MODULE ="0x8c109349c6bd91411d6bc962e080c4a3::TokenSwapFarmScript";
    private ObjectMapper objectMapper;
    @Value("${starcoin.network}")
    private String network;
    private StarcoinNetwork localNetwork;
    @Autowired
    private StateRPCClient stateRPCClient;
    @Autowired
    private SwapTxnService swapTxnService;
    @Autowired
    private SwapApiClient swapApiClient;
    @Autowired
    private OffsetService offsetService;
    @Autowired
    private MoveScanClient moveScanClient;
    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StructTag.class, new StructTagDeserializer());
        module.addDeserializer(TypeTag.class, new TypeTagDeserializer());
        module.addDeserializer(ModuleId.class, new ModuleDeserializer());
        module.addDeserializer(ScriptFunction.class, new ScriptFunctionDeserializer());
        module.addDeserializer(TransactionPayload.class, new TransactionPayloadDeserializer());

        module.addSerializer(TransactionPayload.class, new TransactionPayloadSerializer());
        module.addSerializer(TypeTag.class, new TypeTagSerializer());
        module.addSerializer(StructTag.class, new StructTagSerializer());
        module.addSerializer(ScriptFunction.class, new ScriptFunctionSerializer());
        module.addSerializer(ModuleId.class, new ModuleIdSerializer());

        objectMapper.registerModule(module);
        //init network
        if (localNetwork == null) {
            localNetwork = StarcoinNetwork.fromValue(network);
        }
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        if (localNetwork == null) {
            init();
        }
        long offset = offsetService.getOffset(network, SWAP_TXN_OFFSET_KEY + network);
        log.info("handle swap txn: {}", offset);

        try {
            List<TransactionEntity> transactionEntityList = moveScanClient.getSwapTxn(offset + 1, 50);
            if (!transactionEntityList.isEmpty()) {
                List<SwapTransaction> swapTransactionList = new ArrayList<>();
                Map<String, BigDecimal> tokenPriceMap = new HashMap<>();
                long globalIndex = 0;
                for (TransactionEntity entity: transactionEntityList) {
                    if(entity.getGlobalIndex() > globalIndex) {
                        globalIndex = entity.getGlobalIndex();
                    }
                    JSONObject jb = JSONObject.parseObject(entity.getPayload());
                    JSONObject scripts = jb.getJSONObject("ScriptFunction");
                    String module = String.valueOf(scripts.get("module"));
                    if(SWAP_MODULE.equals(module) ) {
                        SwapTransaction swapTransaction = new SwapTransaction();
                        List<String> tokenList = new ArrayList<>();
                        UserTransactionEntity userTransactionEntity = moveScanClient.getUserTxn(entity.getTxnHash());
                        if(userTransactionEntity != null) {
                            swapTransaction.setAccount(userTransactionEntity.getSender());
                        }
                        swapTransaction.setTransactionHash(entity.getTxnHash());
                        swapTransaction.setTimestamp(entity.getCreateAt());
                        JSONArray typeArray = scripts.getJSONArray("ty_args");
                        String tokenA =  String.valueOf(typeArray.get(0));
                        String tokenB =  String.valueOf(typeArray.get(1));
                        swapTransaction.setTokenA(tokenA);
                        swapTransaction.setTokenB(tokenB);
                        SwapType swapType = SwapType.fromValue(String.valueOf(scripts.get("function")));
                        JSONArray argsArray = scripts.getJSONArray("args");
                        BigInteger argFirst = argsArray.getBigInteger(0);
                        BigInteger argSecond = new BigInteger("0");
                        if(swapType == SwapType.RemoveLiquidity) {
                            argFirst = argsArray.getBigInteger(1);
                            argSecond = argsArray.getBigInteger(2);
                        }else if(swapType == SwapType.Stake || swapType == SwapType.Harvest) {
                            //only one arg
                        }else {
                            argSecond = argsArray.getBigInteger(1);
                        }
                        swapTransaction.setSwapType(swapType);
                        tokenList.add(tokenA);
                        tokenList.add(tokenB);
                        swapTransaction.setAmountA(ServiceUtils.divideScalingFactor(stateRPCClient, swapTransaction.getTokenA(), new BigDecimal(argFirst)));
                        swapTransaction.setAmountB(ServiceUtils.divideScalingFactor(stateRPCClient, swapTransaction.getTokenB(), new BigDecimal(argSecond)));
                        boolean isSwap = SwapType.isSwap(swapTransaction.getSwapType());
                        BigDecimal value = getTotalValue(tokenPriceMap, swapTransaction.getTokenA(), swapTransaction.getAmountA(),
                                swapTransaction.getTokenB(), swapTransaction.getAmountB(), isSwap);
                        if (value != null) {
                            swapTransaction.setTotalValue(value);
                        } else {
                            int retry = 3;
                            while (retry > 0) {
                                //get oracle price
                                log.info("token price not cache, load from oracle: {}, {}", swapTransaction.getTokenA(), swapTransaction.getTokenB());
                                long priceTime = swapTransaction.getTimestamp() - 300000 * (6 - retry);
                                List<org.starcoin.bean.OracleTokenPair> oracleTokenPairs =
                                        swapApiClient.getProximatePriceRounds(localNetwork.getValue(), tokenList, String.valueOf(priceTime));
                                if (oracleTokenPairs != null && !oracleTokenPairs.isEmpty()) {
                                    BigDecimal priceA = null;
                                    OracleTokenPair oracleTokenA = oracleTokenPairs.get(0);
                                    if (oracleTokenA != null) {
                                        priceA = new BigDecimal(oracleTokenA.getPrice());
                                        priceA = priceA.movePointLeft(oracleTokenA.getDecimals());
                                        tokenPriceMap.put(swapTransaction.getTokenA(), priceA);
                                        log.info("get oracle price1 ok: {}", oracleTokenA);
                                    }
                                    // get tokenB price
                                    BigDecimal priceB = null;
                                    OracleTokenPair oracleTokenB = oracleTokenPairs.get(1);
                                    if (oracleTokenB != null) {
                                        priceB = new BigDecimal(oracleTokenB.getPrice());
                                        priceB = priceB.movePointLeft(oracleTokenB.getDecimals());
                                        tokenPriceMap.put(swapTransaction.getTokenB(), priceB);
                                        log.info("get oracle price2 ok: {}", oracleTokenB);
                                    }
                                    BigDecimal zero = new BigDecimal(0);
                                    if (isSwap) {
                                        if (priceA != null && priceA.compareTo(zero) == 1) {
                                            swapTransaction.setTotalValue(priceA.multiply(swapTransaction.getAmountA()));
                                            break;
                                        }
                                        if (priceB != null && priceB.compareTo(zero) == 1) {
                                            swapTransaction.setTotalValue(priceB.multiply(swapTransaction.getAmountB()));
                                            break;
                                        }
                                        log.warn("get oracle price null: {}, {}, {}", swapTransaction.getTokenA(), swapTransaction.getTokenB(), priceTime);
                                        retry--;
                                    } else {
                                        // add or remove
                                        boolean isExistB = priceB != null && priceB.compareTo(zero) == 1;
                                        if (priceA != null && priceA.compareTo(zero) == 1) {
                                            BigDecimal valueA = priceA.multiply(swapTransaction.getAmountA());
                                            if (isExistB) {
                                                swapTransaction.setTotalValue(priceB.multiply(swapTransaction.getAmountB()).add(valueA));
                                            } else {
                                                swapTransaction.setTotalValue(valueA.multiply(new BigDecimal(2)));
                                            }
                                            break;
                                        } else {
                                            if (isExistB) {
                                                swapTransaction.setTotalValue(priceB.multiply(swapTransaction.getAmountB()).multiply(new BigDecimal(2)));
                                                break;
                                            } else {
                                                log.warn("get oracle price null: {}, {}, {}", swapTransaction.getTokenA(), swapTransaction.getTokenB(), priceTime);
                                                retry--;
                                            }
                                        }
                                    }
                                } else {
                                    log.warn("getProximatePriceRounds null: {}, {}, {}", swapTransaction.getTokenA(), swapTransaction.getTokenB(), priceTime);
                                    retry--;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        swapTransactionList.add(swapTransaction);
                    }
                }

                //save swap transaction
                try {
                    if(!swapTransactionList.isEmpty()) {
                        swapTxnService.saveList(swapTransactionList);
                        log.info("save swap txn ok: {}", swapTransactionList.size());
                    }
                    if(globalIndex > 0) {
                        //update offset
                        offsetService.updateOffset(network, SWAP_TXN_OFFSET_KEY+ network, globalIndex);
                        log.info("update payload ok: {}", globalIndex);
                    }
                } catch (Exception e) {
                    log.error("save swap err:", e);
                }
            } else {
                log.warn("get txn list null");
            }

        } catch (IOException e) {
            log.warn("handle swap transaction error:", e);
        }
    }

    private BigDecimal getTotalValue(Map<String, BigDecimal> priceMap, String tokenA, BigDecimal amountA, String tokenB,
                                     BigDecimal amountB, boolean isSwap) {
        BigDecimal priceA = priceMap.get(tokenA);
        BigDecimal priceB = priceMap.get(tokenB);
        if (isSwap) {
            if (priceA != null) {
                return priceA.multiply(amountA);
            } else if (priceB != null) {
                return priceB.multiply(amountB);
            }
        } else {
            BigDecimal total;
            BigDecimal two = new BigDecimal(2);
            if (priceA != null) {
                total = priceA.multiply(amountA);
                if (priceB != null) {
                    return total.add(priceB.multiply(amountB));
                } else {
                    return total.multiply(two);
                }
            } else {
                if (priceB != null) {
                    return two.multiply(priceB.multiply(amountB));
                }
            }
        }
        return null;
    }
}
