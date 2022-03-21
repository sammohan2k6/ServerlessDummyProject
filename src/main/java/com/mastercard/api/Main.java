package com.mastercard.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.api.dagger.component.APIComponent;

public class Main {

    private final APIComponent apiComponent;

    private Main() {

        apiComponent = DaggerApiComponent.builder().build();
        apiComponent.inject(this);
    }

    public static void main(String[] args) throws JsonProcessingException {
        Main main = new Main();
        main.testProcessService();
    }

    public void testProcessService() throws JsonProcessingException {
        String request = " ";
        ObjectMapper objectMapper = new ObjectMapper();
        Transmission transmissionRequest = objectMapper.readValue(request, Transmission.class);

    }


}