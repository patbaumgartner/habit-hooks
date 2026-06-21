package com.patbaumgartner.habithooks.update;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

/**
 * {@link HttpFetcher} implementation backed by the JDK {@link HttpClient}.
 */
public final class JdkHttpFetcher implements HttpFetcher {

    private static final String USER_AGENT = "habit-hooks-updater";

    private static final int CONNECT_TIMEOUT_SECONDS = 20;

    private static final int STATUS_FAMILY_DIVISOR = 100;

    private static final int SUCCESS_FAMILY = 2;

    private final HttpClient client;

    /**
     * Creates a fetcher with a redirect-following client and a connect timeout.
     */
    public JdkHttpFetcher() {
        this.client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .build();
    }

    @Override
    public String getText(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", USER_AGENT)
            .GET()
            .build();
        HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response.statusCode(), url);
        return response.body();
    }

    @Override
    public void downloadTo(String url, Path destination) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).header("User-Agent", USER_AGENT).GET().build();
        HttpResponse<Path> response = this.client.send(request, HttpResponse.BodyHandlers.ofFile(destination));
        ensureSuccess(response.statusCode(), url);
    }

    private static void ensureSuccess(int statusCode, String url) throws IOException {
        if (statusCode / STATUS_FAMILY_DIVISOR != SUCCESS_FAMILY) {
            throw new IOException("HTTP " + statusCode + " for " + url);
        }
    }

}
