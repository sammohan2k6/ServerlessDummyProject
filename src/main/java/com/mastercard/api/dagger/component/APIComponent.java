package com.mastercard.api.dagger.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.api.lambda.MCUploadAPI.MCUploadAPIHandler;
import com.mastercard.api.lambda.MCUploadAPI.MCUploadAPIHandler;
import com.mastercard.api.lambda.apiValidation.APIValidationHandler;
import com.mastercard.api.Main;
import com.mastercard.api.dagger.module.APIModule;
import dagger.Component;

import javax.inject.Singleton;


@Singleton
@Component(modules = {
        APIModule.class, SqsModule.class,
        SnsModule.class, EnvironmentVariableModule.class})
public interface APIComponent {

    void inject(MCUploadAPIHandler mcUploadAPIHandler);

    void inject(APIValidationHandler apiValidationHandler);

    ObjectMapper objectMapper();

    void inject(Main requestHandler);

}
