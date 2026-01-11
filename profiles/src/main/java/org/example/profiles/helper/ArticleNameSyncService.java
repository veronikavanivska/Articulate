package org.example.profiles.helper;

import org.example.profiles.clients.ArticleClient;
import org.springframework.stereotype.Service;

@Service
public class ArticleNameSyncService {
    private final ArticleClient articleClient;

    public ArticleNameSyncService(ArticleClient articleClient) {
        this.articleClient = articleClient;
    }
    public void syncUpdatedFullName(long userId, String fullName) {
        try {
            articleClient.syncAuthorFullName(userId, fullName);
            System.out.println("[AuthorNameSync] updated userId=" + userId);
        } catch (Exception e) {
            System.err.println("[AuthorNameSync] failed userId=" + userId + ": " + e.getMessage());
        }
    }
}