package com.task07;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

@LambdaHandler(lambdaName = "uuid_generator",
        roleName = "uuid_generator-role"
)
@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
public class UuidGenerator implements RequestHandler<CloudWatchLogsEvent, Void> {

    private AmazonS3 s3Client;
    private ObjectMapper objectMapper;

    public UuidGenerator() {
        this.s3Client = AmazonS3Client.builder().withRegion(Regions.EU_CENTRAL_1).build();
        this.objectMapper = new ObjectMapper();
    }

    public Void handleRequest(CloudWatchLogsEvent event, Context context) {
        String bucketName = System.getenv("target_bucket");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String isoDateTime = LocalDateTime.now().format(formatter);

        List<String> uuidList = IntStream.range(0, 10)
                .mapToObj(value -> UUID.randomUUID())
                .map(UUID::toString)
                .collect(Collectors.toList());
        context.getLogger().log(event.toString());
        s3Client.putObject(bucketName, isoDateTime, convertObjectToJson(new Content(uuidList)));

        return null;
    }

    private String convertObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Object cannot be converted to JSON: " + object);
        }
    }

    private class Content {
        private List<String> ids;

        public Content(List<String> ids) {
            this.ids = ids;
        }

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }
    }
}
