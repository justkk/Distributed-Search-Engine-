package edu.upenn.cis555.pagerank;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import edu.upenn.cis555.dynamodb.UrlOnlyInfoDynamoDb;
import edu.upenn.cis555.pagerank.models.ParentChild;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import edu.upenn.cis555.pagerank.models.ParentChildOutputData;

public class GenerateCsv {
private static final Gson gson = new Gson();
	
	private static Logger logger = LogManager.getLogger(VerifyPageRankSum.class);

	/**
	 * @author the_katoch
	 * Mapper, emits pagerank for each parent
	 */
	public static class CsvMapper extends Mapper<Object, Text, Text, Text> {
		
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			if(value!=null) {

				Type listType = new TypeToken<ArrayList<UrlOnlyInfoDynamoDb>>() {
				}.getType();
				List<UrlOnlyInfoDynamoDb> urlOnlyInfoDynamoDbList = gson.fromJson(value.toString(), listType);

				for(UrlOnlyInfoDynamoDb urlOnlyInfoDynamoDb : urlOnlyInfoDynamoDbList) {
					String url = urlOnlyInfoDynamoDb.getKeyString();
					String[] split = url.split(":", 2);
					String finalUrl = null;
					if(split.length > 1) {
						finalUrl = split[1];
					} else {
						finalUrl = split[0];
					}
					context.write(new Text(finalUrl), new Text(urlOnlyInfoDynamoDb.getDocId()));
				}

			}
		}
	}

	/**
	 * @author the_katoch
	 * Reducer, emits the sum of the pageranks
	 */
	public static class CsvReducer extends Reducer<Text,Text,NullWritable,Text> {
		
		@Override
		public void reduce(Text keyString, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			context.write(NullWritable.get(), new Text(keyString+","+values.iterator().next()));
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis555", Level.DEBUG);
		
		if (args.length == 0 || args.length > 2) {
			args = new String[] {"input/", "csv/"};
			//GenerateCsv.logger.error("Invalid arguments");
            //System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];
        
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "generateCsv");
        job.setJarByClass(GenerateCsv.class);
        job.setMapperClass(GenerateCsv.CsvMapper.class);
        job.setReducerClass(GenerateCsv.CsvReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
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
