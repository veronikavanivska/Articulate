package org.example.auth.helpers;

import com.example.generated.ApiResponse;
import org.example.auth.clients.ProfileCommandClient;
import org.springframework.stereotype.Service;

@Service
public class ProfileSyncService {
    private final ProfileCommandClient profileCommandClient;

    public ProfileSyncService(ProfileCommandClient profileCommandClient) {
        this.profileCommandClient = profileCommandClient;
    }
    public void syncUserRegistered(long userId) {
        try {
            ApiResponse resp = profileCommandClient.ensureUserProfile(userId);
            logResult("EnsureUserProfile", userId, resp);
        } catch (Exception e) {
            System.err.println("[ProfileSync] failed EnsureUserProfile userId=" + userId + ": " + e.getMessage());
        }
    }

    public void syncRoleAssigned(long userId, String role) {
        try {
            ApiResponse resp;

            if ("ROLE_WORKER".equals(role)) {
                resp = profileCommandClient.ensureWorkerProfile(userId);
                logResult("EnsureWorkerProfile", userId, resp);
                return;
            }

            if ("ROLE_ADMIN".equals(role)) {
                resp = profileCommandClient.ensureAdminProfile(userId);
                logResult("EnsureAdminProfile", userId, resp);
                return;
            }

            // inne role ignorujemy
            System.out.println("[ProfileSync] skip roleAssigned userId=" + userId + " role=" + role);

        } catch (Exception e) {
            System.err.println("[ProfileSync] failed roleAssigned userId=" + userId + " role=" + role + ": " + e.getMessage());
        }
    }

    public void syncRoleRevoked(long userId, String role) {
        try {
            ApiResponse resp;

            if ("ROLE_WORKER".equals(role)) {
                resp = profileCommandClient.deleteWorkerProfile(userId);
                logResult("DeleteWorkerProfile", userId, resp);
                return;
            }

            if ("ROLE_ADMIN".equals(role)) {
                resp = profileCommandClient.deleteAdminProfile(userId);
                logResult("DeleteAdminProfile", userId, resp);
                return;
            }

            System.out.println("[ProfileSync] skip roleRevoked userId=" + userId + " role=" + role);

        } catch (Exception e) {
            System.err.println("[ProfileSync] failed roleRevoked userId=" + userId + " role=" + role + ": " + e.getMessage());
        }
    }

    public void syncUserDeleted(long userId) {
        try {
            ApiResponse resp = profileCommandClient.deleteAllProfiles(userId);
            logResult("DeleteAllProfiles", userId, resp);
        } catch (Exception e) {
            System.err.println("[ProfileSync] failed DeleteAllProfiles userId=" + userId + ": " + e.getMessage());
        }
    }

    private static void logResult(String op, long userId, ApiResponse resp) {
        if (resp == null) {
            System.err.println("[ProfileSync] " + op + " userId=" + userId + " -> null response");
            return;
        }

        // Zakładam, że u Ciebie ApiResponse ma code + message (jak w AuthService)
        if (resp.getCode() >= 200 && resp.getCode() < 300) {
            System.out.println("[ProfileSync] OK " + op + " userId=" + userId);
        } else {
            System.err.println("[ProfileSync] FAIL " + op + " userId=" + userId
                    + " code=" + resp.getCode()
                    + " msg=" + resp.getMessage());
        }
    }
}
