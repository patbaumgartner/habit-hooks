package com.patbaumgartner.habithooks.update;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Minimal HTTP access seam used by {@link SelfUpdater}.
 *
 * <p>
 * Extracted as an interface so the self-update flow can be exercised in tests without
 * performing real network I/O.
 */
public interface HttpFetcher {

    /**
     * Fetches the body of the given URL as text.
     * @param url the absolute URL to request
     * @return the response body
     * @throws IOException if the request fails or returns a non-success status
     * @throws InterruptedException if the calling thread is interrupted
     */
    String getText(String url) throws IOException, InterruptedException;

    /**
     * Downloads the given URL to the destination file.
     * @param url the absolute URL to download
     * @param destination the file to write the response body into
     * @throws IOException if the request fails or returns a non-success status
     * @throws InterruptedException if the calling thread is interrupted
     */
    void downloadTo(String url, Path destination) throws IOException, InterruptedException;

}
