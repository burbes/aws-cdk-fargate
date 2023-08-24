package com.myorg;

import software.amazon.awscdk.App;

public class CursoAwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

//        new CursoAwsCdkStack(app, "CursoAwsCdkStack", StackProps.builder()
//                .build());

        VpcStack vpcStack = new VpcStack(app, "VpcStack");

        ClusterStack clusterStack = new ClusterStack(app, "ClusterStack", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        RdsStack rdsStack = new RdsStack(app, "RdsStack", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);
//        rdsStack.addDependency(clusterStack);

        SnsStack snsStack = new SnsStack(app, "SnsStack");

        Service01Stack service01Stack = new Service01Stack(app, "Service01Stack", clusterStack.getCluster(), snsStack.getProductEventTopic());
        service01Stack.addDependency(clusterStack);
        service01Stack.addDependency(rdsStack);
        service01Stack.addDependency(snsStack);

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "DynamoDBStack");
        dynamoDBStack.addDependency(vpcStack);

        Service02Stack service02Stack = new Service02Stack(app, "Service02Stack", clusterStack.getCluster(), snsStack.getProductEventTopic(), dynamoDBStack.getProductEventsDynamoDBTable());
        service02Stack.addDependency(clusterStack);
        service02Stack.addDependency(snsStack);
        service02Stack.addDependency(dynamoDBStack);

        app.synth();
    }
}
