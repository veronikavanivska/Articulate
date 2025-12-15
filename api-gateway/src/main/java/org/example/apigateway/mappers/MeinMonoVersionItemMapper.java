package org.example.apigateway.mappers;

import com.example.generated.AdminGetMeinMonoVersionResponse;
import com.example.generated.MeinVersionItem;
import com.google.protobuf.Timestamp;
import org.example.apigateway.responses.mono.MeinMonoVersionItem;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class MeinMonoVersionItemMapper {

    public static MeinMonoVersionItem map(com.example.generated.MeinMonoVersionItem meinMonoVersion) {


        Timestamp timestamp = meinMonoVersion.getImportedAt();
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());


        MeinMonoVersionItem meinMonoVersionItem = new MeinMonoVersionItem();
        meinMonoVersionItem.setId(meinMonoVersion.getId());
        meinMonoVersionItem.setLabel(meinMonoVersion.getLabel());
        meinMonoVersionItem.setSourceFilename(meinMonoVersion.getSourceFilename());
        meinMonoVersionItem.setImportedBy(meinMonoVersion.getImportedBy());
        meinMonoVersionItem.setImportedAt(instant);
        meinMonoVersionItem.setPublishers(meinMonoVersion.getPublishers());

        return meinMonoVersionItem;
    }
}
