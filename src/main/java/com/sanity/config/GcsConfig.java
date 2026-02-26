package com.sanity.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class GcsConfig {

    @Value("${gcs.credentials-path}")
    private Resource credentialsResource;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Bean
    public Storage googleCloudStorage() throws IOException {
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.fromStream(credentialsResource.getInputStream());
        } catch (Exception e) {
            System.err.println("⚠️  No se encontró gcs-credentials.json. Usando credenciales por defecto.");
            credentials = GoogleCredentials.getApplicationDefault();
        }

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    @Bean
    public String gcsBucketName() {
        return bucketName;
    }
}
