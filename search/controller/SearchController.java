package com.nexora.search.controller;

import com.nexora.search.dto.HashtagSearchItem;
import com.nexora.search.dto.PagedResponse;
import com.nexora.search.dto.PostSearchItem;
import com.nexora.search.dto.SuggestionItem;
import com.nexora.search.dto.UserSearchItem;
import com.nexora.search.service.SearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
   
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
  
@Validated
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/users")
    public PagedResponse<UserSearchItem> searchUsers(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return searchService.searchUsers(q, page, size);
    }

    @GetMapping("/posts")
    public PagedResponse<PostSearchItem> searchPosts(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) @Min(0) Integer minPopularity,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return searchService.searchPosts(q, from, to, minPopularity, page, size);
    }

    @GetMapping("/hashtags")
    public PagedResponse<HashtagSearchItem> searchHashtags(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return searchService.searchHashtags(q, page, size);
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<SuggestionItem>> suggest(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        List<SuggestionItem> suggestions = searchService.suggest(q, limit);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .header("X-Debounce-Friendly", "true")
                .body(suggestions);
    }
};  

