package com.task10.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.task10.model.Reservation;
import com.task10.model.ReservationResponse;
import com.task10.model.ReservationsResponse;
import com.task10.model.Table;
import com.task10.model.TablesResponse;

public class DynamoDbService {

    private String tableTables;
    private String tableReservations;
    private AmazonDynamoDB amazonDynamoDB;

    public DynamoDbService(String tableTables, String tableReservations) {
        this.tableTables = tableTables;
        this.tableReservations = tableReservations;
        amazonDynamoDB =
                AmazonDynamoDBClientBuilder.standard()
                        .withRegion(Regions.EU_CENTRAL_1)
                        .build();

    }

    public TablesResponse getAllTables() {

        // Create DynamoDBMapperConfig with the retrieved table name
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableTables))
                .build();

        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, mapperConfig);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Table> tablePaginatedScanList = dynamoDBMapper.scan(Table.class,
                scanExpression, mapperConfig);

        // Create DynamoDBMapper with the DynamoDB client and the mapper config

        List<Table> tables = new ArrayList<>(tablePaginatedScanList);
        TablesResponse tablesResponse = new TablesResponse();
        tablesResponse.setTables(tables);
        return tablesResponse;
    }

    public int createTable(Table table) {
        // Create DynamoDBMapperConfig with the retrieved table name
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableTables))
                .build();

        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, mapperConfig);
        dynamoDBMapper.save(table);
        return table.getId();
    }

    public Table getTableById(int id) {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableTables))
                .build();

        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, mapperConfig);
        System.out.println("Table id: " + id);
        Table table = dynamoDBMapper.load(Table.class, id);
        return table;
    }

    private boolean existsTableByNumber(int number) {
        String filterExpression = "#attributeName = :value";
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":value", new AttributeValue().withN(String.valueOf(number)));

        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#attributeName", "number");

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableTables)
                .withFilterExpression(filterExpression)
                .withExpressionAttributeValues(expressionAttributeValues)
                .withExpressionAttributeNames(expressionAttributeNames);

        ScanResult scanResult = amazonDynamoDB.scan(scanRequest);
        System.out.println(scanResult.getItems());

        return scanResult.getCount() > 0;
    }


    public ReservationsResponse getAllReservations() {
        // Create DynamoDBMapperConfig with the retrieved table name
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableReservations))
                .build();

        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, mapperConfig);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Reservation> reservationPaginatedScanList = dynamoDBMapper.scan(Reservation.class,
                scanExpression, mapperConfig);

        // Create DynamoDBMapper with the DynamoDB client and the mapper config

        List<Reservation> reservations = reservationPaginatedScanList.stream().collect(Collectors.toList());
        ReservationsResponse reservationsResponse = new ReservationsResponse();
        reservationsResponse.setReservations(reservations);
        return reservationsResponse;
    }

    public ReservationResponse createReservation(Reservation reservationsRequest) {
        // Create DynamoDBMapperConfig with the retrieved table name
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableReservations))
                .build();

        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, mapperConfig);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Reservation> reservationPaginatedScanList = dynamoDBMapper.scan(Reservation.class,
                scanExpression, mapperConfig);

        System.out.println("Before exists table");
        if (!existsTableByNumber(reservationsRequest.getTableNumber())) {
            throw new RuntimeException("Table not exists");
        }
        List<Reservation> reservations = new ArrayList<>(reservationPaginatedScanList);
        System.out.println(reservations);
        boolean anyMatch = reservations.stream().anyMatch(x ->
                x.getDate().equals(reservationsRequest.getDate())
                        && x.getTableNumber() == reservationsRequest.getTableNumber()
                        && x.getSlotTimeEnd().equals(reservationsRequest.getSlotTimeEnd())
                        && x.getSlotTimeStart().equals(reservationsRequest.getSlotTimeStart()));
        if (anyMatch) {
            throw new RuntimeException("Reserve already exists");
        }

        String reservationId = UUID.randomUUID().toString();
        reservationsRequest.setReservationId(reservationId);
        dynamoDBMapper.save(reservationsRequest);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setStatusCode(200);
        reservationResponse.setReservationId(reservationId);
        return reservationResponse;
    }

}
