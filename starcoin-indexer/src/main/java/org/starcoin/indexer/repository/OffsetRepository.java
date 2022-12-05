package org.starcoin.indexer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bean.OffsetEntity;

public interface OffsetRepository extends JpaRepository<OffsetEntity, String> {
    OffsetEntity getByOffsetId(String offsetId);
}
