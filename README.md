# AWS CDK Fargate Microservices Infrastructure

A robust microservices infrastructure deployed on AWS using CDK and Fargate. This project implements a scalable architecture with multiple services, event-driven communication, and various AWS services integration.

## Architecture Overview

This project implements a microservices architecture using AWS CDK to provision and manage the following components:

### Infrastructure Components

- **VPC Stack**
  - 3 Availability Zones configuration
  - NAT Gateways for private subnet access
  - Public and private subnets

- **ECS Fargate Cluster**
  - Managed container orchestration
  - Application Load Balancer integration
  - Service auto-scaling capabilities

- **RDS Database**
  - MariaDB instance
  - Secure VPC deployment
  - Automated password management

- **SNS Topic for Product Events**
  - Event-driven architecture backbone
  - Asynchronous communication between services
  - Topic ARN available via environment variables

- **DynamoDB Table**
  - Product events storage
  - Integration with Service02
  - Scalable NoSQL database

- **S3 Bucket for Invoices**
  - Secure document storage
  - Integration with Service01
  - Read/Write permissions managed through IAM

- **SQS Queue for Invoice Processing**
  - Asynchronous invoice processing
  - Integration with Service01
  - Managed message delivery

### Microservices

#### Service01
- Spring Boot application
- Integrates with RDS for data persistence
- Publishes events to SNS Topic
- Processes invoices using S3 and SQS
- Auto-scaling configuration (2-4 instances)
- Health check endpoint at `/actuator/health`

#### Service02
- Spring Boot application
- Subscribes to SNS Topic for product events
- Stores events in DynamoDB
- Event processing and persistence

## Prerequisites

- AWS CLI installed and configured
- AWS CDK CLI installed
- Java 11 or later
- Maven
- Docker (for local testing)

## AWS Credentials Setup

1. Configure AWS CLI:
```bash
aws configure
```

2. Bootstrap CDK in your AWS account:
```bash
cdk bootstrap
```

## Building and Deployment

### Build Steps

1. Build the Java applications:
```bash
# For Service01
cd aws_project01_start
./mvnw clean package

# For Service02
cd aws_project02_start
./mvnw clean package
```

2. Build the CDK project:
```bash
cd curso_aws_cdk
mvn clean package
```

### Deployment Order

The stacks must be deployed in the following order due to dependencies:

1. VPC Stack
2. Cluster Stack
3. RDS Stack
4. SNS Stack
5. Invoice App Stack
6. Service01 Stack
7. DynamoDB Stack
8. Service02 Stack

To deploy all stacks:
```bash
cdk deploy --all
```

## Configuration

### Environment Variables

Service01 Environment Variables:
```
SPRING_DATASOURCE_URL=jdbc:mariadb://<rds-endpoint>:3306/aws_project01
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=<rds-password>
AWS_REGION=us-east-1
AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN=<sns-topic-arn>
AWS_SQS_QUEUE_INVOICE_EVENTS_NAME=<queue-name>
AWS_S3_BUCKET_INVOICE_NAME=<bucket-name>
```

Service02 Environment Variables:
```
AWS_REGION=us-east-1
AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN=<sns-topic-arn>
```

## Auto-scaling Configuration

### Service01 Auto-scaling

- Minimum capacity: 2 instances
- Maximum capacity: 4 instances
- Target CPU utilization: 50%
- Scale-in cooldown: 60 seconds
- Scale-out cooldown: 60 seconds

## Health Checks and Monitoring

### Health Check Endpoints

- Service01: `/actuator/health` (Port 8080)
- Health check configured with ALB target group
- Healthy response code: 200

### Logging

- CloudWatch Logs integration
- Log groups:
  - Service01: /aws/service01
  - Service02: /aws/service02

### Monitoring

- CloudWatch metrics available for:
  - ECS Service metrics
  - ALB metrics
  - RDS metrics
  - DynamoDB metrics

## Security Considerations

- Services run in private subnets
- RDS instance in private subnet
- IAM roles and policies automatically configured
- Security groups managing access between services

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details
