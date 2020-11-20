package edu.upenn.cis555.pagerank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import edu.upenn.cis555.pagerank.models.ParentChildOutputData;

/**
 * @author the_katoch
 * Class to verify counts after pagerank iterations
 */
public class VerifyPageRankSum {
	private static final Gson gson = new Gson();
	
	private static Logger logger = LogManager.getLogger(VerifyPageRankSum.class);

	/**
	 * @author the_katoch
	 * Mapper, emits pagerank for each parent
	 */
	public static class SumMapper extends Mapper<Object, Text, NullWritable, DoubleWritable> {
		
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			if(value!=null) {
				ParentChildOutputData data = gson.fromJson(value.toString(), ParentChildOutputData.class);
				context.write(NullWritable.get(), new DoubleWritable(data.getPageRank()));
			}
		}
	}

	/**
	 * @author the_katoch
	 * Reducer, emits the sum of the pageranks
	 */
	public static class SumReducer extends Reducer<NullWritable,DoubleWritable,NullWritable,DoubleWritable> {
		
		@Override
		public void reduce(NullWritable keyString, Iterable<DoubleWritable> values,Context context) throws IOException, InterruptedException {
			Double sum = new Double(0);
			for(DoubleWritable value:values) {
				sum += value.get();
			}
			logger.info("Pagerank count: "+sum);
			context.write(NullWritable.get(), new DoubleWritable(sum));
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis555", Level.DEBUG);
		
		if (args.length == 0 || args.length > 2) {
            VerifyPageRankSum.logger.error("Invalid arguments");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];
        
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "verifySum");
        job.setJarByClass(VerifyPageRankSum.class);
        job.setMapperClass(VerifyPageRankSum.SumMapper.class);
        job.setReducerClass(VerifyPageRankSum.SumReducer.class);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(DoubleWritable.class);
        //job.setInputFormatClass(CompleteFileInputFormat.class);

        FileSystem fileSystem = FileSystem.get(conf);
        String jobOutputDir = outputDir;
        if (fileSystem.exists(new Path(jobOutputDir)))
            fileSystem.delete(new Path(jobOutputDir), true);

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(jobOutputDir));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
