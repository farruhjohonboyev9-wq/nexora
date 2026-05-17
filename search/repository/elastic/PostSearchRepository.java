package com.nexora.search.repository.elastic;

import com.nexora.search.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {
}
