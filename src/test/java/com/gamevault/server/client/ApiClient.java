package com.gamevault.server.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private final String baseUrl;
    private final HttpClient httpClient;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> send(
            String method,
            String path,
            String body,
            String token
    ) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                    URI.create(baseUrl + path))
                    .header("Content-type", "application/json"
                );

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        if (body != null) {
            requestBuilder.method(
                    method,
                    HttpRequest.BodyPublishers.ofString(body)
            );
        } else {
            requestBuilder.method(
                    method,
                    HttpRequest.BodyPublishers.noBody()
            );
        }

        return httpClient.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
