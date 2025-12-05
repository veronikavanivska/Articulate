package org.example.article.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.article.ETL.IsbnUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.article.ETL.IsbnUtil.normalizePublisherName;

//TODO: ask if i need to check the match and what to do if it is not in database
@Service
public class OpenLibraryIsbnService {
    private static final String BASE_URL = "https://openlibrary.org/api/books";

    private final RestTemplate restTemplate;

    public OpenLibraryIsbnService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .rootUri(BASE_URL)
                .build();
    }

    public Optional<String> findPublisherByIsbn(String rawIsbn) {
        String isbn13 = IsbnUtil.normalizeIsbn13Strict(rawIsbn);

        String url = "?bibkeys=ISBN:" + isbn13 + "&format=json&jscmd=data";

        HttpHeaders headers = new HttpHeaders();

        headers.set(HttpHeaders.USER_AGENT,
                "Articulate (https://github.com/veronikavanivska)");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, OpenLibraryBookData>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        Map<String, OpenLibraryBookData> body = response.getBody();
        if (body == null || body.isEmpty()) {
            return Optional.empty();
        }

        OpenLibraryBookData data = body.values().iterator().next();
        if (data.publishers == null || data.publishers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(data.publishers.get(0).name);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenLibraryBookData {
        public List<Publisher> publishers;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Publisher {
        public String name;
    }

    public boolean publisherMatches(String isbn, String localPublisher) {
        if (localPublisher == null || localPublisher.isBlank()) {
            return false;
        }

        return findPublisherByIsbn(isbn)
                .map(remote -> normalizePublisherName(remote)
                        .equals(normalizePublisherName(localPublisher)))
                .orElse(false);
    }
}
