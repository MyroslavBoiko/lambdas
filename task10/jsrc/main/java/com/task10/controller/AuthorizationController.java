package com.task10.controller;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.task10.model.SignInRequest;
import com.task10.model.SignUpRequest;
import com.task10.service.CognitoService;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;

public class AuthorizationController {

    private static final Gson gson = new Gson();

    private final CognitoService cognitoService;

    public AuthorizationController() {
        this.cognitoService = new CognitoService();
    }

    public APIGatewayProxyResponseEvent signUp(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            SignUpRequest signUpRequest = gson.fromJson(request.getBody(), SignUpRequest.class);

            // Put sign-up logic with Cognito here...
            boolean successfull = cognitoService.signUpNewUser(signUpRequest.getFirstName(),
                    signUpRequest.getLastName(),
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword());

            response.setStatusCode(successfull ? 200 : 400);
        } catch (RuntimeException e) {
            response.setStatusCode(400);
            response.setBody(e.getMessage());
        }

        System.out.println(response);
        return response;
    }

    public APIGatewayProxyResponseEvent signIn(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            SignInRequest signInRequest = gson.fromJson(request.getBody(), SignInRequest.class);

            AdminInitiateAuthResponse adminInitiateAuthResponse = cognitoService.signIn(signInRequest.getEmail(),
                    signInRequest.getPassword());

            response.setStatusCode(200);
            String accessToken = adminInitiateAuthResponse.authenticationResult().accessToken();
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("accessToken", accessToken);
            response.setBody(new ObjectMapper().writeValueAsString(responseMap));
        } catch (RuntimeException e) {
            response.setStatusCode(400);
            response.setBody("Invalid request");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return response;
    }
}
