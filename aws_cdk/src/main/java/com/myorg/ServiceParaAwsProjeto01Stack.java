package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class ServiceParaAwsProjeto01Stack extends Stack {
    public ServiceParaAwsProjeto01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public ServiceParaAwsProjeto01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue("rds-endpoint")
                + ":3306/aws_project01?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "admin");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));

        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService
                .Builder
                .create(this, "ALB01")
                .serviceName("service-01") // nome do serviço
                .cluster(cluster) // O cluster a ser usado
                .cpu(512) // Tamanho da CPU
                .desiredCount(2) // Quantidade iniciais de instâncias desejadas
                .listenerPort(8080) // Porta de escuta
                .memoryLimitMiB(1024) // Definição de memória
                .taskImageOptions( // Definição da task (contendo a imagem da aplicação rodando em um container desponibilizando os logs)
                        ApplicationLoadBalancedTaskImageOptions.builder() // Especificação do container e da image
                                .containerName("aws_projeto01_container")
                                .image(ContainerImage.fromRegistry("zemiro/curso_aws_project01:1.2.2-SNAPSHOT"))
                                .containerPort(8080)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder() // Expecificando os Logs
                                                .logGroup(LogGroup.Builder.create(this, "Service01LogGroup")
                                                        .logGroupName("Service01")
                                                        .removalPolicy(RemovalPolicy.DESTROY)
                                                        .build())
                                                .streamPrefix("Service01")
                                        .build()))
                                .environment(envVariables)
                                .build())
                .publicLoadBalancer(true) // Tornando o LoadBalancer público. Teremos um DNS definido aqui que nos permitirá acesssar a aplicação
                .build();

        // Configuração do TargetGroup. Responsável em verificar se a aplicação está UP ou não.
        service01.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/actuator/health")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

        // Configuração da AutoScale: Monitora as requisições da aplicação
        ScalableTaskCount scalableTaskCount = service01.getService().autoScaleTaskCount(EnableScalingProps.builder()
                        .minCapacity(2) // número mínimo de instancia da aplicação
                        .maxCapacity(4) // número máximo de instancia da aplicação
                .build());

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
                        .targetUtilizationPercent(50) // Percentagem de consumo da aplicação usado como parametro para destruir ou construir uma instancia da aplicação
                        .scaleInCooldown(Duration.seconds(60)) // tempo necessário para construir nova instancia da aplicação
                        .scaleOutCooldown(Duration.seconds(60)) // tempo necessário para destruir uma instancia da aplicação
                .build());
    }
}
