package org.example.profiles.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileDiscSync extends ProfilesDisciplineSyncServiceGrpc.ProfilesDisciplineSyncServiceImplBase {

    private final JdbcTemplate jdbcTemplate;

    public ProfileDiscSync(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void upsertDisciplines(UpsertDisciplinesRequest request,
                                  StreamObserver<UpsertDisciplinesResponse> responseObserver) {
        try {
            final String UPSERT = """
                INSERT INTO discipline(id, name)
                VALUES (?, ?)
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name
            """;

            var items = request.getItemsList();
            if (items == null || items.isEmpty()) {
                responseObserver.onNext(UpsertDisciplinesResponse.newBuilder().setUpserted(0).build());
                responseObserver.onCompleted();
                return;
            }

            // Walidacja/filtracja
            var filtered = new java.util.ArrayList<DisciplineUpsertItem>(items.size());
            for (var it : items) {
                long id = it.getId();
                String name = it.getName() == null ? "" : it.getName().trim();
                if (id > 0 && !name.isBlank()) {
                    filtered.add(DisciplineUpsertItem.newBuilder().setId(id).setName(name).build());
                }
            }

            if (filtered.isEmpty()) {
                responseObserver.onNext(UpsertDisciplinesResponse.newBuilder().setUpserted(0).build());
                responseObserver.onCompleted();
                return;
            }

            jdbcTemplate.batchUpdate(UPSERT, filtered, 200, (ps, it) -> {
                ps.setLong(1, it.getId());
                ps.setString(2, it.getName());
            });

            responseObserver.onNext(UpsertDisciplinesResponse.newBuilder()
                    .setUpserted(filtered.size())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void deleteDiscipline(DeleteDisciplineRequest request,
                                 StreamObserver<DeleteDisciplineResponse> responseObserver) {
        try {
            long id = request.getId();
            if (id <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("id must be > 0").asRuntimeException());
                return;
            }

            jdbcTemplate.update("DELETE FROM discipline WHERE id = ?", id);

            responseObserver.onNext(DeleteDisciplineResponse.newBuilder().setId(id).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
