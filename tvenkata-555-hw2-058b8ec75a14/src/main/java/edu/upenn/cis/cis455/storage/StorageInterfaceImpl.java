package edu.upenn.cis.cis455.storage;

import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.crawler.DocType;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.model.UserInfoManager;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;
import edu.upenn.cis.cis455.storage.managers.*;
import edu.upenn.cis.cis455.utils.SHAHashGenerator;

import java.util.HashMap;

public class StorageInterfaceImpl implements StorageInterface {

    private String location;
    private HostRobotInfoManager hostRobotInfoManager;
    private HostCrawlDelayManager hostCrawlDelayManager;
    private URLDataManager urlDataManager;
    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private UserInfoManager userInfoManager;
    private UserChannelDataManager userChannelDataManager;
    private IndexManager indexManager;
    private SubcriptionManger subcriptionManger;

    public StorageInterfaceImpl(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        hostRobotInfoManager = new HostRobotInfoManager(dataBaseConnectorConfig);
        hostCrawlDelayManager = new HostCrawlDelayManager();
        urlDataManager = new URLDataManager(dataBaseConnectorConfig);
        userInfoManager = new UserInfoManager(dataBaseConnectorConfig);
        userChannelDataManager = new UserChannelDataManager(dataBaseConnectorConfig);
        indexManager = new IndexManager(dataBaseConnectorConfig);
        subcriptionManger = new SubcriptionManger(dataBaseConnectorConfig);

    }

    public StorageInterfaceImpl(String location) {
        this.location = location;
        dataBaseConnectorConfig = new DataBaseConnectorConfig(location);
        hostRobotInfoManager = new HostRobotInfoManager(dataBaseConnectorConfig);
        hostCrawlDelayManager = new HostCrawlDelayManager();
        urlDataManager = new URLDataManager(dataBaseConnectorConfig);
        userInfoManager = new UserInfoManager(dataBaseConnectorConfig);
        userChannelDataManager = new UserChannelDataManager(dataBaseConnectorConfig);
        indexManager = new IndexManager(dataBaseConnectorConfig);
        subcriptionManger = new SubcriptionManger(dataBaseConnectorConfig);
    }

    @Override
    public int getCorpusSize() {
        return urlDataManager.getSize();
    }

    @Override
    public int addDocument(String url, String documentContents) {
        Transaction txn = this.dataBaseConnectorConfig.getDatabaseEnvironment().beginTransaction(null,
                null);
        URLInfo urlInfo = new URLInfo(url);
        String protocol = urlInfo.isSecure() ? "https" : "http";
        URLDataInfoKey urlDataInfoKey = new URLDataInfoKey(protocol, urlInfo.getHostName(), urlInfo.getPortNo(),
                urlInfo.getFilePath(), "GET");
        URLDataInfo urlDataInfo = new URLDataInfo(urlDataInfoKey);
        urlDataInfo.setHeaders(new HashMap<>());
        urlDataInfo.setContentType("text/html");
        urlDataInfo.setContentLength(documentContents.length());
        urlDataInfo = urlDataManager.insertURLDataInfo(urlDataInfo, txn);
        txn.commit();
        return urlDataInfo.getId();
    }

    @Override
    public int getLexiconSize() {
        return 0;
    }

    @Override
    public int addOrGetKeywordId(String keyword) {
        return 0;
    }

    @Override
    public int addUser(String username, String password) {
        String pass = SHAHashGenerator.getHash(password);
        User user = new User(username, pass);
        user.setFirstName("");
        user.setLastName("");
        User oldUser = userInfoManager.getUserFromUserName(username);
        if (oldUser != null) {
            return -1;
        }
        user = userInfoManager.insertUser(user);
        return user.getUserId();
    }


    public int addUser(String username, String password, String firstName, String lastName) {
        String pass = SHAHashGenerator.getHash(password);
        User user = new User(username, pass);
        user.setLastName(lastName);
        user.setFirstName(firstName);
        User oldUser = userInfoManager.getUserFromUserName(username);
        if (oldUser != null) {
            return -1;
        }
        user = userInfoManager.insertUser(user);
        return user.getUserId();
    }

