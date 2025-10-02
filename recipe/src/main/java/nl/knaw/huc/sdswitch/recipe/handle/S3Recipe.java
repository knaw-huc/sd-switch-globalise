// foo bar

package nl.knaw.huc.sdswitch.recipe.handle;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;


public class S3Recipe implements Recipe<Void> {
    @Override
    public void validateConfig(Void config, Set<String> pathParams) throws RecipeValidationException {
        if (!pathParams.contains("prefix"))
            throw new RecipeValidationException("Missing required path parameter 'prefix'");
        if (!pathParams.contains("uuid"))
            throw new RecipeValidationException("Missing required path parameter 'uuid'");
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        String prefix = data.pathParam("prefix");
        String uuid = data.pathParam("uuid");
        if(uuid != "") {
            String result = model(uuid);
            String contentType = "application/json";
            return RecipeResponse.withBody(result, contentType);
        }

        String url = "https://globalise.huygens.knaw.nl/#" + "/" + uuid + "?noredirect";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String body = response.body();
//        return RecipeResponse.withRedirect(url, 301);
        String contentType = "application/json";
        return RecipeResponse.withBody(body, contentType);
    }

    public String model(String key) {
        String result = "";
        String bucketName = "globalise-test";

//      Deze drie uit ENV halen
        String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
        String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String endpoint = System.getenv("AWS_S3_ENDPOINT_URL");

        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        );

        S3Client s3Client = S3Client.builder()
                .credentialsProvider(credentials)
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(srvConf -> {
                    srvConf.pathStyleAccessEnabled(true);
                })
                .region(Region.US_EAST_1)  // region is ignored
                .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseInputStream));
        String line = "";
        while (true) {
            try {
                if (!Objects.nonNull(line = bufferedReader.readLine())) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(line);
            result = line;
        }
        return result;
    }
}
