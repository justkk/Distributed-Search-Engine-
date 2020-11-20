package test;

import html.HtmlFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import java.util.StringTokenizer;

public class TestJsoup {
    public static void main(String[] args) {
       String html = "<html><head><title>hi</title>jump</head>you<body>hello<a>this</a>is<p>university</p><b>of</b>pennsylvania";
       StringTokenizer stringTokenizer = new StringTokenizer(HtmlFactory.getBody(html));
        while (stringTokenizer.hasMoreTokens()) {
            System.out.println(stringTokenizer.nextToken()+"|");
        }
       System.out.println(HtmlFactory.getBody(html));
    }
}
