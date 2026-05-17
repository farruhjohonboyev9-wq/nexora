package com.nexora.search.bootstrap;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.nexora.search.properties.SearchIndexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
public class ElasticsearchIndexBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexBootstrap.class);

    private final ElasticsearchClient elasticsearchClient;
    private final SearchIndexProperties indexProperties;

    @Value("classpath:elasticsearch/users-index.json")
    private Resource usersIndexDefinition;

    @Value("classpath:elasticsearch/posts-index.json")
    private Resource postsIndexDefinition;

    @Value("classpath:elasticsearch/hashtags-index.json")
    private Resource hashtagsIndexDefinition;

    public ElasticsearchIndexBootstrap(
            ElasticsearchClient elasticsearchClient,
            SearchIndexProperties indexProperties
    ) {
        this.elasticsearchClient = elasticsearchClient;
        this.indexProperties = indexProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        ensureIndex(indexProperties.getUsers(), usersIndexDefinition);
        ensureIndex(indexProperties.getPosts(), postsIndexDefinition);
        ensureIndex(indexProperties.getHashtags(), hashtagsIndexDefinition);
    }

    private void ensureIndex(String indexName, Resource definition) throws Exception {
        boolean exists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();
        if (exists) {
            return;
        }

        try (Reader reader = new InputStreamReader(definition.getInputStream(), StandardCharsets.UTF_8)) {
            elasticsearchClient.indices().create(c -> c.index(indexName).withJson(reader));
            log.info("Created Elasticsearch index {}", indexName);
        }
    }
}
