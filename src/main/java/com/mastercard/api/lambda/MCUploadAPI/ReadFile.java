package com.mastercard.api.lambda.MCUploadAPI;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ReadFile {

    private static final Logger logger = LogManager.getLogger(ReadFile.class);
    private static final String CLIENT_REGION = new S3BucketModule().bucketRegion();
    private static final String BUCKET_NAME = new S3BucketModule().bucketName();

    public APIFile read(MultipartStream multipartStream) throws IOException {
        APIFile APIFile = new APIFile();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //Find first boundary in the MultipartStream
        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {
            String headers = multipartStream.readHeaders();
            logger.info("headers {}", headers);
            if (headers.contains("name=")) {
                //Log header for debugging
                String[] fileNameSplit = headers.split("name=\"");
                String fileObjKeyName = "api-files/" + fileNameSplit[1].substring(0, fileNameSplit[1].indexOf("\""));
                APIFile.setFileObjKeyName(fileObjKeyName);
            } else if (headers.contains("filename=")) {
                String[] fileNameSplit = headers.split("filename=\"");
                String fileObjKeyName = "api-files/" + fileNameSplit[1].substring(0, fileNameSplit[1].indexOf("\""));
                APIFile.setFileObjKeyName(fileObjKeyName);
            }
            multipartStream.readBodyData(out);
            nextPart = multipartStream.readBoundary();
        }

        //Prepare an InputStream from the ByteArrayOutputStream
        APIFile.setFile(out.toByteArray());
        return APIFile;
    }

    public APIGatewayProxyResponseEvent sendToBucket(APIFile APIFile) throws SdkClientException {

        byte[] fileBytes = APIFile.getFile();
        String fileObjKeyName = APIFile.getFileObjKeyName();
        logger.info("fileObjKeyName {}", fileObjKeyName);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        //Create our S3Client Object
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(CLIENT_REGION)
                .build();

        //Configure the file metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileBytes.length);
        metadata.setContentType(MediaType.APPLICATION_XML);
        metadata.setCacheControl("public, max-age=31536000");

        //Put file into S3
        InputStream fis = new ByteArrayInputStream(fileBytes);
        PutObjectResult putObjectResult = s3Client.putObject(BUCKET_NAME, fileObjKeyName, fis, metadata);
        logger.info("Placed object in S3 {}", putObjectResult);

        //Provide a response
        response.setStatusCode(Response.Status.OK.getStatusCode());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("Status", "File stored in S3");
        String responseBodyString = new JSONObject(responseBody).toString();
        response.setBody(responseBodyString);
        return response;
    }
}
