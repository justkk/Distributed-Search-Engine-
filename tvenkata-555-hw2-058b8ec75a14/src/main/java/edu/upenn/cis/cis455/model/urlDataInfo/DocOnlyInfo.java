package edu.upenn.cis.cis455.model.urlDataInfo;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import edu.upenn.cis.cis455.utils.Md5HashGenerator;

@Entity
public class DocOnlyInfo {

    @PrimaryKey(sequence = "DocOnlyInfoId")
    private Integer id;
    private String content;
    private int contentLength = 0;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String hash;

    public DocOnlyInfo() {
    }

    public DocOnlyInfo(String content, int contentLength) {
        this.content = content;
        this.contentLength = contentLength;
        this.hash = Md5HashGenerator.getHash(content);
    }

    public DocOnlyInfo(Integer id, String content, int contentLength) {
        this.id = id;
        this.content = content;
        this.contentLength = contentLength;
    }

    public Integer getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getHash() {
        return hash;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
