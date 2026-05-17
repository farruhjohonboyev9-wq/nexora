package com.nexora.search.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "search.index")
public class SearchIndexProperties {

    private String users = "users_search";
    private String posts = "posts_search";
    private String hashtags = "hashtags_search";

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }
   
    public String getPosts() {
        return posts;
    }

    public void setPosts(String posts) {
        this.posts = posts;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }
}
