package com.huachao.selenium;

import lombok.Data;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.http.HttpClient;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 爬取96网站的漫画
 */
public class Mh96Obtain {

    private String mainUrl="http://www.96mh.com/rexue/doupocangzuozhidazhuzai/";
    private HttpClient httpClient=HttpUtil.getHttpClient();
    private ChromeDriver chromeDriver=ChromeDriverUtil.getChromeDriver();
    private WebDriverWait webDriverWait=ChromeDriverUtil.getWebDriverWait();
    private Pattern pattern=Pattern.compile("\\d+");

    private List<Chapter> getAllChapter(){
        chromeDriver.get(mainUrl);
        webDriverWait.until(w-> w.getCurrentUrl().equals(mainUrl));

    }

    @Data
    private static class Chapter{
        private String url;
        private String chapterName;
        private Integer index;
    }

}
