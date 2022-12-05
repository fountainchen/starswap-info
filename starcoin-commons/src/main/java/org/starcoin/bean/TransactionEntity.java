package org.starcoin.bean;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TransactionEntity {
    @JSONField(name = "txn_type")
    @ApiModelProperty(value = "交易类型(*)")
    private TransactionType transactionType;
    @JSONField(name = "block_hash")
    @ApiModelProperty(value = "块哈希")
    private String blockHash;
    @JSONField(name = "block_number")
    @ApiModelProperty(value = "块高度")
    private long blockNumber;
    @JSONField(name = "payload")
    @ApiModelProperty(value = "负载")
    private String payload;
    @JSONField(name = "global_index")
    @ApiModelProperty(value = "全局索引号(*)")
    private long globalIndex;
    @JSONField(name = "txn_hash")
    @ApiModelProperty(value = "交易哈希(*)")
    private String txnHash;
    @JSONField(name = "state_root_hash")
    @ApiModelProperty(value = "状态根哈希")
    private String stateRootHash;
    @JSONField(name = "event_root_hash")
    @ApiModelProperty(value = "事件根哈希")
    private String eventRootHash;
    @JSONField(name = "gas_used")
    @ApiModelProperty(value = "GAS消耗量(*)")
    private long gasUsed;
    @JSONField(name = "success")
    @ApiModelProperty(value = "是否成功(*)")
    private boolean success;
    @JSONField(name = "vm_status")
    @ApiModelProperty(value = "虚拟机状态")
    private String vmStatus;
    @JSONField(name = "accumulator_root_hash")
    @ApiModelProperty(value = "累加器根哈希")
    private String accumulatorRootHash;
    @JSONField(name = "create_at")
    @ApiModelProperty(value = "创建时间(*)")
    private long createAt;
}
