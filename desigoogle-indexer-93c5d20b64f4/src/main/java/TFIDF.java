import Document.DocObject;
import Document.Frequency;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class TFIDF {
    private static final Logger logger = LogManager.getLogger(WordCount.class);

    public static class TFMapper
            extends Mapper<Object, Text, Text, Text> {
        private final static Gson gson = new Gson();
        private Map<String, Integer> wordFreq = new HashMap<>();
        private int maxFrequency = 0;
        private Text outputKey = new Text();
        private Text outputVal = new Text();

        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            wordFreq.clear();
            maxFrequency = 0;

            DocObject docObject = gson.fromJson(value.toString(), DocObject.class);
            StringTokenizer itr = new StringTokenizer(docObject.getContent());

            while (itr.hasMoreTokens()) {
                String newWord = itr.nextToken();

                if (wordFreq.containsKey(newWord)) {
                    Integer frequency = wordFreq.get(newWord);
                    wordFreq.put(newWord, frequency + 1);
                } else {
                    wordFreq.put(newWord, 1);
                }
            }

            Set<String> words = wordFreq.keySet();

            for (String word : words) {
                Integer frequency = wordFreq.get(word);
                if (maxFrequency < frequency)
                    maxFrequency = frequency;
            }

            for (String word : words) {
                Integer frequency = wordFreq.get(word);
                Double tf = 0.5 + (0.5 * frequency) / maxFrequency;
                Frequency freq = new Frequency(docObject.getId(), tf);
                String val = gson.toJson(freq, Frequency.class);
                outputKey.set(word);
                outputVal.set(val);
                context.write(outputKey, outputVal);
            }
        }
    }

    public static class TFIDFReducer
            extends Reducer<Text, Text, NullWritable, Text> {
        private final static Gson gson = new Gson();
        private Text outputVal = new Text();
        private List<String> inputValues = new LinkedList<>();
        private Double idf = 0.0;

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {
            inputValues.clear();
            int nDocs = 0;
            for (Text value : values) {
                inputValues.add(value.toString());
                nDocs += 1;
            }

            idf = Math.log(1007820.0 / nDocs);

            for (String inputValue : inputValues) {
                Frequency frequency = gson.fromJson(inputValue, Frequency.class);
                Double termFreq = frequency.getTfIdf();
                Double tfIdf = termFreq * idf;
                frequency.setTfIdf(tfIdf);
                frequency.setIdf(idf);
                frequency.setWord(key.toString());
                String out = gson.toJson(frequency, Frequency.class);
                outputVal.set(out);
                context.write(NullWritable.get(), outputVal);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length > 2) {
            TFIDF.logger.error("Invalid arguments");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "tfIdf");
        job.setJarByClass(TFIDF.class);
        job.setMapperClass(TFIDF.TFMapper.class);
        job.setReducerClass(TFIDF.TFIDFReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        FileSystem fileSystem = FileSystem.get(conf);
        if (fileSystem.exists(new Path(outputDir)))
            fileSystem.delete(new Path(outputDir), true);

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(outputDir));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
