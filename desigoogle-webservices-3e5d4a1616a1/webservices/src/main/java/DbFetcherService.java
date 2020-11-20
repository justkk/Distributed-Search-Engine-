import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.tartarus.snowball.ext.PorterStemmer;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DbFetcherService {

    Connection conn;

    private PorterStemmer porterStemmer = new PorterStemmer();

    Gson gson = new Gson();

    public DbFetcherService() {

        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://34.200.242.63:3306/desigoogle", "root", "iiit123");
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getWord(String wordWithPunctuations) {
        String word = wordWithPunctuations.replaceAll("\\W", " ");
        word = word.replaceAll("\\b\\d+\\b", "");
        word = word.toLowerCase();
        word = word.replaceAll("\n", "");
        word = word.replaceAll("\r", "");
        return word;
    }


    public List<UiResponse> getResponseForQuery(String queryString) {

        String cleanedQuery = getCleanedQuert(queryString);
        LinkedHashMap<String, Double> words = getTokens(cleanedQuery);
        List<List<DocVectorObject>> allVectors = words.entrySet().stream().map
                (wordEntrySet -> getDocVectorObject(wordEntrySet.getKey())).collect(Collectors.toList());

        List<DocVectorObject> normalizedList = normalize(allVectors, words);
        ArrayList<UrlObject> urlObjectList = getURLObject(normalizedList);
        List<String> hostnames = getHostNames(urlObjectList);
        Map<String, Double> pageRank = getPageRankValue(hostnames);
        enrichPageRank(urlObjectList, pageRank);
        Collections.sort(urlObjectList);
        Collections.reverse(urlObjectList);
        int size = 100;
        if(urlObjectList.size() < size) {
            size =  urlObjectList.size();
        }
        List<UrlObject> subList = urlObjectList.subList(0,size);
        enrichDocContent(subList);

        return subList.stream().map(entry -> new UiResponse(entry.getUrl(), entry.getContent(), getHostName(entry.getUrl()))).collect(Collectors.toList());

    }

    public String getHostName(String url) {
        URLInfo urlInfo = new URLInfo(url);
        return urlInfo.getHostName();
    }


    public void enrichPageRank(List<UrlObject> urlObjectList, Map<String, Double> pagerank) {

        for (UrlObject urlObject : urlObjectList) {
            String completeUrl = urlObject.getUrl();
            URLInfo urlInfo = new URLInfo(completeUrl);
            Double pageRank = pagerank.getOrDefault(urlInfo.getHostName(), 0.15);
            urlObject.setPagerank(pageRank);
            urlObject.setScore(getScore(urlObject.getTfidf(), urlObject.getPagerank(), urlInfo.getFilePath()));
        }

    }


    public void enrichDocContent(List<UrlObject> urlObjectList) {


        List<String> docId = urlObjectList.stream().map(entry -> entry.getDocId()).collect(Collectors.toList());

        Map<String, String> data = new HashMap<>();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < docId.size(); i++) {
            stringBuilder.append("'");
            stringBuilder.append(docId.get(i));
            stringBuilder.append("'");
            if (i != docId.size() - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")");
        try {

            String selectStatement = "SELECT * from docdata where docid in " + stringBuilder.toString();
            PreparedStatement pst = conn.prepareStatement(selectStatement);
//            pst.setString(1, stringBuilder.toString());

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String docid = rs.getString(1);
                String content = rs.getString(2);
                data.put(docid, content);
            }

            urlObjectList.forEach(entry -> {
                String did = entry.getDocId();
                String content = data.get(did).replaceAll("\\s+", " ");
                entry.setContent(content);
            });

        } catch (Exception e) {

        }



    }

    public Double getScore(Double tfidf, Double pagerank, String filepath) {
        Double penality = 0.2 - Math.log(filepath.length()) / 2.3 * 0.03;
        return tfidf + Math.log(1 + pagerank) * 0.1 / 2.3 + penality;
    }

    public List<String> getHostNames(List<UrlObject> urlObjects) {

        List<String> hostLinks = new ArrayList<>();

        for (UrlObject urlObject : urlObjects) {
            String completeUrl = urlObject.getUrl();
            URLInfo urlInfo = new URLInfo(completeUrl);
            hostLinks.add(urlInfo.getHostName());
        }
        return hostLinks;
    }


    public List<DocVectorObject> normalize(List<List<DocVectorObject>> allVectors, LinkedHashMap<String, Double> words) {

        Map<String, Double> finalScoreMap = new HashMap<>();

        List<String> wordList = new ArrayList<>(words.keySet());

        for (int index = 0; index < wordList.size(); index++) {

            String word = wordList.get(index);
            List<DocVectorObject> docVectorObjectList = allVectors.get(index);

            for (DocVectorObject docVectorObject : docVectorObjectList) {

                String docIndex = docVectorObject.getDocId();

                if (!finalScoreMap.containsKey(docIndex)) {
                    finalScoreMap.put(docIndex, 0.0);
                }

                Double prevValue = finalScoreMap.get(docIndex);
                Double currentValue = prevValue + words.get(word) * docVectorObject.getTfIDF();
                finalScoreMap.put(docIndex, currentValue);

            }

        }

        List<DocVectorObject> result = new ArrayList<>();
        finalScoreMap.entrySet().stream().forEach(stringDoubleEntry -> {
            result.add(new DocVectorObject(stringDoubleEntry.getKey(), stringDoubleEntry.getValue()));
        });
        return result;

    }

    public ArrayList<UrlObject> getURLObject(List<DocVectorObject> docVectorObjects) {

        Map<String, Double> stringDoubleMap = new HashMap<>();
        Map<String, Double> oldValueMap = new HashMap<>();
        ArrayList<UrlObject> urlObjectList = new ArrayList<>();


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < docVectorObjects.size(); i++) {
            stringBuilder.append("'");
            stringBuilder.append(docVectorObjects.get(i).getDocId());
            oldValueMap.put(docVectorObjects.get(i).getDocId(), docVectorObjects.get(i).getTfIDF());
            stringBuilder.append("'");
            if (i != docVectorObjects.size() - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")");
        try {

            String selectStatement = "SELECT * from urldoc where docid in " + stringBuilder.toString();
            PreparedStatement pst = conn.prepareStatement(selectStatement);
//            pst.setString(1, stringBuilder.toString());

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String url = rs.getString(1);
                String docid = rs.getString(2);
                Double tfidf = oldValueMap.get(docid);
                urlObjectList.add(new UrlObject(url, docid, tfidf, 0.15));
            }

        } catch (Exception e) {

        }
        return urlObjectList;
    }

    public Map<String, Double> getPageRankValue(List<String> hostList) {

        Map<String, Double> stringDoubleMap = new HashMap<>();

        hostList.stream().forEach(stringValue -> {
            stringDoubleMap.put(stringValue, 0.15);
        });

        try {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(");
            for (int i = 0; i < hostList.size(); i++) {
                stringBuilder.append("'");
                stringBuilder.append(hostList.get(i));
                stringBuilder.append("'");
                if (i != hostList.size() - 1) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append(")");
            String selectStatement = "SELECT * from pagerank where url in " + stringBuilder.toString();
            PreparedStatement pst = conn.prepareStatement(selectStatement);
//            pst.setString(1, stringBuilder.toString());

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String value = rs.getString(2);
                String key = rs.getString(1);
                Double v = Double.valueOf(value);
                if (v < 0.15) {
                    v = 0.15;
                }
                stringDoubleMap.put(key, v);
            }

        } catch (Exception e) {

        }
        return stringDoubleMap;
    }


    public List<DocVectorObject> getDocVectorObject(String word) {

        Map<String, Double> docVectorObjectMap = new HashMap<>();
        try {
            String queryWord = "" + word + "_1";
            String selectStatement = "SELECT * from indexdata where wordkey=?";
            PreparedStatement pst = null;
            pst = conn.prepareStatement(selectStatement);
            pst.setString(1, queryWord);
            ResultSet rs = pst.executeQuery();
            String value = null;
            while (rs.next()) {
                value = rs.getString(2);
                Type listType = new TypeToken<ArrayList<DocVectorObject>>() {
                }.getType();
                List<DocVectorObject> docObjects = gson.fromJson(value.toString(), listType);
                for (DocVectorObject docVectorObject : docObjects) {
                    Double currentValue = docVectorObject.getTfIDF();
                    Double prevValue = 0.0;
                    if (docVectorObjectMap.containsKey(docVectorObject.getDocId())) {
                        prevValue = docVectorObjectMap.get(docVectorObject.getDocId());
                    }

                    if (prevValue < currentValue) {
                        prevValue = currentValue;
                    }
                    docVectorObjectMap.put(docVectorObject.getDocId(), prevValue);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return docVectorObjectMap.entrySet().stream()
                .map(mapentry -> new DocVectorObject(mapentry.getKey(), mapentry.getValue())).collect(Collectors.toList());
    }


    private LinkedHashMap<String, Double> getTokens(String cleanedQuery) {
        StringTokenizer stringTokenizer = new StringTokenizer(cleanedQuery);
        LinkedHashMap<String, Integer> stringMap = new LinkedHashMap<>();
        int totalCount = 0;
        while (stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            if (!stringMap.containsKey(word)) {
                stringMap.put(word, 0);
            }
            Integer finalValue = stringMap.get(word) + 1;
            stringMap.put(word, finalValue);
            totalCount += 1;
        }
        LinkedHashMap<String, Double> tfidf = new LinkedHashMap<>();

        final int totalCountFinal = totalCount;
        stringMap.entrySet().stream().forEach(stringIntegerEntry -> {
            Double value = stringIntegerEntry.getValue() * 1.0 / totalCountFinal;
            tfidf.put(stringIntegerEntry.getKey(), value);
        });

        return tfidf;
    }

    private String getCleanedQuert(String queryString) {
        StringTokenizer stringTokenizer = new StringTokenizer(queryString);
        String cleanedQuery = "";
        while (stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();
            String trimmedWord = DbFetcherService.getWord(word);
            if (!trimmedWord.equals("")) {
                porterStemmer.setCurrent(trimmedWord);
                porterStemmer.stem();
                String ss = porterStemmer.getCurrent();
                cleanedQuery += ss + " ";
            }
        }
        return cleanedQuery;
    }


    public static void main(String[] args) {


        String query = "donald trump";

        DbFetcherService dbFetcherService = new DbFetcherService();
        System.out.println(dbFetcherService.getCleanedQuert(query));
        dbFetcherService.getResponseForQuery(query);

    }
}
