package edu.upenn.cis455.mapreduce.job;

import java.util.Iterator;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WordCount implements Job {

	/**
	 * This is a method that lets us call map while recording the StormLite source executor ID.
	 * 
	 */
	public void map(String key, String value, Context context, String sourceExecutor)
	{
		// Your map function for WordCount goes here
		// key is index, Value is word
		context.write(value, "1", sourceExecutor);
	}

	/**
	 * This is a method that lets us call map while recording the StormLite source executor ID.
	 * 
	 */
	public void reduce(String key, Iterator<String> values, Context context, String sourceExecutor)
	{
		// Your reduce function goes here
		int finalCount = 0;
		while (values.hasNext()) {
			int value = Integer.valueOf(values.next());
			System.out.println("Entry " + key + " : " + String.valueOf(value));
			finalCount += value;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		context.write(key, String.valueOf(finalCount), sourceExecutor);
	}

}
