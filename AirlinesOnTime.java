// CS644 - Intro To Big data
// Final Project
// Team:  Ami Patel and Rayon Myrie
// Goal - to identify the three airlines with the highest and lowest probability of being on schedule
import java.io.IOException;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.cli.util.SubstringComparator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.DoubleBinaryOperator;

public class AirlinesOnTime {
	
	public static class OnTimeMapper extends Mapper<Object, Text, Text, Text> {
		//@Override
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] input = value.toString().split(",");
			
			if (!"Year".equals(input[0])) {
				String ontime = "0";
				if(!"NA".equals(input[14]) && !"NA".equals(input[15])){ //'On time' is defined as having less than 10 minutes total difference from scheduled time
					if (Math.abs(Integer.parseInt(input[14])) + Math.abs(Integer.parseInt(input[15])) <= 10 ) {
						ontime = "1"; 
					}
				context.write(new Text(input[8]), new Text(ontime)); //enter carrier code + 0 or 1
				}
			}
		}
	}	
	
	public static class OnTimeReducer extends Reducer<Text, Text, Text, Text> {
		private Map<String, Double> flights = new TreeMap<String, Double>();
		
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			Iterator<Text> iter = values.iterator();
			int total = 0;
			double ontime = 0;
			
			while (iter.hasNext()) {
				int curr = Integer.parseInt(iter.next().toString());
				ontime = ontime + curr; 
				total = total + 1;
			}
			
			double prb = ontime/total;
			
			
			context.write(key, new Text("Probability = "+ prb + "  , ontime " + ontime +  " / Total " + total )); 
			
			flights.put(key.toString(), Double.valueOf(prb));
		}
		
		protected void cleanup (Context context) throws IOException, InterruptedException {
			List<Entry<String, Double>> flightList = new ArrayList<Entry<String, Double>>(flights.entrySet());
			
			Collections.sort(flightList,new Comparator<Map.Entry<String, Double>>() {  
	            public int compare(Entry<String, Double> line1, Entry<String, Double> line2) {  
	                return line2.getValue().compareTo(line1.getValue());  
	            }  
	        });
			context.write(new Text(" "), new Text( )); 
			context.write(new Text("The 3 Airlines with the Highest Probability of being on time are:"), new Text(""));
			for(int i = 0; i < 3; i++){
				Entry<String, Double> airline = flightList.get(i);
				context.write(new Text(airline.getKey()), new Text(airline.getValue() + ""));
			}
			context.write(new Text(" "), new Text( )); 
			context.write(new Text("The 3 Airlines with the Lowest Probability of being on time are:"), new Text(""));
			int length = flightList.size();
			for(int i = length - 1; i > length - 4; i--){
				Entry<String, Double> airline = flightList.get(i);
				context.write(new Text(airline.getKey()), new Text(airline.getValue()+""));
			}
			
			if(length == 0){
				context.write(new Text("No Output."), new Text(""));
			}
		}
			//	
			
		
	}

		

	public static void main(String[] args) throws IOException,
	ClassNotFoundException, InterruptedException {

		Configuration conf=new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		if(otherArgs.length !=2){
			System.err.println("Usage: Airline <input_file> <output_directory>");
			System.exit(2);
		}
		Job OnTime = Job.getInstance(conf,"d");
		
		OnTime.setJarByClass(AirlinesOnTime.class);
		
		OnTime.setMapperClass(OnTimeMapper.class);
		//OnTime.setCombinerClass(OnTimeReducer.class);
		OnTime.setReducerClass(OnTimeReducer.class);
		
	
		FileInputFormat.addInputPath(OnTime, new Path(args[0]));
		FileOutputFormat.setOutputPath(OnTime,new Path(otherArgs[1]));
		
		OnTime.setMapOutputKeyClass(Text.class);
		OnTime.setMapOutputValueClass(Text.class);
		OnTime.setOutputKeyClass(Text.class);
		OnTime.setOutputValueClass(Text.class);
		OnTime.waitForCompletion(true);
        
        
        
      }

}
