package edu.upenn.cis.cis455.crawler.queue;

import edu.upenn.cis.cis455.crawler.info.URLInfo;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ReadyQueueInstance implements Serializable {

    private URLInfo urlInfo;
    private String parentId;

    public ReadyQueueInstance(URLInfo urlInfo) {
        this.urlInfo = urlInfo;
        this.parentId = "Root";
    }

    public ReadyQueueInstance(URLInfo urlInfo, String parentId) {
        this.urlInfo = urlInfo;
        this.parentId = parentId;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ReadyQueueInstance) {
            ReadyQueueInstance instance = (ReadyQueueInstance) obj;
            if (this.getUrlInfo().equals(instance.getUrlInfo())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return urlInfo.hashCode();
    }

    public URLInfo getUrlInfo() {
        return urlInfo;
    }

    public boolean isValid() {
        return urlInfo.isValid();
    }


    public static void main(String[] args) throws MalformedURLException {

//        URL url = new URL("http://www.google.com");
////        System.out.println(url.toString());
//
//        ReadyQueueInstance readyQueueInstance1 = new ReadyQueueInstance(new URLInfo("https://www.google.com:443"));
//        ReadyQueueInstance readyQueueInstance2 = new ReadyQueueInstance(new URLInfo("https://www.google.com"));
//
////        System.out.println(readyQueueInstance1.equals(readyQueueInstance2));
//
//        Set<ReadyQueueInstance> visitedReadyQueueInstanceSet = new HashSet<>();
//        visitedReadyQueueInstanceSet.add(readyQueueInstance1);
//        System.out.println(visitedReadyQueueInstanceSet.contains(readyQueueInstance2));

    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
