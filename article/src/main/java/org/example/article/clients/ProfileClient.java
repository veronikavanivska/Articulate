package org.example.article.clients;

import com.example.generated.DeleteDisciplineRequest;
import com.example.generated.DisciplineUpsertItem;
import com.example.generated.ProfilesDisciplineSyncServiceGrpc;
import com.example.generated.UpsertDisciplinesRequest;
import io.grpc.Channel;
import org.example.article.Client;

import java.util.List;

@Client(host = "${profiles.server.host}", port = "${profiles.server.port}")
public class ProfileClient {

    private static ProfilesDisciplineSyncServiceGrpc.ProfilesDisciplineSyncServiceBlockingStub stub;

    public static void init(Channel channel) {
        stub = ProfilesDisciplineSyncServiceGrpc.newBlockingStub(channel);
    }

    public static void upsertDisciplines(List<DisciplineUpsertItem> items) {
        if (stub == null) throw new IllegalStateException("ProfilesClient not initialized");
        var req = UpsertDisciplinesRequest.newBuilder().addAllItems(items).build();
        stub.upsertDisciplines(req);
    }

    public static void upsertDiscipline(long id, String name) {
        upsertDisciplines(List.of(
                DisciplineUpsertItem.newBuilder()
                        .setId(id)
                        .setName(name == null ? "" : name.trim())
                        .build()
        ));
    }

    public static void deleteDiscipline(long id) {
        if (stub == null) throw new IllegalStateException("ProfilesClient not initialized");
        stub.deleteDiscipline(DeleteDisciplineRequest.newBuilder().setId(id).build());
    }
}