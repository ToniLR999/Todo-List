package com.tonilr.ToDoList.lambda;

import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.tonilr.ToDoList.ToDoListApplication;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.exceptions.ContainerInitializationException;

import java.io.InputStream;
import java.io.OutputStream;

public class LambdaHandler implements RequestStreamHandler {
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = new SpringBootProxyHandlerBuilder<AwsProxyRequest>()
                    .defaultProxy()
                    .asyncInit()
                    .springBootApplication(ToDoListApplication.class)
                    .buildAndInitialize();
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Error inicializando el contenedor de Lambda", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws java.io.IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
} 