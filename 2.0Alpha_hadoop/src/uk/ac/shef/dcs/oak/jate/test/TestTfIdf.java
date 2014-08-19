package uk.ac.shef.dcs.oak.jate.test;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;

public class TestTfIdf extends Mapper<Text, Text, Text, Text> {

	private static final Log log = LogFactory.getLog(TestTfIdf.class);

	public void map(Text key, Text value, Context context) {
		FileSplit inputSplit = (FileSplit) context.getInputSplit();

		Path hdfsPath = inputSplit.getPath();

		Path parent = hdfsPath.getParent();
		try {

			System.out.println("Started " + TestTfIdf.class + "at: "
					+ new Date()
					+ "... For detailed progress see log file jate.log.");

			// creates instances of required processors and resources

			// stop word list
			StopList stop = new StopList(true);

			// lemmatiser
			Lemmatizer lemmatizer = new Lemmatizer();

			// noun phrase extractor
			CandidateTermExtractor npextractor = new NounPhraseExtractorOpenNLP(
					stop, lemmatizer);
			// WordExtractor npextractor = new WordExtractor(stop, lemmatizer);

			// counters
			TermFreqCounter npcounter = new TermFreqCounter();
			WordCounter wordcounter = new WordCounter();

			// create global resource index builder, which indexes global
			// resources, such as documents and terms and their
			// relations
			GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
			// build the global resource index
			GlobalIndexMem termDocIndex = builder.build(new CorpusImpl(
					parent.toString()), npextractor);

			// build a feature store required by the tfidf algorithm, using the
			// processors instantiated above
			FeatureCorpusTermFrequency termCorpusFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(termDocIndex);

			AlgorithmTester tester = new AlgorithmTester();
			tester.registerAlgorithm(new TFIDFAlgorithm(),
					new TFIDFFeatureWrapper(termCorpusFreq));
			tester.execute(termDocIndex, context);
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

		TestTfIdfJob weirdnessJob = new TestTfIdfJob();
		try {
			weirdnessJob.runIfIdfJob(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
