import org.apache.commons.math3.analysis.function.Max;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
import java.util.StringTokenizer;

public class MaxFrequency {
    private static final Logger logger = LogManager.getLogger(MaxFrequency.class);
    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());

            assert (itr.countTokens() == 2) : value.toString() + " has more than 2 tokens";

            String docIdWord = itr.nextToken();
            int frequency = Integer.parseInt(itr.nextToken());

            int _pos = docIdWord.indexOf("_");
            assert (_pos != -1) : docIdWord + " does not contain _ to split docId and word";

            String docId = docIdWord.substring(0, _pos);

            context.write(new Text(docId), new IntWritable(frequency));
        }
    }

    public static class IntMaxReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int maxFrequency = 0;
            for (IntWritable val : values) {
                int frequency = val.get();
                if (maxFrequency < frequency)
                    maxFrequency = frequency;
            }
            result.set(maxFrequency);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length > 2) {
            MaxFrequency.logger.error("Invalid arguments");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "maxFrequency");
        job.setJarByClass(MaxFrequency.class);
        job.setMapperClass(MaxFrequency.TokenizerMapper.class);
        job.setCombinerClass(MaxFrequency.IntMaxReducer.class);
        job.setReducerClass(MaxFrequency.IntMaxReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileSystem fileSystem = FileSystem.get(conf);
        if (fileSystem.exists(new Path(outputDir)))
            fileSystem.delete(new Path(outputDir), true);

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(outputDir));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}