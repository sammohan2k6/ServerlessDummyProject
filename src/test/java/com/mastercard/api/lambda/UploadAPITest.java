package com.mastercard.api.lambda;

import com.mastercard.api.lambda.MCUploadAPI.ReadFile;
import org.apache.commons.fileupload.MultipartStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;

public class UploadAPITest {

    @Test
    public void upload() throws IOException {
        String boundary = "0OLxSuZh5BArS_8U1Q7mzMZs-yiRDZ07";
        Path path = Paths.get("src/test/resources/testdata/test.xml");
        byte[] fileBytes = readAllBytes(path);

        ReadFile readFile = new ReadFile();
        ByteArrayInputStream content = new ByteArrayInputStream(fileBytes);
        //TODO: Figure out how to mock the boundary
        MultipartStream multipartStream = new MultipartStream(content, boundary.getBytes(), fileBytes.length, null);
        readFile.read(multipartStream);
    }

    @Test
    public void extractInfoForQueueTest() {
        String headers = "Content-Disposition: form-data; name=AWS_93104_93492_907876_08172021163030.xml";
        String[] fileNameSplit = headers.split("name=");
        Assert.assertEquals("AWS_93104_93492_907876_08172021163030.xml", fileNameSplit[1]);
        String[] parts = APIFileUtils.extractInfoForQueue("api-files/" + fileNameSplit[1]);
        Assert.assertEquals("93104", parts[1]);
        Assert.assertEquals("93492", parts[2]);
    }
}