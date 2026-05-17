package com.nexora.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.nexora.search.document.HashtagDocument;
import com.nexora.search.document.PostDocument;
import com.nexora.search.document.UserDocument;
import com.nexora.search.dto.HashtagSearchItem;
import com.nexora.search.dto.PagedResponse;
import com.nexora.search.dto.PostSearchItem;
import com.nexora.search.dto.SuggestionItem;
import com.nexora.search.dto.UserSearchItem;
import com.nexora.search.repository.postgres.HashtagUsageRepository;
import com.nexora.search.service.QueryAnalyticsService;
import com.nexora.search.service.SearchService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final HashtagUsageRepository hashtagUsageRepository;
    private final QueryAnalyticsService queryAnalyticsService;
    
    public SearchServiceImpl(
            ElasticsearchOperations elasticsearchOperations,
            HashtagUsageRepository hashtagUsageRepository,
            QueryAnalyticsService queryAnalyticsService
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.hashtagUsageRepository = hashtagUsageRepository;
        this.queryAnalyticsService = queryAnalyticsService;
    }

    @Override    
    @Cacheable(value = "search-users", key = "#q + '|' + #page + '|' + #size", unless = "#q == null || #q.length() < 2")
    public PagedResponse<UserSearchItem> searchUsers(String q, int page, int size) {
        long start = System.currentTimeMillis();
        String query = normalizeQuery(q);

        if (query.length() < 2) {
            return emptyPage(page, size);
        }

        Query esQuery = Query.of(node -> node.bool(bool -> bool
                .should(s -> s.match(m -> m.field("username").query(query).fuzziness("AUTO").boost(3.0f)))
                .should(s -> s.match(m -> m.field("fullName").query(query).fuzziness("AUTO").boost(2.0f)))
                .should(s -> s.prefix(p -> p.field("username").value(query).boost(4.0f)))
                .minimumShouldMatch("1")
        ));

        SearchHits<UserDocument> hits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(esQuery).withPageable(PageRequest.of(page, size)).build(),
                UserDocument.class
        );

        List<UserSearchItem> items = hits.getSearchHits().stream()
                .map(hit -> toUserItem(hit.getContent(), hit.getScore()))
                .toList();

        queryAnalyticsService.trackPopularQuery("users", query);
        queryAnalyticsService.logQuery("users", query, items.size(), System.currentTimeMillis() - start, false);

        return toPagedResponse(page, size, hits, items);
    }
  
    @Override
    @Cacheable(value = "search-posts", key = "#q + '|' + #from + '|' + #to + '|' + #minPopularity + '|' + #page + '|' + #size", unless = "#q == null || #q.length() < 2")
    public PagedResponse<PostSearchItem> searchPosts(String q, Instant from, Instant to, Integer minPopularity, int page, int size) {
        long start = System.currentTimeMillis();
        String query = normalizeQuery(q);

        if (query.length() < 2) {
            return emptyPage(page, size);
        }

        Query esQuery = Query.of(node -> node.bool(bool -> {
            bool.should(s -> s.match(m -> m.field("content").query(query).fuzziness("AUTO").boost(3.0f)));
            bool.should(s -> s.term(t -> t.field("hashtags").value(query.replace("#", ""))));
            bool.minimumShouldMatch("1");

            if (from != null) {
                bool.filter(f -> f.range(r -> r.field("createdAt").gte(JsonData.of(from.toString()))));
            }
            if (to != null) {
                bool.filter(f -> f.range(r -> r.field("createdAt").lte(JsonData.of(to.toString()))));
            }
            if (minPopularity != null) {
                bool.filter(f -> f.range(r -> r.field("popularity").gte(JsonData.of(minPopularity))));
            }
            return bool;
        }));

        SearchHits<PostDocument> hits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(esQuery).withPageable(PageRequest.of(page, size)).build(),
                PostDocument.class
        );

        List<PostSearchItem> items = hits.getSearchHits().stream()
                .map(hit -> toPostItem(hit.getContent(), hit.getScore()))
                .toList();

        queryAnalyticsService.trackPopularQuery("posts", query);
        queryAnalyticsService.logQuery("posts", query, items.size(), System.currentTimeMillis() - start, false);

        return toPagedResponse(page, size, hits, items);
    }

    @Override
    @Cacheable(value = "search-hashtags", key = "#q + '|' + #page + '|' + #size")
    public PagedResponse<HashtagSearchItem> searchHashtags(String q, int page, int size) {
        long start = System.currentTimeMillis();
        String query = normalizeQuery(q);

        if (query.isBlank()) {
            List<Object[]> trendingRows = hashtagUsageRepository.findTrending(
                    Instant.now().minus(24, ChronoUnit.HOURS),
                    PageRequest.of(page, size)
            );

            List<HashtagSearchItem> trendingItems = trendingRows.stream()
                    .map(row -> {
                        String tag = String.valueOf(row[0]);
                        long usage = ((Number) row[1]).longValue();
                        return new HashtagSearchItem("trend-" + tag, tag, usage, usage);
                    })
                    .toList();

            queryAnalyticsService.logQuery("hashtags", "<trending>", trendingItems.size(), System.currentTimeMillis() - start, false);
            return new PagedResponse<>(page, size, trendingItems.size(), 1, trendingItems);
        }

        Query esQuery = Query.of(node -> node.bool(bool -> bool
                .should(s -> s.prefix(p -> p.field("tag").value(query).boost(4.0f)))
                .should(s -> s.match(m -> m.field("tag").query(query).fuzziness("AUTO").boost(2.0f)))
                .minimumShouldMatch("1")
        ));

        SearchHits<HashtagDocument> hits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(esQuery).withPageable(PageRequest.of(page, size)).build(),
                HashtagDocument.class
        );

        List<HashtagSearchItem> items = hits.getSearchHits().stream()
                .map(hit -> toHashtagItem(hit.getContent(), hit.getScore()))
                .toList();

        queryAnalyticsService.trackPopularQuery("hashtags", query);
        queryAnalyticsService.logQuery("hashtags", query, items.size(), System.currentTimeMillis() - start, false);

        return toPagedResponse(page, size, hits, items);
    }

    @Override
    @Cacheable(value = "search-suggest", key = "#q + '|' + #limit", unless = "#q == null || #q.length() < 2")
    public List<SuggestionItem> suggest(String q, int limit) {
        long start = System.currentTimeMillis();
        String query = normalizeQuery(q);

        if (query.length() < 2) {
            return List.of();
        }

        int perType = Math.max(1, limit / 3);
        List<SuggestionItem> suggestions = new ArrayList<>();

        Query userSuggestQuery = Query.of(node -> node.prefix(p -> p.field("username").value(query)));
        SearchHits<UserDocument> users = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(userSuggestQuery).withPageable(PageRequest.of(0, perType)).build(),
                UserDocument.class
        );
        users.getSearchHits().forEach(hit -> suggestions.add(new SuggestionItem("user", hit.getContent().getUsername(), hit.getScore())));

        Query hashtagSuggestQuery = Query.of(node -> node.prefix(p -> p.field("tag").value(query.replace("#", ""))));
        SearchHits<HashtagDocument> hashtags = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(hashtagSuggestQuery).withPageable(PageRequest.of(0, perType)).build(),
                HashtagDocument.class
        );
        hashtags.getSearchHits().forEach(hit -> suggestions.add(new SuggestionItem("hashtag", "#" + hit.getContent().getTag(), hit.getScore())));

        Query postSuggestQuery = Query.of(node -> node.match(m -> m.field("content").query(query).fuzziness("AUTO")));
        SearchHits<PostDocument> posts = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(postSuggestQuery).withPageable(PageRequest.of(0, perType)).build(),
                PostDocument.class
        );
        posts.getSearchHits().forEach(hit -> suggestions.add(new SuggestionItem("post", trimSnippet(hit.getContent().getContent()), hit.getScore())));

        List<SuggestionItem> capped = suggestions.stream().limit(limit).toList();

        queryAnalyticsService.trackPopularQuery("suggest", query);
        queryAnalyticsService.logQuery("suggest", query, capped.size(), System.currentTimeMillis() - start, false);

        return capped;
    }

    private String normalizeQuery(String q) {
        return q == null ? "" : q.trim().toLowerCase();
    }

    private static String trimSnippet(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= 80 ? content : content.substring(0, 80) + "...";
    }

    private UserSearchItem toUserItem(UserDocument doc, float score) {
        return new UserSearchItem(doc.getId(), doc.getUsername(), doc.getFullName(), doc.isVerified(), score);
    }

    private PostSearchItem toPostItem(PostDocument doc, float score) {
        return new PostSearchItem(
                doc.getId(),
                doc.getAuthorId(),
                doc.getContent(),
                doc.getHashtags(),
                doc.getPopularity(),
                doc.getCreatedAt(),
                score
        );
    }

    private HashtagSearchItem toHashtagItem(HashtagDocument doc, float score) {
        double rank = (doc.getUsageCount() * 0.7D) + (score * 0.3D);
        return new HashtagSearchItem(doc.getId(), doc.getTag(), doc.getUsageCount(), rank);
    }

    private <T> PagedResponse<T> emptyPage(int page, int size) {
        return new PagedResponse<>(page, size, 0, 0, List.of());
    }

    private <T, D> PagedResponse<T> toPagedResponse(int page, int size, SearchHits<D> hits, List<T> items) {
        long total = hits.getTotalHits();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PagedResponse<>(page, size, total, totalPages, items);
    }
}


