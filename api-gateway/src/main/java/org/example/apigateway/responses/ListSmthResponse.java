package org.example.apigateway.responses;

import lombok.Data;
import org.example.apigateway.responses.articles.PageMeta;

import java.util.List;

@Data
public class ListSmthResponse<T> {
    List<T> item;
    PageMeta pageMeta;
}
