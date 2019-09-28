package com.huachao.selenium;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.WebDriverWait;

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
                    webDriverWait=new WebDriverWait(getChromeDriver(),20);
                }
            }
        }
        return webDriverWait;
    }

    public static void click(WebElement webElement){
        webDriverWait.until(w->{
            try {
                webElement.click();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    private static ChromeDriver newInstant(){
        ChromeOptions chromeOptions=new ChromeOptions();
        chromeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS,true);
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
        return new ChromeDriver(chromeOptions);
    }

}
