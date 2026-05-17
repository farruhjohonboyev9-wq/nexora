package com.nexora.search.repository.postgres;

import com.nexora.search.entity.HashtagUsageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface HashtagUsageRepository extends JpaRepository<HashtagUsageEntity, Long> {

    @Query("""
            select h.hashtag, sum(h.usageCount) as rank
            from HashtagUsageEntity h
            where h.activityAt >= :from
            group by h.hashtag
            order by rank desc
            """)
    List<Object[]> findTrending(Instant from, Pageable pageable);
}
