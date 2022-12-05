package org.starcoin.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "handle_offset")
public class OffsetEntity {
    @Id
    @Column(name = "offset_id")
    private String offsetId;
    @Column(name = "ts")
    private long timestamp;
    @Column(name = "offset_value")
    private long offset;

    public OffsetEntity(String offsetId, long timestamp, long offset) {
        this.offsetId = offsetId;
        this.timestamp = timestamp;
        this.offset = offset;
    }

    public OffsetEntity(String offsetId, long offset) {
        this.offsetId = offsetId;
        this.offset = offset;
    }

    public OffsetEntity() {
    }
}
