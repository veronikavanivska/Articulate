package org.example.article.helpers;

import com.example.generated.DisciplineUpsertItem;
import org.example.article.clients.ProfileClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DisciplineSyncService {

    public void syncUpsert(long id, String name) {
        try {
            ProfileClient.upsertDiscipline(id, name);
            System.out.println("[DisciplineSync] upsert id=" + id);
        } catch (Exception e) {
            System.err.println("[DisciplineSync] failed upsert id=" + id + ": " + e.getMessage());
        }
    }

    public void syncUpserts(Map<Long, String> touched) {
        try {
            List<DisciplineUpsertItem> items = new ArrayList<>(touched.size());
            for (var e : touched.entrySet()) {
                String name = e.getValue() == null ? "" : e.getValue().trim();
                if (e.getKey() != null && e.getKey() > 0 && !name.isBlank()) {
                    items.add(DisciplineUpsertItem.newBuilder()
                            .setId(e.getKey())
                            .setName(name)
                            .build());
                }
            }

            if (items.isEmpty()) return;

            ProfileClient.upsertDisciplines(items);
            System.out.println("[DisciplineSync] upserted=" + items.size());
        } catch (Exception e) {
            System.err.println("[DisciplineSync] failed batch upsert: " + e.getMessage());
        }
    }

    public void syncDelete(long id) {
        try {
            ProfileClient.deleteDiscipline(id);
            System.out.println("[DisciplineSync] delete id=" + id);
        } catch (Exception e) {
            System.err.println("[DisciplineSync] failed delete id=" + id + ": " + e.getMessage());
        }
    }
}