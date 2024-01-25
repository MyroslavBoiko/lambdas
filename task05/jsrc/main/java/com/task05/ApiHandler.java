package com.task05;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task05.dto.Event;
import com.task05.dto.EventRequest;
import com.task05.dto.EventResponse;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role"
)
@DynamoDbTriggerEventSource(targetTable = "${target_table}", batchSize = 1)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class ApiHandler implements RequestHandler<EventRequest, EventResponse> {
	@Override
	public EventResponse handleRequest(EventRequest eventRequest, Context context) {

		context.getLogger().log(eventRequest.toString());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		Event event = new Event();
		event.setId(UUID.randomUUID());
		event.setPrincipalId(eventRequest.getPrincipalId());
		event.setCreatedAt(ZonedDateTime.now().format(formatter));
		event.setBody(eventRequest.getContent());

		EventResponse eventResponse = new EventResponse();
		eventResponse.setStatusCode(201);
		eventResponse.setEvent(event);
		AmazonDynamoDB amazonDynamoDB =
				AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

		Map<String, AttributeValue> attributesMap = new HashMap<>();

		attributesMap.put("id", new AttributeValue(String.valueOf(event.getId())));
		attributesMap.put("principalId", new AttributeValue().withN(String.valueOf(event.getPrincipalId())));
		attributesMap.put("createdAt", new AttributeValue(event.getCreatedAt()));
		attributesMap.put("body", new AttributeValue().withM(
				event.getBody().entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, o -> new AttributeValue(o.getValue())))));

		context.getLogger().log(attributesMap.toString());

		amazonDynamoDB.putItem(System.getenv("target_table"), attributesMap);
		context.getLogger().log("Item added");
		context.getLogger().log(eventResponse.toString());
		return eventResponse;
	}
}
