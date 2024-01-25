package com.task06;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

@LambdaHandler(lambdaName = "audit_producer",
        roleName = "audit_producer-role"
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@EnvironmentVariable(key = "target_table", value = "${target_table}")
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

    private static final String VALUE = "value";
    private static final String KEY = "key";

    @Override
    public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        AmazonDynamoDB amazonDynamoDB =
                AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
            context.getLogger().log(record.toString());
            String eventName = record.getEventName();
            if ("INSERT".equals(eventName)) {

                Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage
                        = record.getDynamodb().getNewImage();

                Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> attributesMap = new HashMap<>();
                attributesMap.put("id", new AttributeValue(UUID.randomUUID().toString()));
                attributesMap.put("itemKey", new AttributeValue(newImage.get(KEY).getS()));
                attributesMap.put("modificationTime", new AttributeValue(ZonedDateTime.now().format(formatter)));

                HashMap<String, AttributeValue> newValue = new HashMap<>();
                newValue.put(KEY, new AttributeValue(newImage.get(KEY).getS()));
                newValue.put(VALUE, new AttributeValue(newImage.get(VALUE).getS()));

                attributesMap.put("newValue", new AttributeValue().withM(
                        newValue));

                context.getLogger().log(attributesMap.toString());

                amazonDynamoDB.putItem(System.getenv("target_table"), attributesMap);
            } else if ("MODIFY".equals(eventName)) {
                Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage
                        = record.getDynamodb().getNewImage();
                Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> oldImage =
                        record.getDynamodb().getOldImage();

                Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> attributesMap = new HashMap<>();
                attributesMap.put("id", new AttributeValue(UUID.randomUUID().toString()));
                attributesMap.put("itemKey", new AttributeValue(newImage.get(KEY).getS()));
                attributesMap.put("modificationTime", new AttributeValue(ZonedDateTime.now().format(formatter)));
                attributesMap.put("updatedAttribute", new AttributeValue(VALUE));
                attributesMap.put("oldValue", new AttributeValue(oldImage.get(VALUE).getS()));
                attributesMap.put("newValue", new AttributeValue(newImage.get(VALUE).getS()));

                context.getLogger().log(attributesMap.toString());

                amazonDynamoDB.putItem(System.getenv("target_table"), attributesMap);
                ;
            }
        }
        return null;
    }
}
