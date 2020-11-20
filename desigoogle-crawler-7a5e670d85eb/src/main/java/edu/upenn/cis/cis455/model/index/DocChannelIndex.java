package edu.upenn.cis.cis455.model.index;

import com.sleepycat.persist.model.*;
import edu.upenn.cis.cis455.model.urlDataInfo.DocOnlyInfo;

@Entity
public class DocChannelIndex {

    @PrimaryKey
    private DocChannelIndexKey docChannelIndexKey;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
//    relatedEntity = DocOnlyInfo.class,
//    onRelatedEntityDelete = DeleteAction.CASCADE)
    private String docId;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String channelName;

    public DocChannelIndex(DocChannelIndexKey docChannelIndexKey) {
        this.docChannelIndexKey = docChannelIndexKey;
        this.docId = docChannelIndexKey.getDocId();
        this.channelName = docChannelIndexKey.getChannelName();
    }

    public DocChannelIndex() {
    }

    public DocChannelIndexKey getDocChannelIndexKey() {
        return docChannelIndexKey;
    }

    public void setDocChannelIndexKey(DocChannelIndexKey docChannelIndexKey) {
        this.docChannelIndexKey = docChannelIndexKey;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
