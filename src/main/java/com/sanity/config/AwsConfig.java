package com.sanity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

@Configuration
public class AwsConfig {

    @Value("${aws.rekognition.region:us-east-1}")
    private String region;

    @Value("${aws.rekognition.access-key:}")
    private String accessKey;

    @Value("${aws.rekognition.secret-key:}")
    private String secretKey;

    @Bean
    @ConditionalOnProperty(name = "aws.rekognition.access-key", matchIfMissing = false)
    public RekognitionClient rekognitionClient() {
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            System.err.println("⚠️  AWS Rekognition: Credenciales no configuradas. La verificación facial no estará disponible.");
            return null;
        }
        System.out.println("✅ AWS Rekognition: Cliente configurado en región " + region);
        return RekognitionClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
