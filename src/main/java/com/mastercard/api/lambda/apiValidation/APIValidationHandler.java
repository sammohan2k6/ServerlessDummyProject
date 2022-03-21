package com.mastercard.api.lambda.apiValidation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.internal.SSEResultBase;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mastercard.api.dagger.component.APIComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class APIValidationHandler implements RequestHandler<S3Event, String> {
    static final Logger logger = LogManager.getLogger(APIValidationHandler.class);
    private final APIComponent apiConnectorComponent;

    public APIValidationHandler() {
        apiConnectorComponent = DaggerApiComponent.builder().build();
        apiConnectorComponent.inject(this);
    }

    NotificationService notificationService;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        logger.info("Calling APIValidationHandler Handler Request");
        logger.info(" EVENT: " + gson.toJson(s3Event));
        S3EventNotificationRecord record = s3Event.getRecords().get(0);
        String srcBucket = record.getS3().getBucket().getName();
        // Object key may have spaces or unicode non-ASCII characters.
        String srcKey = record.getS3().getObject().getUrlDecodedKey();
        try {
            // Download the File from S3 into a stream
            AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
            //Validating XML against the xsd
            ClassLoader classLoader = getClass().getClassLoader();
            File schemaFiles = new File(classLoader.getResource("xyz.xsd").getFile());
            //Copying the incoming file to a tmp directory for now.
            String fileName = "/tmp/" + srcKey;
            File temp = new File(fileName);
            s3Client.getObject(new GetObjectRequest(
                    srcBucket, srcKey), temp);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFiles);
            validateAndCopy(srcBucket, srcKey, s3Client, fileName, temp, schema);
            //    SNSTopics.listSNSTopics(snsClient);
            return "Ok";
        } catch (Exception e) {
            notificationService.publishTechErrorsToSNS("Validation Error ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateAndCopy(String srcBucket, String srcKey, AmazonS3 s3Client, String fileName, File temp, Schema schema) throws SAXException, IOException {
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new FileReader(fileName)));

        //Storing the validated file in a different folder
        String dstBucket = srcBucket;
        String dstKey = srcKey.substring(0, srcKey.indexOf('/')) + "-validated/" + srcKey.substring(srcKey.indexOf('/') + 1, srcKey.length());

        // Make a copy of the object and use server-side encryption while storing.
        CopyObjectRequest request = new CopyObjectRequest(srcBucket,
                srcKey,
                dstBucket,
                dstKey);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        request.setNewObjectMetadata(objectMetadata);

        // Perform the copy operation and display the copy's encryption status.
        CopyObjectResult response = s3Client.copyObject(request);
        System.out.println("Object \"" + dstKey + "\" uploaded with SSE.");
        printEncryptionStatus(response);
        //Deleting temp file on exit
        temp.deleteOnExit();
        logger.info(" Successfully Validated " + srcBucket + "/"
                + srcKey + " and uploaded to " + dstBucket + "/" + dstKey);
    }

    private static void printEncryptionStatus(SSEResultBase response) {
        String encryptionStatus = response.getSSEAlgorithm();
        if (encryptionStatus==null) {
            encryptionStatus = "Not encrypted with SSE";
        }
        System.out.println("Object encryption status is: " + encryptionStatus);
    }
}
