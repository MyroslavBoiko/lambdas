package com.task10.controller;

import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.task10.model.Table;
import com.task10.model.TablesResponse;
import com.task10.service.CognitoService;
import com.task10.service.DynamoDbService;

public class TablesController {


    private CognitoService cognitoService;
    private DynamoDbService dynamoDbService;

    public TablesController(String tableTables, String tableReservations) {
        cognitoService = new CognitoService();
        dynamoDbService = new DynamoDbService(tableTables, tableReservations);
    }

    private static final Gson gson = new Gson();

    public APIGatewayProxyResponseEvent getTables(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        if (request.getHeaders() == null || request.getHeaders().get("Authorization") == null) {
            response.setStatusCode(401);
            return response;
        }
        String authorization = request.getHeaders().get("Authorization");
        String clearToken = authorization.replace("Bearer", "").trim();
        // This is a GET function, so we don't need to parse the body.
        boolean validated = cognitoService.validToken(clearToken);

        if (!validated) {
            response.setStatusCode(401);
            return response;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TablesResponse allTables = dynamoDbService.getAllTables();
            response.setBody(objectMapper.writeValueAsString(allTables));
            response.setStatusCode(200);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public APIGatewayProxyResponseEvent postTable(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        // implementation here...
        try {
            Table table = gson.fromJson(request.getBody(), Table.class);

            int tableId = dynamoDbService.createTable(table);
            // Put sign-in logic with Cognito here...

            response.setStatusCode(200);
            HashMap<String, Integer> value = new HashMap<>();
            value.put("id", tableId);
            response.setBody(new ObjectMapper().writeValueAsString(value));
        } catch (RuntimeException e) {
            response.setStatusCode(400);
            response.setBody("Invalid request");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public APIGatewayProxyResponseEvent getTableById(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        String tableId = request.getPathParameters().get("tableId");
        if (tableId == null) {
            response.setStatusCode(400);
            return response;
        }
        Table tableById = dynamoDbService.getTableById(Integer.parseInt(tableId));
        response.setStatusCode(200);
        try {
            response.setBody(new ObjectMapper().writeValueAsString(tableById));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
