package com.nexora.search.repository.elastic;

import com.nexora.search.document.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {
}