    @Override
    public boolean getSessionForUser(String username, String password) {
        if (password == null) {
            password = "";
        }
        if (username == null) {
            return false;
        }
        User user = userInfoManager.getUserFromUserName(username);
        if (user == null) {
            return false;
        }
        String pass = SHAHashGenerator.getHash(password);
        if (pass.equals(user.getPassword())) {
            return true;
        }
        return false;
    }

    @Override
    public String getDocument(String url) {
        Transaction txn = this.dataBaseConnectorConfig.getDatabaseEnvironment().beginTransaction(null,
                null);
        URLInfo urlInfo = new URLInfo(url);
        if (!urlInfo.isValid()) {
            return null;
        }
        String protocol = urlInfo.isSecure() ? "https" : "http";
        URLDataInfoKey urlDataInfoKey = new URLDataInfoKey(protocol, urlInfo.getHostName(), urlInfo.getPortNo(),
                urlInfo.getFilePath(), "GET");
        URLDataInfo urlDataInfo = urlDataManager.getURLDataInfo(urlDataInfoKey, txn);
        if (urlDataInfo == null) {
            return null;
        }
        txn.commit();
        return urlDataInfo.getData();
    }

    public String getContentType(String url) {

        Transaction txn = this.dataBaseConnectorConfig.getDatabaseEnvironment().beginTransaction(null,
                null);
        URLInfo urlInfo = new URLInfo(url);
        if (!urlInfo.isValid()) {
            return null;
        }
        String protocol = urlInfo.isSecure() ? "https" : "http";
        URLDataInfoKey urlDataInfoKey = new URLDataInfoKey(protocol, urlInfo.getHostName(), urlInfo.getPortNo(),
                urlInfo.getFilePath(), "GET");
        URLDataInfo urlDataInfo = urlDataManager.getURLDataInfo(urlDataInfoKey, txn);
        if (urlDataInfo == null) {
            return null;
        }
        txn.commit();
        DocType docType = DocType.getTypeFromContentType(urlDataInfo.getContentType());
        return docType.toString();
    }

    @Override
    public void close() {

        try {
            indexManager.close();
            userChannelDataManager.close();
            urlDataManager.close();
            hostRobotInfoManager.close();
            userInfoManager.close();
            dataBaseConnectorConfig.getDatabaseEnvironment().cleanLog();
            dataBaseConnectorConfig.getDatabaseEnvironment().close();
        } catch (Exception e) {
        }
    }


    public String getLocation() {
        return location;
    }

    public HostRobotInfoManager getHostRobotInfoManager() {
        return hostRobotInfoManager;
    }

    public HostCrawlDelayManager getHostCrawlDelayManager() {
        return hostCrawlDelayManager;
    }

    public URLDataManager getUrlDataManager() {
        return urlDataManager;
    }

    public DataBaseConnectorConfig getDataBaseConnectorConfig() {
        return dataBaseConnectorConfig;
    }

    public UserInfoManager getUserInfoManager() {
        return userInfoManager;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setHostRobotInfoManager(HostRobotInfoManager hostRobotInfoManager) {
        this.hostRobotInfoManager = hostRobotInfoManager;
    }

    public void setHostCrawlDelayManager(HostCrawlDelayManager hostCrawlDelayManager) {
        this.hostCrawlDelayManager = hostCrawlDelayManager;
    }

    public void setUrlDataManager(URLDataManager urlDataManager) {
        this.urlDataManager = urlDataManager;
    }

    public void setDataBaseConnectorConfig(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
    }

    public void setUserInfoManager(UserInfoManager userInfoManager) {
        this.userInfoManager = userInfoManager;
    }

    public UserChannelDataManager getUserChannelDataManager() {
        return userChannelDataManager;
    }

    public void setUserChannelDataManager(UserChannelDataManager userChannelDataManager) {
        this.userChannelDataManager = userChannelDataManager;
    }

    public IndexManager getIndexManager() {
        return indexManager;
    }

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    public SubcriptionManger getSubcriptionManger() {
        return subcriptionManger;
    }
}
