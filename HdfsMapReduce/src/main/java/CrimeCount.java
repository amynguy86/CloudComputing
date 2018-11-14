import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

public class CrimeCount {
	private static int numRegionDigits;

	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable> {
		private final static LongWritable one = new LongWritable(1);
		private Text newKey = new Text();

		public void map(LongWritable key, Text value, OutputCollector<Text, LongWritable> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			String values[] = line.split(",");
			String regionCode = "";
			String crimeType = "";
			if (values.length != 8)
				return;
			
			numRegionDigits = 1 ; //I had to hardcode this
			if (values[4].length() >= numRegionDigits && values[5].length() >= numRegionDigits) {
				regionCode = values[4].substring(0, numRegionDigits).concat(values[5].substring(0, numRegionDigits));
				crimeType = values[7];

				newKey.set(regionCode.concat("|").concat(crimeType));
				output.collect(newKey, one);
			}
		}
	}

	public static class Reduce extends MapReduceBase implements Reducer<Text, LongWritable, Text, LongWritable> {
		// One key now could be mapped to multiple values
		public void reduce(Text key, Iterator<LongWritable> values, OutputCollector<Text, LongWritable> output,
				Reporter reporter) throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(key, new LongWritable(sum));
		}
	}

	public static void main(String args[]) throws Exception {
		if (args.length == 3) {
			numRegionDigits = Integer.parseInt(args[2]);
		} else {
			numRegionDigits = 1;
		}
		
		JobConf conf = new JobConf(CrimeCount.class);
		conf.setJobName("crimecount");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(LongWritable.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		if (args.length == 3) {
			numRegionDigits = Integer.parseInt(args[2]);
		} else {
			numRegionDigits = 1;
		}
		JobClient.runJob(conf);
	}
}