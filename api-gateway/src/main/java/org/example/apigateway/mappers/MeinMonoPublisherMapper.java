package org.example.apigateway.mappers;

import com.example.generated.AdminGetMeinMonoPublisherResponse;
import org.example.apigateway.responses.mono.MeinMonoPublisherItem;

public class MeinMonoPublisherMapper {

    public static MeinMonoPublisherItem map(com.example.generated.MeinMonoPublisherItem item) {

        MeinMonoPublisherItem meinMonoPublisherItem = new MeinMonoPublisherItem();
        meinMonoPublisherItem.setId(item.getId());
        meinMonoPublisherItem.setTitle(item.getTitle());
        meinMonoPublisherItem.setPoints(item.getPoints());
        meinMonoPublisherItem.setVersionId(item.getVersionId());
        meinMonoPublisherItem.setLevel(item.getLevel());

        return meinMonoPublisherItem;
    }
}
