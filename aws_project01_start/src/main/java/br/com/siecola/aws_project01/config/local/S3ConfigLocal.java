package br.com.siecola.aws_project01.config.local;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class S3ConfigLocal {

    private static final String BUCKET_NAME = "pcs-invoice";

    private AmazonS3 amazonS3;

    public S3ConfigLocal() {
        this.amazonS3 = getAmazonS3();
        createBucket();
        AmazonSNS snsClient = getAmazongSNS();

        String s3InvoiceEventsTopicArn = createTopic(snsClient);

        AmazonSQS sqsClient = getAmazonSQS();
        createQueueAndSubscribe(snsClient, s3InvoiceEventsTopicArn, sqsClient);

        configureBucket(s3InvoiceEventsTopicArn);
    }


    private static AmazonS3 getAmazonS3() {
        AWSCredentials credentials = new BasicAWSCredentials("test", "test");
        return AmazonS3Client.builder().standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName()))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .enablePathStyleAccess()
                .build();
    }


    private void createBucket() {
        this.amazonS3.createBucket(BUCKET_NAME);
    }

    @Bean
    public AmazonS3 s3Client() {
        return this.amazonS3;
    }

    private static AmazonSNS getAmazongSNS() {
        return AmazonSNSClient.builder().standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

    }

    private AmazonSQS getAmazonSQS() {
        return AmazonSQSClient.builder().standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private String createTopic(AmazonSNS snsClient) {
        CreateTopicRequest createTopicRequest = new CreateTopicRequest("s3-invoice-events");
        return snsClient.createTopic(createTopicRequest).getTopicArn();
    }

    private void createQueueAndSubscribe(AmazonSNS snsClient, String s3InvoiceEventsTopicArn, AmazonSQS sqsClient) {
        String s3InvoiceEventsQueueUrl = sqsClient.createQueue("s3-invoice-events").getQueueUrl();
        Topics.subscribeQueue(snsClient, sqsClient, s3InvoiceEventsTopicArn, s3InvoiceEventsQueueUrl);
    }

    private void configureBucket(String s3InvoiceEventsTopicArn) {
        TopicConfiguration topicConfiguration = new TopicConfiguration();
        topicConfiguration.setTopicARN(s3InvoiceEventsTopicArn);
        topicConfiguration.addEvent(S3Event.ObjectCreatedByPut);

        BucketNotificationConfiguration putObject = new BucketNotificationConfiguration().addConfiguration("putObject", topicConfiguration);
        this.amazonS3.setBucketNotificationConfiguration(BUCKET_NAME, putObject);

    }

}
