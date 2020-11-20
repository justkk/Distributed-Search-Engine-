import Document.Frequency;
import Document.SQLInsert;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SimpleTFIDF {
    private static final Logger logger = LogManager.getLogger(SimpleTFIDF.class);

    public static class IdemMapper
            extends Mapper<Object, Text, Text, Text> {
        private final static Gson gson = new Gson();
        private Text outputKey = new Text();

        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            Frequency docObject = gson.fromJson(value.toString(), Frequency.class);
            outputKey.set(docObject.getWord());
            context.write(outputKey, value);
        }
    }

    public static class TFIDFReducer
            extends Reducer<Text, Text, Text, Text> {
        private final static Gson gson = new Gson();

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {

            List<SQLInsert> sqlInserts = SortUtility.getSQLInsert(values.iterator(), 20000);
            int size = sqlInserts.size();

            String word = key.toString();
            if (size >= 10000) {
                context.write(new Text(word + "_1"), new Text(gson.toJson(sqlInserts.subList(0, 10000))));
                if (size >= 20000)
                    context.write(new Text(word + "_2"), new Text(gson.toJson(sqlInserts.subList(10000, 20000))));
                else
                    context.write(new Text(word + "_2"), new Text(gson.toJson(sqlInserts.subList(10000, size))));
            }
            else {
                context.write(new Text(word + "_1"), new Text(gson.toJson(sqlInserts.subList(0, size))));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length > 2) {
            SimpleTFIDF.logger.error("Invalid arguments");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "simpletfIdf");
        job.setJarByClass(SimpleTFIDF.class);
        job.setMapperClass(SimpleTFIDF.IdemMapper.class);
        job.setReducerClass(SimpleTFIDF.TFIDFReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileSystem fileSystem = FileSystem.get(conf);
        if (fileSystem.exists(new Path(outputDir)))
            fileSystem.delete(new Path(outputDir), true);

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(outputDir));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}