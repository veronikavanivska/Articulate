package org.example.article.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.article.entities.MEiN.monographs.MonographAuthor;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.MonographChapterAuthor;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.example.article.entities.Publication;
import org.example.article.entities.PublicationCoauthor;
import org.example.article.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
public class ArticleSlotService extends ArticleSlotsServiceGrpc.ArticleSlotsServiceImplBase {

    private final PublicationRepository publicationRepository;
    private final PublicationCoauthorRepository publicationCoauthorRepository;

    private final MonographicRepository monographicRepository;
    private final MonographAuthorRepository monographAuthorRepository;

    private final MonographChapterRepository monographChapterRepository;
    private final MonographChapterAuthorRepository monographChapterAuthorRepository;

    public ArticleSlotService(PublicationRepository publicationRepository,PublicationCoauthorRepository publicationCoauthorRepository, MonographicRepository monographicRepository ,MonographAuthorRepository monographAuthorRepository, MonographChapterRepository monographChapterRepository, MonographChapterAuthorRepository monographChapterAuthorRepository) {
        this.publicationRepository = publicationRepository;
        this.publicationCoauthorRepository = publicationCoauthorRepository;
        this.monographicRepository = monographicRepository;
        this.monographAuthorRepository = monographAuthorRepository;
        this.monographChapterRepository = monographChapterRepository;
        this.monographChapterAuthorRepository = monographChapterAuthorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void getItemForSlots(GetItemForSlotsRequest request,
                                StreamObserver<ItemForSlots> responseObserver) {

        long userId = request.getUserId();
        SlotItemType itemType = request.getItemType();
        long itemId = request.getItemId();

        if (userId <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("userId must be positive.")
                    .asRuntimeException());
            return;
        }
        if (itemId <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("itemId must be positive.")
                    .asRuntimeException());
            return;
        }

        try {
            ItemForSlots out = switch (itemType) {
                case SLOT_ITEM_ARTICLE -> buildForArticle(userId, itemId);
                case SLOT_ITEM_MONOGRAPH -> buildForMonograph(userId, itemId);
                case SLOT_ITEM_CHAPTER -> buildForChapter(userId, itemId);
                default -> throw Status.INVALID_ARGUMENT
                        .withDescription("Unsupported itemType: " + itemType)
                        .asRuntimeException();
            };

            responseObserver.onNext(out);
            responseObserver.onCompleted();

        } catch (io.grpc.StatusRuntimeException e) {
            responseObserver.onError(e);
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN
                    .withDescription("GetItemForSlots failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /* =========================
       ARTICLE
       ========================= */
    private ItemForSlots buildForArticle(long userId, long id) {

        Publication p = publicationRepository.findWithAllRelations(id)
                .orElseThrow(() -> Status.NOT_FOUND
                        .withDescription("Publication not found: " + id)
                        .asRuntimeException());

        // AUTORYZACJA
        // dopasuj nazwę pola jeśli u Ciebie jest inaczej
        if (p.getAuthorId() == null || p.getAuthorId() != userId) {
            throw Status.PERMISSION_DENIED
                    .withDescription("You do not own this publication.")
                    .asRuntimeException();
        }

        String title = p.getTitle() == null ? "" : p.getTitle();
        Integer year = p.getPublicationYear();
        if (year == null) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Publication has no publicationYear: " + id)
                    .asRuntimeException();
        }

        double points = toDoubleSafe(p.getMeinPoints());

        List<PublicationCoauthor> coauthors =
                publicationCoauthorRepository.findByPublicationIdOrderByPosition(p.getId());

        ItemForSlots.Builder b = ItemForSlots.newBuilder()
                .setItemType(SlotItemType.SLOT_ITEM_ARTICLE)
                .setItemId(p.getId())
                .setTitle(title)
                .setPublicationYear(year)
                .setPoints(points);

        for (PublicationCoauthor a : coauthors) {
            long uid = (a.getUserId() == null ? 0L : a.getUserId());
            b.addAuthors(SlotAuthor.newBuilder()
                    .setUserId(uid)
                    .setFullName(a.getFullName() == null ? "" : a.getFullName())
                    .build());
        }

        return b.build();
    }

    /* =========================
       MONOGRAPH
       ========================= */
    private ItemForSlots buildForMonograph(long userId, long id) {

        Monographic m = monographicRepository.findById(id)
                .orElseThrow(() -> Status.NOT_FOUND
                        .withDescription("Monograph not found: " + id)
                        .asRuntimeException());

        // AUTORYZACJA
        if (m.getAuthorId() == null || m.getAuthorId() != userId) {
            throw Status.PERMISSION_DENIED
                    .withDescription("You do not own this monograph.")
                    .asRuntimeException();
        }

        String title = m.getTitle() == null ? "" : m.getTitle();
        Integer year = m.getPublicationYear();
        if (year == null) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Monograph has no publicationYear: " + id)
                    .asRuntimeException();
        }

        double points = toDoubleSafe(m.getMeinPoints());

        List<MonographAuthor> authors =
                monographAuthorRepository.findByMonographIdOrderByPosition(m.getId());

        ItemForSlots.Builder b = ItemForSlots.newBuilder()
                .setItemType(SlotItemType.SLOT_ITEM_MONOGRAPH)
                .setItemId(m.getId())
                .setTitle(title)
                .setPublicationYear(year)
                .setPoints(points);

        for (MonographAuthor a : authors) {
            long uid = (a.getUserId() == null ? 0L : a.getUserId());
            b.addAuthors(SlotAuthor.newBuilder()
                    .setUserId(uid)
                    .setFullName(a.getFullName() == null ? "" : a.getFullName())
                    .build());
        }

        return b.build();
    }

    /* =========================
       CHAPTER
       ========================= */
    private ItemForSlots buildForChapter(long userId, long id) {

        MonographChapter c = monographChapterRepository.findById(id)
                .orElseThrow(() -> Status.NOT_FOUND
                        .withDescription("Chapter not found: " + id)
                        .asRuntimeException());

        // AUTORYZACJA
        if (c.getAuthorId() == null || c.getAuthorId() != userId) {
            throw Status.PERMISSION_DENIED
                    .withDescription("You do not own this chapter.")
                    .asRuntimeException();
        }

        String title = c.getMonograficChapterTitle() == null ? "" : c.getMonograficChapterTitle();
        Integer year = c.getPublicationYear();
        if (year == null) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Chapter has no publicationYear: " + id)
                    .asRuntimeException();
        }

        double points = toDoubleSafe(c.getMeinPoints());

        List<MonographChapterAuthor> authors =
                monographChapterAuthorRepository.findByMonographChapterIdOrderByPosition(c.getId());

        ItemForSlots.Builder b = ItemForSlots.newBuilder()
                .setItemType(SlotItemType.SLOT_ITEM_CHAPTER)
                .setItemId(c.getId())
                .setTitle(title)
                .setPublicationYear(year)
                .setPoints(points);

        for (MonographChapterAuthor a : authors) {
            long uid = (a.getUserId() == null ? 0L : a.getUserId());
            b.addAuthors(SlotAuthor.newBuilder()
                    .setUserId(uid)
                    .setFullName(a.getFullName() == null ? "" : a.getFullName())
                    .build());
        }

        return b.build();
    }

    private double toDoubleSafe(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof Long l) return l.doubleValue();
        if (value instanceof Double d) return d;
        if (value instanceof BigDecimal bd) return bd.doubleValue();
        if (value instanceof Float f) return f.doubleValue();
        return 0.0;
    }
}
