import Document.Frequency;
import Document.SQLInsert;
import com.google.gson.Gson;
import org.apache.hadoop.io.Text;

import java.util.*;

public class SortUtility {

    public static final Gson gson = new Gson();

    public static class MyComparator implements Comparator<SQLInsert>
    {
        public int compare( SQLInsert x, SQLInsert y )
        {
            if( x.getTfIDF() - y.getTfIDF() > 0) {
                return 1;
            }
            if(x.getTfIDF() - y.getTfIDF() < 0) {
                return -1;
            }
            return 0;
        }
    }

    public static class MyComparator2 implements Comparator<SQLInsert>
    {
        public int compare( SQLInsert x, SQLInsert y )
        {
            if( x.getTfIDF() - y.getTfIDF() > 0) {
                return -1;
            }
            if(x.getTfIDF() - y.getTfIDF() < 0) {
                return 1;
            }
            return 0;
        }
    }



    public static List<SQLInsert> getSQLInsert(Iterator<Text> hdfsValues, int limit) {

        PriorityQueue<SQLInsert> minHeap= new PriorityQueue<SQLInsert>( new MyComparator());

        while (hdfsValues.hasNext()) {
            String value = hdfsValues.next().toString();
            Frequency frequency = gson.fromJson(value, Frequency.class);
            minHeap.add(new SQLInsert(frequency.getDocId(), frequency.getTfIdf()));
            if(minHeap.size() > limit) {
                minHeap.remove();
            }
        }
        List<SQLInsert> data = new ArrayList<SQLInsert>(minHeap);
        Collections.sort(data, new MyComparator2());
        return data;
    }

    public static void main(String[] args) {

        Frequency frequency1 = new Frequency("1", 10.0);
        Frequency frequency2 = new Frequency("2", 0.0);
        Frequency frequency3 = new Frequency("3", 20.0);
        Frequency frequency4 = new Frequency("4", 5.0);
        Frequency frequency5 = new Frequency("5", 6.0);


        List<Text> textList = new ArrayList<>();

        textList.add(new Text(gson.toJson(frequency1)));
        textList.add(new Text(gson.toJson(frequency2)));
        textList.add(new Text(gson.toJson(frequency3)));
        textList.add(new Text(gson.toJson(frequency4)));
        textList.add(new Text(gson.toJson(frequency5)));


        List<SQLInsert> answer = getSQLInsert(textList.iterator(), 3);
        System.out.println(answer);


    }
}
