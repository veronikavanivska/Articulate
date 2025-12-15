package org.example.apigateway.mappers;

import com.google.protobuf.Timestamp;
import org.example.apigateway.responses.articles.MeinVersionItem;

import java.time.Instant;

public class MeinVersionItemMapper {

    public static MeinVersionItem toMeinVersionItem(com.example.generated.MeinVersionItem protoMeinVersionItem) {
        MeinVersionItem meinVersionItem = new MeinVersionItem();

        Timestamp timestamp = protoMeinVersionItem.getImportedAt();
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

        meinVersionItem.setId(protoMeinVersionItem.getId());
        meinVersionItem.setActive(protoMeinVersionItem.getIsActive());
        meinVersionItem.setJournals(protoMeinVersionItem.getJournals());
        meinVersionItem.setJournalCodes(protoMeinVersionItem.getJournalCodes());
        meinVersionItem.setLabel(protoMeinVersionItem.getLabel());
        meinVersionItem.setSourceFilename(protoMeinVersionItem.getSourceFilename());
        meinVersionItem.setImportedAt(instant);
        meinVersionItem.setImportedBy(protoMeinVersionItem.getImportedBy());

        return meinVersionItem;

    }
}
