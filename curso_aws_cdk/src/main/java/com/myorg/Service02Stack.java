package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDrivers;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueEncryption;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class Service02Stack extends Stack {
    public Service02Stack(final Construct scope, final String id, Cluster cluster, SnsTopic productEventTopic, Table productEventsDynamoDBTable) {
        this(scope, id, null, cluster, productEventTopic, productEventsDynamoDBTable);
    }

    public Service02Stack(final Construct scope, final String id, final StackProps props, Cluster cluster, SnsTopic productEventTopic, Table productEventsDynamoDBTable) {
        super(scope, id, props);

        // # SQS
        Queue productEventDlq = Queue.Builder.create(this, "ProductEventsDlq")
                .queueName("product-events-dlq")
                .enforceSsl(false)
                .encryption(QueueEncryption.UNENCRYPTED)
                .build();

        DeadLetterQueue deadLetterQueue = DeadLetterQueue.builder()
                .queue(productEventDlq)
                .maxReceiveCount(3)
                .build();

        Queue productEventQueue = Queue.Builder.create(this, "ProductEvents")
                .queueName("product-events")
                .enforceSsl(false)
                .encryption(QueueEncryption.UNENCRYPTED)
                .deadLetterQueue(deadLetterQueue)
                .build();

        // # Subscribe SQS to SNS Topic
        SqsSubscription sqsSubscription = SqsSubscription.Builder.create(productEventQueue)
                .build();
        productEventTopic.getTopic().addSubscription(sqsSubscription);

        //Environment Variables
        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("AWS_REGION", "us-east-1");
        envVariables.put("AWS_SQS_QUEUE_PRODUCT_EVENTS_NAME", productEventQueue.getQueueName());

        ApplicationLoadBalancedFargateService service02 =
                ApplicationLoadBalancedFargateService.Builder.create(this, "ALB02")
                        .serviceName("service-02")
                        .cluster(cluster)
                        .cpu(512)
                        .desiredCount(2)
                        .listenerPort(9090)
                        .memoryLimitMiB(1024)
//                        .assignPublicIp(true) //case without NAT Gateway
                        .taskImageOptions(
                                ApplicationLoadBalancedTaskImageOptions.builder()
                                        .containerName("aws-cdk-ecs-sample")
                                        .image(ContainerImage.fromRegistry("burbes/curso_aws_project02:1.5.0"))
                                        .containerPort(9090)
                                        .logDriver(LogDrivers.awsLogs(
                                                        AwsLogDriverProps.builder()
                                                                .logGroup(LogGroup.Builder.create(this, "Service02LogGroup")
                                                                        .logGroupName("Service02")
                                                                        .removalPolicy(RemovalPolicy.DESTROY)
                                                                        .build())
                                                                .streamPrefix("Service02")
                                                                .build()
                                                )
                                        )
                                        .environment(envVariables)
                                        .build()
                        )
                        .publicLoadBalancer(true)
                        .build();

        // Health Check
        service02.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                        .path("/actuator/health")
                        .port("9090")
                        .healthyHttpCodes("200")
                        .build());

        // Auto Scaling
        ScalableTaskCount scalableTaskCount = service02.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());
        scalableTaskCount.scaleOnCpuUtilization("Service02AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

        // # Grant Access to SQS
        productEventQueue.grantConsumeMessages(service02.getTaskDefinition().getTaskRole());

        // # Grant Access to DynamnoDB
        productEventsDynamoDBTable.grantReadWriteData(service02.getTaskDefinition().getTaskRole());

    }
}
