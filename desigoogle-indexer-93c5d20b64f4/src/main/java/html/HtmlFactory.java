package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

public class HtmlFactory {
    public static String getHead(Document document) {
        Element head = document.head();
        String str = head.toString();
        str = str.replaceAll("</", " </");
        String parsedHead = Jsoup.clean(str, Whitelist.relaxed());
        parsedHead = Jsoup.clean(parsedHead, Whitelist.none());
        return parsedHead;
    }

    public static String getBody(Document document) {
        String str = document.toString();
        str = str.replaceAll("</", " </");
        str = str.replaceAll(">", "> ");
        String parsedBody = Jsoup.clean(str, Whitelist.relaxed());
        parsedBody = Jsoup.clean(parsedBody, Whitelist.none());
        return parsedBody;
    }

    public static String getBody(String str) {
        str = str.replaceAll("</", " </");
        str = str.replaceAll(">", "> ");
        String parsedBody = Jsoup.clean(str, Whitelist.relaxed());
        parsedBody = Jsoup.clean(parsedBody, Whitelist.none());
        return parsedBody;
    }

    public static String getWord(String wordWithPunctuations) {
        String word = wordWithPunctuations.replaceAll("\\W", " ");
        word = word.replaceAll("\\b\\d+\\b", "");
        word = word.toLowerCase();
        word = word.replaceAll("\n", "");
        word = word.replaceAll("\r", "");
        return word;
    }

}
