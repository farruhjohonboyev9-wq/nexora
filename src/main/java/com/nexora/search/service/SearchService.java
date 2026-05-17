package com.nexora.search.service;

import com.nexora.search.dto.HashtagSearchItem;
import com.nexora.search.dto.PagedResponse;
import com.nexora.search.dto.PostSearchItem;
import com.nexora.search.dto.SuggestionItem;
import com.nexora.search.dto.UserSearchItem;

import java.time.Instant;
import java.util.List;

public interface SearchService {

    PagedResponse<UserSearchItem> searchUsers(String q, int page, int size);

    PagedResponse<PostSearchItem> searchPosts(
            String q,
            Instant from,
            Instant to,
            Integer minPopularity,
            int page,
            int size
    );

    PagedResponse<HashtagSearchItem> searchHashtags(String q, int page, int size);

    List<SuggestionItem> suggest(String q, int limit);
}
