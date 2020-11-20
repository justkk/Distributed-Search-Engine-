package Document;

public class DocObject {
    private String id;
    private String content;
    private Integer contentLength = 0;
    private String hashString;

    public DocObject(String id, String content, Integer contentLength, String hashString) {
        this.id = id;
        this.content = content;
        this.contentLength = contentLength;
        this.hashString = hashString;
    }

    public DocObject() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public String getHashString() {
        return hashString;
    }

    public void setHashString(String hashString) {
        this.hashString = hashString;
    }
}
