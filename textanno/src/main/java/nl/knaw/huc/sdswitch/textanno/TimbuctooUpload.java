package nl.knaw.huc.sdswitch.textanno;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class TimbuctooUpload {
    private final String url;
    private final CloseableHttpClient httpClient;

    @JsonCreator
    public TimbuctooUpload(String url) {
        this.url = url;
        httpClient = HttpClients.createMinimal();
    }

    public void uploadRDF(String userId, String datasetName, String authorization,
                          byte[] body, String contentType)
            throws IOException, TimbuctooUploadException, URISyntaxException {
        URI uploadEndpoint = new URI(url + "/" + userId + "/" + datasetName + "/upload/rdf?async=true&forceCreation=true");

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addTextBody("encoding", "UTF-8")
                .addBinaryBody("file", body, ContentType.create(contentType, Consts.UTF_8), UUID.randomUUID() + ".ttl")
                .build();

        HttpPost httpPost = new HttpPost(uploadEndpoint);
        httpPost.setHeader("Authorization", authorization);
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() != 202)
                throw new TimbuctooUploadException(
                        "Failed to upload RDF to Timbuctoo: " + response.getStatusLine().getStatusCode() +
                                " - " + response.getStatusLine().getReasonPhrase());
        }
    }

    public static class TimbuctooUploadException extends Exception {
        public TimbuctooUploadException(String message) {
            super(message);
        }
    }
}
