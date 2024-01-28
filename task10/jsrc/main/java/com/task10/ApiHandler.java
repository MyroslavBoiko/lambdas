package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task10.controller.AuthorizationController;
import com.task10.controller.ReservationsController;
import com.task10.controller.TablesController;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role"
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "tables_table", value = "${tables_table}"),
		@EnvironmentVariable(key = "reservations_table", value = "${reservations_table}")
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Gson gson = new Gson();
	private final TablesController tablesController = new TablesController(System.getenv("tables_table"),
			System.getenv("reservations_table"));
	private final ReservationsController reservationsController = new ReservationsController(System.getenv(
			"tables_table"),
			System.getenv("reservations_table"));
	private final AuthorizationController authorizationController = new AuthorizationController();


	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		context.getLogger().log(request.toString());

		switch (request.getResource()) {
			case "/signup":
				return authorizationController.signUp(request);
			case "/signin":
				return authorizationController.signIn(request);
			case "/tables":
				if ("GET".equals(request.getHttpMethod())) {
					return tablesController.getTables(request);
				} else if ("POST".equals(request.getHttpMethod())) {
					return tablesController.postTable(request);
				}
				break;
			case "/tables/{tableId}":
				return tablesController.getTableById(request);
			case "/reservations":
				if ("GET".equals(request.getHttpMethod())) {
					return reservationsController.getReservations(request);
				} else if ("POST".equals(request.getHttpMethod())) {
					return reservationsController.postReservation(request);
				}
				break;
			default:
				return defaultResponse();
		}
		return defaultResponse();
	}

	private APIGatewayProxyResponseEvent defaultResponse() {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(404);
		response.setBody("Endpoint not found");
		return response;
	}
}
