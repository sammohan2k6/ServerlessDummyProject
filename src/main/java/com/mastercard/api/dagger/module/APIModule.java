package com.mastercard.api.dagger.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class APIModule {

    @Singleton
    @Provides
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
