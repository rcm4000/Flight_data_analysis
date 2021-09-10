// CS644 - Intro To Big data
// Final Project
// Team:  Ami Patel and Rayon Myrie
// Goal - to identify the three airlines with the highest and lowest probability of being on schedule
import java.io.IOException;
import java.util.TreeSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class CancellationReasonV2 {
	
			public static TreeSet<pairing> Count = new TreeSet<>();
			
			public static class reasonMapper extends Mapper<Object, Text, Text, IntWritable>
			{
			
			public void map(Object key, Text value, Context context) throws IOException, InterruptedException
			{
				String[] input = value.toString().split(",");
					
					if (!"Year".equals(input[0])) {
						if (!"NA".equals(input[21])) {
						 if (!"  ".equals(input[21])) {	
						   if ("1".equals(input[21])) {
							 String rea= input[22].substring(0,1);
							
							 String cancellationReason="";
							 switch(rea){
								case "A" : cancellationReason = "carrier";
											break;
								case "B" : cancellationReason = "weather";
											break;
								case "C" : cancellationReason = "NAS";
											break;
								case "D" : cancellationReason = "security";
											break;

						   } 
							 
							 context.write(new Text(cancellationReason), new IntWritable(1));
						   }	
						   }
						 
						}
						
					}
			}//endfunc
		
			}//endmapper
	
		
			public static class reasonReducer extends Reducer<Text, IntWritable, Text, IntWritable>
			{
		
			public void reduce(Text key, Iterable<IntWritable> values , Context context) throws IOException, InterruptedException 
			{
				int total = 0;
				for(IntWritable val : values)
					{
					total = total + val.get();
					}
	
				Count.add( new pairing(key.toString(), total) );
			
				if(Count.size() > 1)
				{
					Count.pollLast();
				}
				
				context.write(new Text(key), new IntWritable(total));
			//}
			}
		
		 protected void cleanup(Context context) throws IOException, InterruptedException 
			 {
			 context.write(new Text("  "),null);
			 context.write(new Text("  "),null);
			 
			 context.write(new Text("A most reason to cancel a flight is:  "),null);
    		  while (!Count.isEmpty())
				  {
					pairing MaxReason = Count.pollFirst();
					
					context.write(new Text(MaxReason.reason), new IntWritable(MaxReason.count));
				 }
			}
			
			}//endreducer



		public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException 
			{
		
		Configuration conf=new Configuration();
		Job job = Job.getInstance(conf,"d");
		
		
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		if(otherArgs.length !=2){
			System.err.println("Usage: Airline <input_file> <output_directory>");
			System.exit(2);
		}
		job.setJarByClass(CancellationReasonV2.class);

		job.setMapperClass(reasonMapper.class);
		job.setReducerClass(reasonReducer.class);
		

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.waitForCompletion(true);
		
		}

		public static class pairing implements Comparable<pairing>
		{
		
		String reason ;
		int count;
		pairing(String reason , int count)
			{
			this.reason  =  reason ;
			this.count = count;
			}

		public int compareTo(pairing reasonpair) 
			{
			if(this.count <= reasonpair.count)
				return 1;
			else
				return -1;
			}
		}

}
