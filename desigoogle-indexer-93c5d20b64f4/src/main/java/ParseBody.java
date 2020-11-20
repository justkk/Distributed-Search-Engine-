import Document.DocObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.stanford.nlp.simple.Sentence;
import html.HtmlFactory;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ParseBody {
    private static Logger logger = LogManager.getLogger(ParseBody.class);

    public static class ParserMapper
            extends Mapper<Object, Text, Text, Text> {
        private static final Gson gson = new Gson();

        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            System.out.println(key.toString());
            Text id = new Text();
            Text wordText = new Text();


            Type listType = new TypeToken<ArrayList<DocObject>>() {
            }.getType();
            List<DocObject> docObjects = gson.fromJson(value.toString(), listType);
            for (DocObject docObject : docObjects) {

                String docId = docObject.getId();
                String docContent = docObject.getContent();
                String head = HtmlFactory.getBody(docContent);

                id.set(docId);
                wordText.set(docContent);
                context.write(id, wordText);
            }
        }
    }

    public static class JsonReducer
            extends Reducer<Text, Text, NullWritable, Text> {

        private static final Gson gson = new Gson();
        @Override
        public void reduce(Text key, Iterable<Text> content,
                           Context context
        ) throws IOException, InterruptedException {

            DocObject docObject = new DocObject();
            Text result = new Text();

            docObject.setId(key.toString());
            docObject.setContentLength(-1);
            docObject.setHashString("####");

            for (Text text : content)
                docObject.setContent(text.toString());

            String res = gson.toJson(docObject);
            result.set(res);
            context.write(NullWritable.get(), result);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length > 2) {
            ParseBody.logger.error("Invalid arguments");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "parseBody");

        job.setJarByClass(ParseBody.class);
        job.setMapperClass(ParseBody.ParserMapper.class);
        job.setReducerClass(ParseBody.JsonReducer.class);

        job.setMapOutputKeyClass(Text.class);
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