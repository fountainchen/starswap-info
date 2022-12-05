package org.starcoin.indexer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bean.OffsetEntity;
import org.starcoin.indexer.repository.OffsetRepository;

@Service
@Slf4j
public class OffsetService {
    @Autowired
    private OffsetRepository offsetRepository;

    public long getOffset(String network, String offsetId) {
        OffsetEntity offset;
        try {
            offset = offsetRepository.getByOffsetId(offsetId);
            if (offset != null) {
                return offset.getOffset();
            } else {
                log.warn("offset not exist, and create it: {}", offsetId);
                //init offset
                offset = new OffsetEntity(offsetId, System.currentTimeMillis(), 0);
                offsetRepository.save(offset);
                return 0;
            }
        } catch (Exception e) {
            log.error("get offset err:", e);
            return -1;
        }
    }

    public void updateOffset(String network, String offsetId, long offset) {
        OffsetEntity newOffset = new OffsetEntity(offsetId, offset);
        offsetRepository.save(newOffset);
        log.info("save offset ok: {}, {}", offsetId, offset);
    }
}
