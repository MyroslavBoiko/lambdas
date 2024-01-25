package com.task05.dto;

public class EventResponse {
    private int statusCode;
    private Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "EventResponse{" +
                "statusCode=" + statusCode +
                ", event=" + event +
                '}';
    }
}
