package com.huachao;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.Utils;

import java.io.File;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticWeb extends AbstractVerticle {
    private String dir = "D:\\dazhuzai\\";
    Pattern pattern = Pattern.compile("\\d+");
    String urlTemplate = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Title</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <img src=\"{0}\"/>\n" +
            "    <a href=\"{1}\">下一章</a>\n" +
            "</body>\n" +
            "</html>";


    @Override
    public void start() throws Exception {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.routeWithRegex("/").handler(context -> {
            context.vertx().fileSystem().readDir(dir, fileResult -> {
                if (fileResult.failed()) {
                    context.fail(fileResult.cause());
                    return;
                }

                String accept = context.request().headers().get("accept");
                if (accept == null) {
                    accept = "text/plain";
                }

                if (accept.contains("html")) {
                    String normalizedDir = context.normalisedPath();
                    if (!normalizedDir.endsWith("/")) {
                        normalizedDir += "/";
                    }

                    String file;
                    StringBuilder files = new StringBuilder("<ul id=\"files\">");

                    List<String> list = fileResult.result();
                    list.sort((s1, s2) -> {
                        int i = paseInt(s1);
                        int i1 = paseInt(s2);
                        return Integer.compare(i, i1);

                    });

                    for (String s : list) {
                        file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
                        file = file.substring(0, file.lastIndexOf("."));
                        files.append("<li><a href=\"");
                        files.append(normalizedDir);
                        files.append(file);
                        files.append("\" title=\"");
                        files.append(file);
                        files.append("\">");
                        files.append(file);
                        files.append("</a></li>");
                    }

                    files.append("</ul>");

                    // link to parent dir
                    int slashPos = 0;
                    for (int i = normalizedDir.length() - 2; i > 0; i--) {
                        if (normalizedDir.charAt(i) == '/') {
                            slashPos = i;
                            break;
                        }
                    }

                    String parent = "<a href=\"" + normalizedDir.substring(0, slashPos + 1) + "\">..</a>";

                    context.request().response().putHeader("content-type", "text/html");
                    context.response().end(
                            Utils.readFileToString(vertx, StaticHandler.DEFAULT_DIRECTORY_TEMPLATE).replace("{directory}", normalizedDir)
                                    .replace("{parent}", parent)
                                    .replace("{files}", files.toString()));


                }
                ;
            });


        });

        router.route("/*").handler(context -> {
            String s = URIDecoder.decodeURIComponent(context.normalisedPath());
            if (s.endsWith(".jpg")) {
                context.next();
                return;
            }
            int index = paseInt(s);
            final String index1 = String.valueOf(++index);
            String currPage = s + ".jpg";
            context.vertx().fileSystem().readDir(dir, files -> {
                Optional<String> first = files.result().stream().filter(s1 -> s1.contains(index1)).findFirst();
                if (first.isPresent()) {
                    String substring = first.get().substring(first.get().lastIndexOf(File.separatorChar) + 1);
                    substring = substring.substring(0,substring.indexOf("."));
                    context.response().end(MessageFormat.format(urlTemplate,currPage,substring));
                }
            });
        });
        router.route("/*").handler(StaticHandler.create()
                .setDirectoryListing(true)
                .setAllowRootFileSystemAccess(true)
                .setWebRoot(dir));
        httpServer.requestHandler(router);
        httpServer.listen(80);
    }

    public static void main(String[] args) {
        //System.out.println(Pattern.compile("/.*?(?!\\.jpg)").asPredicate().test("abc.jpg"));
        //System.exit(0);
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(StaticWeb.class.getName(), new DeploymentOptions().setInstances(3));
    }


    private int paseInt(String s1) {
        Matcher matcher = pattern.matcher(s1);
        matcher.find();
        return Integer.parseInt(matcher.group());
    }
}