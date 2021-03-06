package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class CursoAwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        // VPC
        VpcStack vpcStack = new VpcStack(app, "Vpc");

        // CLUSTER
        ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        // RDS
        RdsStack rdsStack = new RdsStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        SnsStack snsStack = new SnsStack(app, "Sns");
        // TASK - Service
        ServiceParaAwsProjeto01Stack serviceParaAwsProjeto01Stack = new ServiceParaAwsProjeto01Stack(app, "Service01",
                clusterStack.getCluster(), snsStack.getProductEventsTopic());
        serviceParaAwsProjeto01Stack.addDependency(clusterStack);
        serviceParaAwsProjeto01Stack.addDependency(rdsStack);
        serviceParaAwsProjeto01Stack.addDependency(snsStack);

        app.synth();
    }
}

