package com.ferragem.avila.pdv.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class AwsConfig {

	@Value("${aws.region}")
	private String awsRegion;

	@Value("${aws.access.key:}")
	private String accessKeyId;

	@Value("${aws.secret.key:}")
	private String secretAccessKey;

	@Value("${aws.endpoint}")
	private String awsEndpoint;

	@Bean
	S3Client createS3Instance() {
		S3ClientBuilder s3ClientBuilder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
				.region(Region.of(awsRegion))
				.endpointOverride(URI.create(awsEndpoint))
				.forcePathStyle(true);

		return s3ClientBuilder.build();
	}
}
