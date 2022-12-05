package org.starcoin.bean;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserTransactionEntity {
    @JSONField(name = "txn_hash")
    @ApiModelProperty(value = "交易哈希(*)")
    private String txnHash;
    @JSONField(name = "signature")
    @ApiModelProperty(value = "签名")
    private String signature;
    @JSONField(name = "sender")
    @ApiModelProperty(value = "发送者(*)")
    private String sender;
    @JSONField(name = "sequence_number")
    @ApiModelProperty(value = "序列号")
    private long sequenceNumber;
    @JSONField(name = "max_gas_amount")
    @ApiModelProperty(value = "最大GAS消耗量")
    private long maxGasAmount;
    @JSONField(name = "expiration_secs")
    @ApiModelProperty(value = "过期时间")
    private long expirationSecs;
    @JSONField(name = "gas_unit_price")
    @ApiModelProperty(value = "GAS单位价格")
    private long gasUnitPrice;
    @JSONField(name = "chain_time")
    @ApiModelProperty(value = "链上时间")
    private long ChainTime;
    @JSONField(serialize = false)
    private String payloadFunction;
    @JSONField(name = "create_at")
    @ApiModelProperty(value = "创建时间(*)")
    private long createAt;
}