package Document;

public class Frequency {
    private String word;
    private String docId;
    private Double tfIdf;
    private Double idf;

    public Frequency(String word, String docId, Double tfIdf, Double idf) {
        this.word = word;
        this.docId = docId;
        this.tfIdf = tfIdf;
        this.idf = idf;
    }

    public Frequency(String docId, Double tfIdf) {
        this.docId = docId;
        this.tfIdf = tfIdf;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Double getTfIdf() {
        return tfIdf;
    }

    public void setTfIdf(Double tfIdf) {
        this.tfIdf = tfIdf;
    }

    public Double getIdf() {
        return idf;
    }

    public void setIdf(Double idf) {
        this.idf = idf;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
