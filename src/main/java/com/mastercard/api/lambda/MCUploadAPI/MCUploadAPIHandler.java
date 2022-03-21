package com.mastercard.api.lambda.MCUploadAPI;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class MCUploadAPIHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LogManager.getLogger(com.mastercard.api.lambda.MCUploadAPI.MCUploadAPIHandler.class);

    @Inject
    ReadFile readFile;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        Map<String, String> hps = event.getHeaders();
        byte[] fileBytes = event.getBody().getBytes();
        String contentType = hps.getOrDefault("Content-Type", "");
        String boundary = MCFileUtils.extractBoundary(contentType);
        logger.info("contentType {}", contentType);

        try {
            //Create a MultipartStream to process the form-data
            MultipartStream multipartStream = new MultipartStream(new ByteArrayInputStream(fileBytes), boundary.getBytes(), fileBytes.length, null);
            APIFile APIFile = readFile.read(multipartStream);
            response = readFile.sendToBucket(APIFile);
        } catch (IOException | SdkClientException e) {
            logger.error("Failed to load file", e);
            response.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        return response;
    }
}