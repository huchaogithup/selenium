package com.huachao.selenium;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    public static HttpClient getHttpClient(){
        return HttpClient.newBuilder()
                .connectTimeout(Duration.of(3, ChronoUnit.SECONDS)).build();
    }
}
