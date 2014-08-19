package uk.ac.shef.dcs.oak.jate.test;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RAKEAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RAKEFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.PhraseExtractor;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;

public class TestRAKE extends Mapper<Text, Text, Text, Text> {

	private static final Log log = LogFactory.getLog(TestRIDF.class);

	public void map(Text key, Text value, Context context) {
		FileSplit inputSplit = (FileSplit) context.getInputSplit();

		Path hdfsPath = inputSplit.getPath();

		Path parent = hdfsPath.getParent();
		try {

			System.out.println("Started " + TestRAKE.class + "at: "
					+ new Date()
					+ "... For detailed progress see log file jate.log.");

			// creates instances of required processors and resources

			// stop word list
			StopList stop = new StopList(true);

			// lemmatiser
			Lemmatizer lemmatizer = new Lemmatizer();

			// noun phrase extractor
			PhraseExtractor npextractor = new PhraseExtractor(stop, lemmatizer);

			GlobalIndexBuilderMem indexbuilder = new GlobalIndexBuilderMem();

			// build the global resource index

			List<String> candidates = indexbuilder.build(new CorpusImpl(
					parent.toString()), npextractor);

			AlgorithmTester tester = new AlgorithmTester();
			tester.registerAlgorithm(new RAKEAlgorithm(),
					new RAKEFeatureWrapper(candidates));
			tester.execute(context);
			System.out.println("Ended at: " + new Date());
		} catch (IOException e) {
			log.error("Got an IOException", e);
		} catch (InterruptedException e) {
			log.error("Got an InterruptedException", e);
		} catch (JATEException e) {
			log.error("Got a JateException", e);
		}
	}

	public static void main(String[] args) throws IOException, JATEException {

		TestRAKEJob ridfJob = new TestRAKEJob();
		try {
			ridfJob.runRakeJob(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}