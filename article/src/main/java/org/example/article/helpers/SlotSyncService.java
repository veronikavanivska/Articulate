package org.example.article.helpers;


import com.example.generated.SlotItemType;
import org.example.article.clients.SlotsClient;
import org.example.article.repositories.PublicationCoauthorRepository;
import org.springframework.stereotype.Service;


@Service
public class SlotSyncService {


    private final PublicationCoauthorRepository publicationCoauthorRepository;

    public SlotSyncService(PublicationCoauthorRepository publicationCoauthorRepository) {
        this.publicationCoauthorRepository = publicationCoauthorRepository;
    }

    public void syncUpdated(SlotItemType type, long id) {

        try {
            SlotsClient.notifyUpdated(type, id);
            System.out.println("[SlotSync] updated id=" + id);
        } catch (Exception e) {
            System.err.println("[SlotSync] failed updated id=" + id + ": " + e.getMessage());
        }
    }

    public void syncDeleted(SlotItemType type, long id) {
        try {
            SlotsClient.notifyDeleted(type, id);
            System.out.println("[SlotSync] deleted id=" + id);
        } catch (Exception e) {
            System.err.println("[SlotSync] failed deleted id=" + id + ": " + e.getMessage());
        }
    }
}