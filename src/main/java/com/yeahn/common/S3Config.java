package com.yeahn.common;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.SdkClientException;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cos.spring.framework.EnableCOS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileUrlResource;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.IOException;

@Configuration
@EnableCOS
@ComponentScan
public class S3Config {

    @Value("${cos.location}")
    private String location;

    @Value("${cos.api-key}")
    private String apiKey;

    @Value("${cos.service-instance-id}")
    private String serviceId;

    @Value("${cos.endpoint}")
    private String endpoint;

    @Bean
    public AmazonS3 amazonS3Client() {
        try {
            AWSCredentials credentials = new BasicIBMOAuthCredentials(apiKey, serviceId);
            ClientConfiguration clientConfig = new ClientConfiguration()
                    .withRequestTimeout(5000)
                    .withTcpKeepAlive(true);

            return AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, location))
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(clientConfig)
                    .build();

        } catch (SdkClientException sdke) {
            System.out.printf("SDK Error: %s\n", sdke.getMessage());
        } catch (Exception e) {
            System.out.printf("Error: %s\n", e.getMessage());
        }

        return null;
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver(
            @Value("${spring.servlet.multipart.location}") String tmpFolder,
            @Value("${spring.servlet.multipart.max-file-size}") DataSize maxFileSize,
            @Value("${spring.servlet.multipart.max-request-size}") DataSize maxRequestSize) throws IOException {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSizePerFile(maxRequestSize.toBytes());
        commonsMultipartResolver.setMaxUploadSize(maxFileSize.toBytes());
        commonsMultipartResolver.setUploadTempDir(new FileUrlResource(tmpFolder));
        return commonsMultipartResolver;
    }
}