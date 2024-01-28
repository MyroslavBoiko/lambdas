package com.task10.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientDescription;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;

public class CognitoService {
    private static final String USER_POOL_NAME = "cmtr-86ce70a2-simple-booking-userpool-test";
    private String userPoolIdByName;
    private CognitoIdentityProviderClient cognitoClient;

    public CognitoService() {
        cognitoClient = CognitoIdentityProviderClient.builder().region(Region.EU_CENTRAL_1).build();
        userPoolIdByName = findUserPoolIdByName(USER_POOL_NAME);
    }

    private String findUserPoolIdByName(String userPoolName) {
        ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder().maxResults(60).build();
        ListUserPoolsResponse response = cognitoClient.listUserPools(listUserPoolsRequest);

        for (UserPoolDescriptionType userPool : response.userPools()) {
            if (userPool.name().equals(userPoolName)) {
                return userPool.id();
            }
        }
        return null;
    }

    private String findClientlId() {
        ListUserPoolClientsRequest listUserPoolClientsRequest = ListUserPoolClientsRequest.builder()
                .userPoolId(userPoolIdByName)
                .build();
        ListUserPoolClientsResponse listUserPoolClientsResponse =
                cognitoClient.listUserPoolClients(listUserPoolClientsRequest);
        Optional<UserPoolClientDescription> first = listUserPoolClientsResponse.userPoolClients().stream().findFirst();
        return first.map(UserPoolClientDescription::clientId).orElse(null);
    }

    public boolean signUpNewUser(String firstName, String lastName, String email, String password) {
        SignUpResponse signUpResponse = cognitoClient.signUp(SignUpRequest.builder()
                .clientId(findClientlId())
                .username(email)
                .userAttributes(
                        AttributeType.builder().name("email").value(email).build(),
                        AttributeType.builder().name("name").value(firstName).build()
                               )
                .password(password)
                .build());

        if (signUpResponse.sdkHttpResponse().isSuccessful()) {
            System.out.println("Sign up response success");
            AdminConfirmSignUpRequest signUpRequest = AdminConfirmSignUpRequest.builder()
                    .userPoolId(userPoolIdByName)
                    .username(email)
                    .build();
            AdminConfirmSignUpResponse adminConfirmSignUpResponse =
                    cognitoClient.adminConfirmSignUp(signUpRequest);

            return adminConfirmSignUpResponse.sdkHttpResponse().isSuccessful();
        }

        return true;
    }

    public AdminInitiateAuthResponse signIn(String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put("USERNAME", username);
        map.put("PASSWORD", password);

        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(userPoolIdByName)
                .clientId(findClientlId())
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .authParameters(map)
                .build();

        AdminInitiateAuthResponse adminInitiateAuthResponse = cognitoClient.adminInitiateAuth(authRequest);

        return adminInitiateAuthResponse;
    }

    public boolean validToken(String accessToken) {
        GetUserRequest userRequest = GetUserRequest.builder().accessToken(accessToken).build();

        try {
            cognitoClient.getUser(userRequest);
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public void changePassword(String accessToken, String previousPassword, String proposedPassword) {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .accessToken(accessToken)
                .previousPassword(previousPassword)
                .proposedPassword(proposedPassword)
                .build();

        cognitoClient.changePassword(changePasswordRequest);
    }
}