package edu.upenn.cis.cis455.model.index;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

@Persistent
public class DocChannelIndexKey {

    @KeyField(1) private Integer docId;
    @KeyField(2) private String channelName;

    public DocChannelIndexKey(Integer docId, String channelName) {
        this.docId = docId;
        this.channelName = channelName;
    }

    public DocChannelIndexKey() {
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
