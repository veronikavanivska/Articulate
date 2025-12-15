package org.example.apigateway.mappers;

import org.example.apigateway.responses.articles.PageMeta;

public class PageMetaMapper {

    public static PageMeta toPageMeta(com.example.generated.PageMeta pageMeta) {
        PageMeta pageMetaItem = new PageMeta();

        pageMetaItem.setPage(pageMeta.getPage());
        pageMetaItem.setSize(pageMeta.getSize());
        pageMetaItem.setTotalItems(pageMeta.getTotalItems());
        pageMetaItem.setTotalPages(pageMeta.getTotalPages());

        return pageMetaItem;
    }
}
