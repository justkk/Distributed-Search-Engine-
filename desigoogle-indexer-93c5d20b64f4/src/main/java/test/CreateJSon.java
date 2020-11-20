package test;

import com.google.gson.Gson;

import java.io.*;
import java.util.StringTokenizer;

import Document.DocObject;
import edu.stanford.nlp.ling.CoreAnnotations;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

public class CreateJSon {
    public static void main(String[] args) {
//        try {
//            File file = new File("./inp/html1");
//            FileInputStream fis = new FileInputStream(file);
//            byte[] data = new byte[(int) file.length()];
//            fis.read(data);
//            fis.close();
//
//            String str;
//            str = new String(data, "UTF-8");
//            DocObject docObject = new DocObject("789", str, str.length(), "####");
//            Gson gson = new Gson();
//            String content = gson.toJson(docObject);
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter("./inp/file2"));
//            writer.write(content);
//
//            writer.close();



//
//            file = new File("./inp/file3");
//            fis = new FileInputStream(file);
//            data = new byte[(int) file.length()];
//            fis.read(data);
//            fis.close();
//
//            str = new String(data, "UTF-8");
//
//            docObject = gson.fromJson(str, DocObject.class);
//
////            System.out.println(docObject.getContent());
//
//
//            String docContent = docObject.getContent();
//
//            String docContent1 = Parser.unescapeEntities(docContent, false);
//            Document document = Jsoup.parse(docContent1);
//
//            String parsedDoc = Jsoup.clean(docContent1, Whitelist.relaxed());
//
////            System.out.println(parsedDoc);
//
//            Element head = document.head();
//            System.out.println(head);
//
//
//            // get the head:
//            String parsedHead = Jsoup.clean(head.toString(), Whitelist.relaxed());
//
//            String parsedBody = Jsoup.clean(document.body().toString(), Whitelist.relaxed());
//
//            parsedBody = Parser.unescapeEntities(Jsoup.clean(parsedBody, Whitelist.none()), false);
//
//            //parsedBody = parsedBody.replaceAll("(^\\h*)|(\\h*$)","");
//            StringTokenizer st = new StringTokenizer(parsedBody);
//            while (st.hasMoreTokens()) {
//                String word = st.nextToken();
//                word = word.replaceAll("\\W", "");
////                if (!word.equals(""))
////                    System.out.println(word);
//            }





            StanfordLemmatizer stanfordLemmatizer = new StanfordLemmatizer();
            System.out.println(stanfordLemmatizer.lemmatize("better"));
            System.out.println(stanfordLemmatizer.lemmatize("caring"));


//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
