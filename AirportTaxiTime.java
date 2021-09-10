
// Goal - to identify the three airports with the longest and shortest average taxi time
import java.io.IOException;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AirportTaxiTime {
	public static class TaxiMapper extends Mapper<Object, Text, Text, Text> {
		@Override
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] input = value.toString().split(",");
			
			if (!"Year".equals(input[0])) {
				if (!"NA".equals(input[20])) {
					context.write(new Text(input[16]), new Text(input[20])); //enter origin + TaxiOut
				}				
				if (!"NA".equals(input[19])) {
					context.write(new Text(input[17]), new Text(input[19])); //enter destination + TaxiIn
				}
			}
		}
	}
	
	public static class TaxiReducer extends Reducer<Text, Text, Text, Text> {
		private Map<String, Double> airports = new TreeMap<String, Double>();

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			Iterator<Text> iter = values.iterator();
			int total = 0;
			Integer sum = 0;

			while (iter.hasNext()) {
				String str = iter.next().toString();
				int curr = Integer.parseInt(str);
				sum = sum + curr;
				total = total + 1;
			}
			
			double avg = sum * 1.0 / total;
			airports.put(key.toString(), Double.valueOf(avg));
	
			//context.write(new Text(key), new Text("Average Time "+ avg));
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			List<Entry<String, Double>> airportList = new ArrayList<Entry<String, Double>>(airports.entrySet());
			
			Collections.sort(airportList, new Comparator<Map.Entry<String, Double>>() {
				public int compare(Entry<String, Double> avg1, Entry<String, Double> avg2) {
					return avg2.getValue().compareTo(avg1.getValue());
				}
			});
			
			context.write(new Text("Longest Taxi Time"), new Text(""));
			for (int i = 0; i < 3; i++) {
				Entry<String, Double> airport = airportList.get(i);
				context.write(new Text(airport.getKey()), new Text(airport.getValue() + ""));
			}
			
			context.write(new Text("Shortest Taxi Time"), new Text(""));
			int length = airportList.size();
			for (int i = length - 1; i > length - 4; i--) {
				Entry<String, Double> airport = airportList.get(i);
				context.write(new Text(airport.getKey()), new Text(airport.getValue() + ""));
			}
			
			if(length == 0) {
				context.write(new Text("No Output."), new Text(""));
			}
			
		}
	}
	
	//Driver Class
	public static void main(String[] args) throws IOException,
	ClassNotFoundException, InterruptedException {

		Configuration conf=new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		if(otherArgs.length !=2){
			System.err.println("Usage: Airline <input_file> <output_directory>");
			System.exit(2);
		}
		Job OnTime = Job.getInstance(conf,"d");
		
		OnTime.setJarByClass(AirportTaxiTime.class);
		//OnTime.setNumReduceTasks(3);
		
		OnTime.setMapperClass(TaxiMapper.class);
		//OnTime.setCombinerClass(OnTimeReducer.class);
		OnTime.setReducerClass(TaxiReducer.class);
		
	
		FileInputFormat.addInputPath(OnTime, new Path(args[0]));
		FileOutputFormat.setOutputPath(OnTime,new Path(otherArgs[1]));
		
		OnTime.setMapOutputKeyClass(Text.class);
		OnTime.setMapOutputValueClass(Text.class);
		OnTime.setOutputKeyClass(Text.class);
		OnTime.setOutputValueClass(Text.class);
		OnTime.waitForCompletion(true);
        
        
        
      }

}
