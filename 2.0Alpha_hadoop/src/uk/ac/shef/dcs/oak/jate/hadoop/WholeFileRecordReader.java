package uk.ac.shef.dcs.oak.jate.hadoop;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * @author priya
 * 
 */
class WholeFileRecordReader extends RecordReader<Text, Text> {
	private FileSplit fileSplit;
	private Configuration conf;
	private boolean processed = false;
	private Text key = new Text();
	private Text value = new Text();

	public void initialize(InputSplit inputSplit,
			TaskAttemptContext taskAttemptContext) throws IOException,
			InterruptedException {
		this.fileSplit = (FileSplit) inputSplit;
		this.conf = taskAttemptContext.getConfiguration();
	}

	public boolean nextKeyValue() throws IOException {
		if (!processed) {
			byte[] contentsByte = new byte[(int) fileSplit.getLength()];
			Path file = fileSplit.getPath();
			FileSystem fs = file.getFileSystem(conf);
			FSDataInputStream in = null;
			try {
				in = fs.open(file);
				IOUtils.readFully(in, contentsByte, 0, contentsByte.length);
			} finally {
				IOUtils.closeStream(in);
			}
			value.set(contentsByte, 0, contentsByte.length);
			processed = true;
			return true;
		}
		return false;
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		String fileName = fileSplit.getPath().getName();
		key.set(fileName);
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return processed ? 1.0f : 0.0f;
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}
}
