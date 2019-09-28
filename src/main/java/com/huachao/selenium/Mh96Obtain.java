package com.huachao.selenium;

import lombok.Data;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 爬取96网站的漫画
 */
public class Mh96Obtain {

    private String mainUrl = "http://www.96mh.com/rexue/doupocangzuozhidazhuzai/";
    private HttpClient httpClient = HttpUtil.getHttpClient();
    private ChromeDriver chromeDriver = ChromeDriverUtil.getChromeDriver();
    private WebDriverWait webDriverWait = ChromeDriverUtil.getWebDriverWait();
    private Pattern pattern = Pattern.compile("\\d+");
    private String hostName;

    {
        Matcher matcher = Pattern.compile("((http://|https://).*?/)").matcher(mainUrl);
        matcher.find();
        hostName = matcher.group(1);
    }

    public static void main(String[] args) {
        while (true){
            try {
                new Mh96Obtain().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        List<Chapter> allChapter = getAllChapter();
        for (Chapter chapter : allChapter) {
            handlerChapter(chapter);
        }
    }

    private void handlerChapter(Chapter chapter) {
        try {
            String index = Files.readString(Path.of("index"));
            if (!(chapter.getIndex() > Integer.parseInt(index))) {
                return;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        loadPageChapter(chapter);
        AtomicInteger atomicInteger = new AtomicInteger();
        String xpath = "//*[@id=\"qTcms_Pic_middle\"]/tbody/tr/td/div/img";
        List<WebElement> until = webDriverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xpath)));
        List<InnerPic> pics = until.stream().map(w -> {
            InnerPic innerPic = new InnerPic();
            String src = w.getAttribute("src");
            innerPic.setUrl(src);
            innerPic.setOrder(atomicInteger.getAndIncrement());
            return innerPic;
        }).collect(Collectors.toList());
        List<CompletableFuture<HttpResponse<byte[]>>> oks =
                pics.stream().map(pic -> {
                    CompletableFuture<HttpResponse<byte[]>> httpResponseCompletableFuture = httpClient.sendAsync(HttpRequest.newBuilder().GET().uri(URI.create(pic.getUrl())).build(), HttpResponse.BodyHandlers.ofByteArray());
                    httpResponseCompletableFuture.thenAccept(httpResponse -> {
                        pic.setBytes(httpResponse.body());
                    });
                    return httpResponseCompletableFuture;
                })
                        .collect(Collectors.toList());
        for (CompletableFuture<HttpResponse<byte[]>> ok : oks) {
            try {
                ok.get(3, TimeUnit.MINUTES);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("图片数据获取完成。。。");
        Mat img = PicUtil.mergePic(pics.stream().map(InnerPic::getBytes).collect(Collectors.toList()), chapter.chapterName);
        Path filePath = Path.of("D:\\dazhuzai");
        Imgcodecs.imwrite(filePath.toString() + "\\0.jpg", img);
        try {
            Files.move(Path.of(filePath.toString(), "0.jpg"), Path.of(filePath.toString(), chapter.getTotalName() + ".jpg"), StandardCopyOption.REPLACE_EXISTING);
            Files.write(Path.of("index"), String.valueOf(chapter.getIndex()).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    private static class InnerPic {
        private Integer order;
        private String url;
        private byte[] bytes;
    }

    private void loadPageChapter(Chapter chapter) {
        chromeDriver.get(chapter.getUrl());
        webDriverWait.until(w -> w.getCurrentUrl().equals(chapter.url));
        chromeDriver.navigate().refresh();
        webDriverWait.until(w -> {
            try {
                chromeDriver.executeScript("window.scrollTo(0,document.body.scrollHeight)");
                return chromeDriver.findElement(By.xpath("//*[@id=\"qTcms_Pic_middle\"]/tbody/tr//div[contains(@style,'height:35px;line-height:35px;font-size:16px')]"));
            } catch (Exception e) {
                return null;
            }
        });

        System.out.println("章节加载完毕。。。");

    }

    private List<Chapter> getAllChapter() {
        chromeDriver.navigate().to(mainUrl);
        webDriverWait.until(w -> {
            if (!w.getCurrentUrl().equals(mainUrl)) {
                w.get(mainUrl);
                return false;
            }
            chromeDriver.navigate().refresh();
            return true;
        });


        checkMainPageload();

        String xpath = "//*[@id=\"mh-chapter-list-ol-0\"]/li/a";
        List<WebElement> elements = webDriverWait.until(w -> {
            WebElement elements1 = w.findElement(By.xpath("//*[@id=\"zhankai\"]"));
            boolean flag = elements1.getText().trim().contains("收起章节");
            if (flag) return w.findElements(By.xpath(xpath));
            return null;
        });
        return elements.stream().map(e -> {
            try {
                Chapter chapter = new Chapter();
                String text = e.getText();
                chapter.setTotalName(text);
                chapter.setChapterName(text.substring(text.indexOf(" ") + 1));
                Matcher matcher = pattern.matcher(text);
                if (!matcher.find()) {
                    System.out.println(text + ":");
                }
                chapter.setIndex(Integer.parseInt(matcher.group()));
                chapter.setUrl(e.getAttribute("href"));

                return chapter;
            } catch (Exception ex) {
                return null;
            }
        }).filter(c -> c != null && c.getIndex() != null)
                .sorted(Comparator.comparing(Chapter::getIndex))
                .collect(Collectors.toList());


    }

    private void click(WebElement webElement) {
        ChromeDriverUtil.click(webElement);
    }

    private void checkMainPageload() {
        String xpath = "//div[@class='cy_zhangjie_top']/p[1]/a";
        webDriverWait.until(w -> {
            String text = w.findElement(By.xpath(xpath)).getText();
            Matcher matcher = pattern.matcher(text);
            return matcher.find() && Integer.parseInt(matcher.group()) > 0;
        });
        WebElement element1 = chromeDriver.findElement(By.xpath("//*[@id=\"zhankai\"]"));
        click(element1);
    }

    @Data
    private static class Chapter {
        private String totalName;
        private String url;
        private String chapterName;
        private Integer index;
    }

}
