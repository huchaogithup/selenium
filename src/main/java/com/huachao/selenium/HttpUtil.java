package com.huachao.selenium;

import java.net.http.HttpClient;

public class HttpUtil {

    public static HttpClient getHttpClient(){
        return HttpClient.newBuilder().build();
    }
}
