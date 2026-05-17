package com.nexora.search.repository.postgres;

import com.nexora.search.entity.QueryLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryLogRepository extends JpaRepository<QueryLogEntity, Long> {
}
