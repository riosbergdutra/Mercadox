package user.api.usuario.usuario.infra;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {

    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create("http://localstack-main:4566")) // Nome do serviço do LocalStack no Docker Compose
                .region(Region.of(awsRegion))
                .build();
    }
}
