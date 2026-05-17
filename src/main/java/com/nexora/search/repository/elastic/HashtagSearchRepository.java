package com.nexora.search.repository.elastic;

import com.nexora.search.document.HashtagDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface HashtagSearchRepository extends ElasticsearchRepository<HashtagDocument, String> {
}
