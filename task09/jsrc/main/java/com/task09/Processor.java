package com.task09;

import java.util.UUID;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

@LambdaHandler(lambdaName = "processor",
		roleName = "processor-role",
		tracingMode = TracingMode.Active
)
@EnvironmentVariable(key = "target_table", value = "${target_table}")
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class Processor implements RequestHandler<Object, String> {

	public static void main(String[] args) {
		Processor processor = new Processor();
		processor.handleRequest(null, null);
	}

	public String handleRequest(Object request, Context context) {

		String targetTable = System.getenv("target_table");
		WeatherForecast.Forecast weatherForecast = OpenMeteoXRayApi.getWeatherForecast();
		try {
			context.getLogger().log(new ObjectMapper().writeValueAsString(weatherForecast));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		AmazonDynamoDB amazonDynamoDB =
				AmazonDynamoDBClientBuilder.standard()
						.withRegion(Regions.EU_CENTRAL_1)
						.withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
						.build();
		// Create DynamoDBMapperConfig with the retrieved table name
		DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
				.withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(targetTable))
				.build();

		// Create DynamoDBMapper with the DynamoDB client and the mapper config
		DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, mapperConfig);


		WeatherForecast wf = new WeatherForecast();
		wf.setId(UUID.randomUUID().toString());
		wf.setForecast(weatherForecast);
		dynamoDBMapper.save(wf);

		return "Weather data successfully imported.";
	}
}
