package com.audiotranscriptionservice.service;

import com.audiotranscriptionservice.service.TranscriptResult;
import com.audiotranscriptionservice.service.internal.GetTranscriptRequest;
import com.audiotranscriptionservice.service.internal.GetTranscriptResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.Objects;
import java.util.logging.Logger;

public class ATSCoreService {
    // make a call to the mock ATS service
    // handle retries
    // handle failures
    // how would you scale if this core service becomes a bottleneck?
    private CloseableHttpClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    // only for unit testing
    public ATSCoreService() {
    }

    // very basic no validations or response null check
    @Nullable
    public GetTranscriptResponse getTranscript(final GetTranscriptRequest request) {
        // Build the URI with a query parameter
        GetTranscriptResponse getTranscriptResponse = null;
        // add validation for jobId
        Objects.requireNonNull(request, "GetTranscriptRequest found null ");
        String audioFilePath = Objects.requireNonNull(request.getAudioFilePath(), "Audio file path in GetTranscriptRequest found null.");

        try {
            URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("localhost:3000")
                .setPath("/get-asr-output")
                .setParameter("path", audioFilePath) // Add parameters here
                .build();

            HttpGet getRequest = new HttpGet(uri);
            this.client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(getRequest);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseBody = EntityUtils.toString(response.getEntity());
                // construct response object
                JsonNode responseJson = objectMapper.readTree(responseBody);
                String path = responseJson.get("path").asText();
                String transcript = responseJson.get("transcript").asText();
                // construct response
                getTranscriptResponse = new GetTranscriptResponse();
                getTranscriptResponse.setTranscript(transcript);
                getTranscriptResponse.setFilePath(path);

                EntityUtils.consume(response.getEntity());
            } else {
                // log failure
                // JUL logger
            }
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getTranscriptResponse;
    }

//    @Nullable
//    public SearchTranscriptResponse searchTranscripts(final String jobStatus, final String userId) {
//
//    }
//
//    @Nullable
//    public TranscribeResponse transcribe(final String[] audioChunkPaths, final String userId) {
//
//    }
}
