package org.example.article.clients;

import com.example.generated.DeleteDisciplineRequest;
import com.example.generated.DisciplineUpsertItem;
import com.example.generated.ProfilesDisciplineSyncServiceGrpc;
import com.example.generated.UpsertDisciplinesRequest;
import io.grpc.Channel;
import org.example.article.Client;
import org.springframework.stereotype.Component;

import java.util.List;

@Client(host = "${profiles.server.host}", port = "${profiles.server.port}")
@Component
public class ProfileClient {

    private ProfilesDisciplineSyncServiceGrpc.ProfilesDisciplineSyncServiceBlockingStub stub;

    public void init(Channel channel) {
        stub = ProfilesDisciplineSyncServiceGrpc.newBlockingStub(channel);
    }

    public void upsertDisciplines(List<DisciplineUpsertItem> items) {
        if (stub == null) throw new IllegalStateException("ProfilesClient not initialized");
        var req = UpsertDisciplinesRequest.newBuilder().addAllItems(items).build();
        stub.upsertDisciplines(req);
    }

    public void upsertDiscipline(long id, String name) {
        upsertDisciplines(List.of(
                DisciplineUpsertItem.newBuilder()
                        .setId(id)
                        .setName(name == null ? "" : name.trim())
                        .build()
        ));
    }

    public void deleteDiscipline(long id) {
        if (stub == null) throw new IllegalStateException("ProfilesClient not initialized");
        stub.deleteDiscipline(DeleteDisciplineRequest.newBuilder().setId(id).build());
    }
}