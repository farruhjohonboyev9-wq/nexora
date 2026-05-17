package com.nexora.search;

import com.nexora.search.properties.SearchIndexProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@EnableCaching
@EnableConfigurationProperties(SearchIndexProperties.class)
@SpringBootApplication
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}



