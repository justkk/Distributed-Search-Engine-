import Document.Frequency;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

public class IDF {
    private static Logger logger = LogManager.getLogger(IDF.class);
    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, DoubleWritable> {
        private final Gson gson = new Gson();
        private final static Text docId = new Text();
        private Text outputKey = new Text();
        private DoubleWritable outputValue = new DoubleWritable();


        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            Frequency docObject = gson.fromJson(value.toString(), Frequency.class);
            outputKey.set(docObject.getWord());
            outputValue.set(docObject.getIdf());
            context.write(outputKey,outputValue );
        }
    }

    public static class IntDocReducer
            extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        private DoubleWritable outputValue = new DoubleWritable();

        @Override
        public void reduce(Text key, Iterable<DoubleWritable> idfs,
                           Context context
        ) throws IOException, InterruptedException {
            for (DoubleWritable idf : idfs) {
                Double d = idf.get();
                outputValue.set(d);
                context.write(key, outputValue);
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length > 2) {
            IDF.logger.error("Invalid arguments");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "idf");

        job.setJarByClass(IDF.class);
        job.setMapperClass(IDF.TokenizerMapper.class);
        job.setReducerClass(IDF.IntDocReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileSystem fileSystem = FileSystem.get(conf);
        if (fileSystem.exists(new Path(outputDir)))
            fileSystem.delete(new Path(outputDir), true);

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(outputDir));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}