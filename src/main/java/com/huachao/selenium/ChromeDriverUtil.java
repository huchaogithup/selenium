package com.huachao.selenium;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChromeDriverUtil {
    private volatile static ChromeDriver chromeDriver;
    private volatile static WebDriverWait webDriverWait;

    private ChromeDriverUtil(){}

    public static ChromeDriver getChromeDriver(){
        if(chromeDriver==null){
            synchronized (ChromeDriverUtil.class){
                if(chromeDriver==null){
                    chromeDriver=newInstant();
                }
            }
        }
        return chromeDriver;
    }

    public static WebDriverWait getWebDriverWait(){
        if(webDriverWait==null){
            synchronized (ChromeDriverUtil.class){
                if(webDriverWait==null){
                    webDriverWait=new WebDriverWait(getChromeDriver(),50);
                }
            }
        }
        return webDriverWait;
    }
    public static void main(String[] args) {

    }

    private static ChromeDriver newInstant(){
        ChromeOptions chromeOptions=new ChromeOptions();
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
        return new ChromeDriver(chromeOptions);
    }

}
