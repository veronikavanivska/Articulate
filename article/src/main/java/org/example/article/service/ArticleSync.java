package org.example.article.service;

import com.example.generated.ArticleProfileSyncServiceGrpc;
import com.example.generated.SyncAuthorFullNameRequest;
import com.example.generated.SyncAuthorFullNameResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.article.repositories.MonographAuthorRepository;
import org.example.article.repositories.MonographChapterAuthorRepository;
import org.example.article.repositories.PublicationCoauthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleSync extends ArticleProfileSyncServiceGrpc.ArticleProfileSyncServiceImplBase {

    private final PublicationCoauthorRepository publicationCoauthorRepository;
    private final MonographAuthorRepository monographAuthorRepository;
    private final MonographChapterAuthorRepository monographChapterAuthorRepository;

    public ArticleSync(PublicationCoauthorRepository p,
                                         MonographAuthorRepository m,
                                         MonographChapterAuthorRepository c) {
        this.publicationCoauthorRepository = p;
        this.monographAuthorRepository = m;
        this.monographChapterAuthorRepository = c;
    }

    @Override
    @Transactional
    public void syncAuthorFullName(SyncAuthorFullNameRequest request,
                                   StreamObserver<SyncAuthorFullNameResponse> responseObserver) {

        long userId = request.getUserId();
        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();

        if (userId <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("user_id must be > 0").asRuntimeException());
            return;
        }

        int updPub = publicationCoauthorRepository.updateFullNameByUserId(userId, fullName);
        int updMon = monographAuthorRepository.updateFullNameByUserId(userId, fullName);
        int updCh  = monographChapterAuthorRepository.updateFullNameByUserId(userId, fullName);

        SyncAuthorFullNameResponse resp = SyncAuthorFullNameResponse.newBuilder()
                .setUpdatedPublicationCoauthor(updPub)
                .setUpdatedMonographAuthor(updMon)
                .setUpdatedMonographChapterAuthor(updCh)
                .setTotalUpdated(updPub + updMon + updCh)
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
