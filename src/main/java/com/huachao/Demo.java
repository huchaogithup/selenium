package com.huachao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Demo {

    public static void main(String[] args) {
        System.out.println(Pattern.compile("/.*?(?!\\.jpg)").asPredicate().test("/%E7%AC%AC11%E8%AF%9D%20%E9%99%A2%E6%96%97%E5%A4%A7%E4%BC%9A.jpg"));
        Pattern compile = Pattern.compile("/.*?(?!\\.jpg)");
        Matcher matcher = compile.matcher("/a.jpg");
        matcher.find();
        System.out.println(matcher.group());
    }
}
