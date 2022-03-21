package com.mastercard.api.lambda.MCUploadAPI;

public class APIFile {

    private byte[] file;
    private String fileObjKeyName;
    private String fileVersionId;

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getFileObjKeyName() {
        return fileObjKeyName;
    }

    public void setFileObjKeyName(String fileObjKeyName) {
        this.fileObjKeyName = fileObjKeyName;
    }

    public String getFileVersionId() {
        return fileVersionId;
    }

    public void setFileVersionId(String fileVersionId) {
        this.fileVersionId = fileVersionId;
    }
}

