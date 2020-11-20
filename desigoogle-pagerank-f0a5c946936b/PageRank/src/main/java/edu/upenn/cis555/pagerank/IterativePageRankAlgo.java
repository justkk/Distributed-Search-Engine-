package edu.upenn.cis555.pagerank;

import java.io.IOException;
import java.util.List;

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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import edu.upenn.cis555.pagerank.models.PageRankIntermediate;
import edu.upenn.cis555.pagerank.models.ParentChildOutputData;

/**
 * @author the_katoch
 * Class for iterative page rank algo. Should be run on output of GetInitialPageRank
 */
public class IterativePageRankAlgo {
	
	private static Logger logger = LogManager.getLogger(IterativePageRankAlgo.class);
	
	private static final Gson gson = new Gson();
	
	/**
	 * @author the_katoch
	 * Mapper for IterativePageRank, emits for each [a,childlist]
	 * a, [null,null,[childlist]]
	 * for each child, child [a, pagerank(a), n_children(a)]
	 */
	public static class PRMapper extends Mapper<Object, Text, Text, Text> {
		
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			if(value!=null) {
				ParentChildOutputData data = gson.fromJson(value.toString(), ParentChildOutputData.class);
				Text text = new Text(gson.toJson(new PageRankIntermediate(data)));
				logger.debug(text);
				for(String child : data.getChildren()) {
					context.write(new Text(child),text);
				}
				//Emit this to pass on childlist info
				context.write(new Text(data.getParent()), 
						new Text(gson.toJson(new PageRankIntermediate(data.getChildren()))));
			}
		}
	}

	/**
	 * @author the_katoch
	 * Reducer for IterativePageRank, emits for each key [parent, pagerank(parent), n_children(parent)]
	 * [key, newpagerank, keychildlist]
	 */
	public static class PRReducer extends Reducer<Text,Text,NullWritable,Text> {
		
		@Override
		public void reduce(Text keyString, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			List<String> forwardList = null;
			Double newPageRank = 0.0;
			Double alpha = 0.85;
			Double beta = 1-alpha;
			for(Text value:values) {
				PageRankIntermediate im = gson.fromJson(value.toString(), PageRankIntermediate.class);
				if(im.getParent()!=null) {
					//Update pagerank
					newPageRank = newPageRank + alpha*(im.getParentPageRank()/im.getParentOutgoingLinks());
				} else {
					//Fetch forward list
					forwardList = im.getOutgoingList();
				}
			}
			newPageRank += beta;
			if(forwardList!=null && forwardList.size()!=0) {
				ParentChildOutputData output = new ParentChildOutputData(keyString.toString(), 
					forwardList, newPageRank);
				Text outText = new Text(gson.toJson(output));
				System.out.println(outText);
				context.write(NullWritable.get(), outText);
			}
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		
		org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis555", Level.DEBUG);
		
		if (args.length == 0 || args.length > 3) {
            IterativePageRankAlgo.logger.error("Invalid arguments");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];
        
        Integer iterNum = null;
        try {
        	iterNum = Integer.valueOf(args[2]);
        } catch(Exception e) {
        	System.exit(1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "iterativePageRank");
        job.setJarByClass(IterativePageRankAlgo.class);
        job.setMapperClass(IterativePageRankAlgo.PRMapper.class);
        job.setReducerClass(IterativePageRankAlgo.PRReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
        //job.setInputFormatClass(CompleteFileInputFormat.class);

        FileSystem fileSystem = FileSystem.get(conf);
        String jobOutputDir = outputDir+"priter"+iterNum+'/';
        if (fileSystem.exists(new Path(jobOutputDir)))
            fileSystem.delete(new Path(jobOutputDir), true);

        FileInputFormat.addInputPath(job, new Path(inputDir));
        FileOutputFormat.setOutputPath(job, new Path(jobOutputDir));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
