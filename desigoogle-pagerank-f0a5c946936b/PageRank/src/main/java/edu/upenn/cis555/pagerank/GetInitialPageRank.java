package edu.upenn.cis555.pagerank;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
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

import edu.upenn.cis555.helpers.CompleteFileInputFormat;
import edu.upenn.cis555.pagerank.models.ParentChild;
import edu.upenn.cis555.pagerank.models.ParentChildOutputData;
import edu.upenn.cis555.pagerank.models.SinksIntermediate;

/**
 * @author the_katoch
 * Class converts raw data stored in a file location/s3 to combined file with adjacency list
 * and initial page rank
 */
public class GetInitialPageRank {
	
	private static Logger logger = LogManager.getLogger(GetInitialPageRank.class);
	private static final Gson gson = new Gson();

	
	/**
	 * @author the_katoch
	 * Mapper for initial page rank
	 * For each (a , childlist) emits (a,n_outgoing,childlist) 
	 * and for every child in childlist emits (child,0,[a])
	 */
	public static class BaseMapper extends Mapper<Object, Text, Text, Text> {
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			if(value!=null) {

				Type listType = new TypeToken<ArrayList<ParentChild>>() {
				}.getType();
				List<ParentChild> docObjects = gson.fromJson(value.toString(), listType);

				for(ParentChild data : docObjects) {
//					ParentChild data = gson.fromJson(value.toString(), ParentChild.class);
					SinksIntermediate parentIntermediate = new SinksIntermediate(data.getChildNames()!=null?data.getChildNames().size():0, data.getChildNames());
					context.write(new Text(data.getHostname()),
							new Text(gson.toJson(parentIntermediate)));
					if(data.getChildNames()!=null) {
						for(String child : data.getChildNames()) {
							//For each child, we send the backlink
							if(!data.getHostname().equals(child)) {
								List<String> parentList = new ArrayList<String>();
								parentList.add(data.getHostname());
								SinksIntermediate im = new SinksIntermediate(0, parentList);
								context.write(new Text(child), new Text(gson.toJson(im)));
							}
						}
					}

				}


			}
		}
	}

	/**
	 * @author the_katoch
	 * Reducer for initial page rank
	 * For each (a , n_outgoing, childlist) emits (a,pagerank,childlist)
	 */
	public static class BaseReducer extends Reducer<Text,Text,NullWritable,Text> {

		@Override
		public void reduce(Text keyString, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			
			//back set
			Set<String> parentSet = new HashSet<String>();
			
			//forward set
			Set<String> childSet = new HashSet<String>();

			for(Text value: values) {
				SinksIntermediate valueData = gson.fromJson(value.toString(), SinksIntermediate.class);
				if(valueData.getNumLinks()==0) {
					parentSet.addAll(valueData.getLinks());
				} else {
					childSet.addAll(valueData.getLinks());
				}
			}

			List<String> resultSet = new ArrayList<String>();
			
			//If child set is 0, we have no forward links, the node is a sink.
			if(childSet.size()!=0) {
				resultSet.addAll(childSet);
			} else if(parentSet.size()!=0){
				//If parent set is also 0, the node only has internal links
				resultSet.addAll(parentSet);
			}

			//Do not emit for sites with internal links
			if(!resultSet.isEmpty()) {
				context.write(NullWritable.get(), new Text(gson.toJson(new ParentChildOutputData(keyString.toString()
						, resultSet, 1.0))));
			}

		}

	}

	public static void main(String[] args) throws Exception {
		org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis555", Level.DEBUG);
		
		if (args.length == 0 || args.length > 2) {
			GetInitialPageRank.logger.error("Invalid arguments");
			System.exit(1);
		}

		String inputDir = args[0];
		String outputDir = args[1];

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "initializePageRanks");
		job.setJarByClass(GetInitialPageRank.class);
		job.setMapperClass(GetInitialPageRank.BaseMapper.class);
		job.setReducerClass(GetInitialPageRank.BaseReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(CompleteFileInputFormat.class);

		FileSystem fileSystem = FileSystem.get(conf);
		if (fileSystem.exists(new Path(outputDir)))
			fileSystem.delete(new Path(outputDir), true);

		FileInputFormat.addInputPath(job, new Path(inputDir));
		FileOutputFormat.setOutputPath(job, new Path(outputDir));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
