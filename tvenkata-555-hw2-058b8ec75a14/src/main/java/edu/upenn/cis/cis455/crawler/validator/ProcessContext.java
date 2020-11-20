package edu.upenn.cis.cis455.crawler.validator;

import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.queue.WaitingQueueInstance;
import edu.upenn.cis.cis455.model.representationModels.RobotStructure;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.storage.managers.HostCrawlDelayManager;
import edu.upenn.cis.cis455.utils.RobotPathMatcher;

public class ProcessContext {

    URLInfo urlInfo;
    RobotStructure robotStructure;
    RobotPathMatcher.MatchingOutput matchingOutput;
    URLMetaInformation headInfo;
    URLMetaInformation getInfoFromDb;
    URLMetaInformation getInfo;
    WaitingQueueInstance waitingQueueInstance;
    ProcessContextStatus status;
    String message;
    HostCrawlDelayManager.CrawlDelayManagerOutput crawlDelayManagerOutput;
    Transaction txn;
    int maxDocumentSize;
    String md5Hash;
    boolean skip = false;
    boolean redirect = false;
    URLInfo redirectUrlInfo;
    boolean indexed = false;
    URLDataInfo urlDataInfo;

    public ProcessContext(URLInfo urlInfo, Transaction txn, int maxDocumentSize) {
        this.urlInfo = urlInfo;
        status = ProcessContextStatus.SUCCESS;
        this.txn = txn;
        this.maxDocumentSize = maxDocumentSize;
    }

    public boolean isValid() {
        return status == ProcessContextStatus.SUCCESS;
    }

    public boolean isWait() {
        return status == ProcessContextStatus.WAIT;
    }

    public boolean isFailure() {
        return status == ProcessContextStatus.FAILURE;
    }


    public enum ProcessContextStatus {
        SUCCESS,
        FAILURE,
        WAIT;
    }

    public URLInfo getUrlInfo() {
        return urlInfo;
    }

    public void setUrlInfo(URLInfo urlInfo) {
        this.urlInfo = urlInfo;
    }

    public RobotStructure getRobotStructure() {
        return robotStructure;
    }

    public void setRobotStructure(RobotStructure robotStructure) {
        this.robotStructure = robotStructure;
    }

    public URLMetaInformation getHeadInfo() {
        return headInfo;
    }

    public void setHeadInfo(URLMetaInformation headInfo) {
        this.headInfo = headInfo;
    }

    public URLMetaInformation getGetInfo() {
        return getInfo;
    }

    public void setGetInfo(URLMetaInformation getInfo) {
        this.getInfo = getInfo;
    }

    public WaitingQueueInstance getWaitingQueueInstance() {
        return waitingQueueInstance;
    }

    public void setWaitingQueueInstance(WaitingQueueInstance waitingQueueInstance) {
        this.waitingQueueInstance = waitingQueueInstance;
    }

    public ProcessContextStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessContextStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public RobotPathMatcher.MatchingOutput getMatchingOutput() {
        return matchingOutput;
    }

    public void setMatchingOutput(RobotPathMatcher.MatchingOutput matchingOutput) {
        this.matchingOutput = matchingOutput;
    }

    public HostCrawlDelayManager.CrawlDelayManagerOutput getCrawlDelayManagerOutput() {
        return crawlDelayManagerOutput;
    }

    public void setCrawlDelayManagerOutput(HostCrawlDelayManager.CrawlDelayManagerOutput crawlDelayManagerOutput) {
        this.crawlDelayManagerOutput = crawlDelayManagerOutput;
    }

    public URLMetaInformation getGetInfoFromDb() {
        return getInfoFromDb;
    }

    public void setGetInfoFromDb(URLMetaInformation getInfoFromDb) {
        this.getInfoFromDb = getInfoFromDb;
    }

    public Transaction getTxn() {
        return txn;
    }

    public int getMaxDocumentSize() {
        return maxDocumentSize;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public URLInfo getRedirectUrlInfo() {
        return redirectUrlInfo;
    }

    public void setRedirectUrlInfo(URLInfo redirectUrlInfo) {
        this.redirectUrlInfo = redirectUrlInfo;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public URLDataInfo getUrlDataInfo() {
        return urlDataInfo;
    }

    public void setUrlDataInfo(URLDataInfo urlDataInfo) {
        this.urlDataInfo = urlDataInfo;
    }
}
