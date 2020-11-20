public class UrlObject implements Comparable<UrlObject> {

    private String url;
    private String docId;
    private Double tfidf;
    private Double pagerank;
    private Double score;
    private String content;

    @Override
    public int compareTo(UrlObject m)
    {
        if (this.score < m.score) {
            return -1;
        }
        if(this.score.equals(m.score)) {
            return 0;
        }
        return 1;
    }

    public UrlObject(String url, String docId, Double tfidf, Double pagerank) {
        this.url = url;
        this.docId = docId;
        this.tfidf = tfidf;
        this.pagerank = pagerank;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Double getTfidf() {
        return tfidf;
    }

    public void setTfidf(Double tfidf) {
        this.tfidf = tfidf;
    }

    public Double getPagerank() {
        return pagerank;
    }

    public void setPagerank(Double pagerank) {
        this.pagerank = pagerank;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
