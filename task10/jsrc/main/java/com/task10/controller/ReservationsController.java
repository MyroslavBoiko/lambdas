package com.task10.controller;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.task10.model.Reservation;
import com.task10.model.ReservationResponse;
import com.task10.model.ReservationsResponse;
import com.task10.service.DynamoDbService;

public class ReservationsController {

    private static final Gson gson = new Gson();

    private DynamoDbService dynamoDbService;

    public ReservationsController(String tableTables, String tableReservations) {
        dynamoDbService = new DynamoDbService(tableTables, tableReservations);
    }

    public APIGatewayProxyResponseEvent postReservation(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            Reservation reservation = gson.fromJson(request.getBody(), Reservation.class);
            System.out.println(reservation);
            ReservationResponse reservationResponse = dynamoDbService.createReservation(reservation);
            String body = null;
            body = new ObjectMapper().writeValueAsString(reservationResponse);

            System.out.println(body);
            response.setBody(body);
            response.setStatusCode(200);
        } catch (RuntimeException e) {
            response.setStatusCode(400);
            response.setBody(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public APIGatewayProxyResponseEvent getReservations(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            ReservationsResponse allReservations = dynamoDbService.getAllReservations();
            response.setBody(new ObjectMapper().writeValueAsString(allReservations));
            response.setStatusCode(200);
        } catch (RuntimeException e) {
            response.setStatusCode(400);
            response.setBody("Invalid request");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

}
