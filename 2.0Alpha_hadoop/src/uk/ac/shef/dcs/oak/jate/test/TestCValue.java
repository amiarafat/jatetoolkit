package uk.ac.shef.dcs.oak.jate.test;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderTermNest;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureTermNest;
import uk.ac.shef.dcs.oak.jate.core.feature.TermVariantsUpdater;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;

public class TestCValue extends Mapper<Text, Text, Text, Text> {

	private static final Log log = LogFactory.getLog(TestCValue.class);

	public void map(Text key, Text value, Context context) {
		byte[] fileContent = value.getBytes();
		String fileContentString = new String(fileContent);
		log.debug("file contents " + fileContentString);
		try {

			System.out.println("Started " + TestCValue.class + "at: "
					+ new Date()
					+ "... For detailed progress see log file jate.log.");

			// creates instances of required processors and resources

			// stop word list
			StopList stop = new StopList(true);

			// lemmatizer
			Lemmatizer lemmatizer = new Lemmatizer();

			// noun phrase extractor
			CandidateTermExtractor npextractor = new NounPhraseExtractorOpenNLP(
					stop, lemmatizer);

			// counters
			TermFreqCounter npcounter = new TermFreqCounter();
			WordCounter wordcounter = new WordCounter();

			// create global resource index builder, which indexes global
			// resources, such as documents and terms and their
			// relations
			GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
			// build the global resource index
			GlobalIndexMem termDocIndex = builder.build(new CorpusImpl(
					fileContentString), npextractor);

			/* newly added for improving frequency count calculation: begins */

			TermVariantsUpdater update = new TermVariantsUpdater(termDocIndex,
					stop, lemmatizer);

			GlobalIndexMem termIndex = update.updateVariants();

			FeatureCorpusTermFrequency termCorpusFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(termIndex);

			/* newly added for improving frequency count calculation: ends */

			// build a feature store required by the tfidf algorithm, using the
			// processors instantiated above
			/*
			 * FeatureCorpusTermFrequency termCorpusFreq = new
			 * FeatureBuilderCorpusTermFrequency(npcounter, wordcounter,
			 * lemmatizer).build(termDocIndex); FeatureTermNest termNest = new
			 * FeatureBuilderTermNest().build(termDocIndex);
			 */
			FeatureTermNest termNest = new FeatureBuilderTermNest()
					.build(termIndex);

			AlgorithmTester tester = new AlgorithmTester();
			tester.registerAlgorithm(new CValueAlgorithm(),
					new CValueFeatureWrapper(termCorpusFreq, termNest));
			// tester.execute(termDocIndex, path_to_output);
			tester.execute(termIndex, context);
			System.out.println("Ended at: " + new Date());
		} catch (IOException e) {
			log.error("Got an IOException", e);
		} catch (InterruptedException e) {
			log.error("Got an InterruptedException", e);
		} catch (JATEException e) {
			log.error("Got a JateException", e);
		}
		System.out.println("Ended at: " + new Date());
	}

	public static AlgorithmTester CValueTester(String[] args)
			throws IOException, JATEException {

		String path_to_corpus = args[0];
		String path_to_output = args[1];
		if (args.length < 2) {
			System.out
					.println("Usage: java TestCValue [path_to_corpus] [output_folder]");
		} else {
			System.out.println("Started " + TestCValue.class + "at: "
					+ new Date()
					+ "... For detailed progress see log file jate.log.");

			// creates instances of required processors and resources

			// stop word list
			StopList stop = new StopList(true);

			// lemmatizer
			Lemmatizer lemmatizer = new Lemmatizer();

			// noun phrase extractor
			CandidateTermExtractor npextractor = new NounPhraseExtractorOpenNLP(
					stop, lemmatizer);

			// counters
			TermFreqCounter npcounter = new TermFreqCounter();
			WordCounter wordcounter = new WordCounter();

			// create global resource index builder, which indexes global
			// resources, such as documents and terms and their
			// relations
			GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
			// build the global resource index
			GlobalIndexMem termDocIndex = builder.build(new CorpusImpl(
					path_to_corpus), npextractor);

			/* newly added for improving frequency count calculation: begins */

			TermVariantsUpdater update = new TermVariantsUpdater(termDocIndex,
					stop, lemmatizer);

			GlobalIndexMem termIndex = update.updateVariants();

			FeatureCorpusTermFrequency termCorpusFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(termIndex);

			/* newly added for improving frequency count calculation: ends */

			// build a feature store required by the tfidf algorithm, using the
			// processors instantiated above
			/*
			 * FeatureCorpusTermFrequency termCorpusFreq = new
			 * FeatureBuilderCorpusTermFrequency(npcounter, wordcounter,
			 * lemmatizer).build(termDocIndex); FeatureTermNest termNest = new
			 * FeatureBuilderTermNest().build(termDocIndex);
			 */
			FeatureTermNest termNest = new FeatureBuilderTermNest()
					.build(termIndex);

			AlgorithmTester tester = new AlgorithmTester();
			tester.registerAlgorithm(new CValueAlgorithm(),
					new CValueFeatureWrapper(termCorpusFreq, termNest));
			// tester.execute(termDocIndex, path_to_output);
			tester.execute(termIndex, path_to_output);
			System.out.println("Ended at: " + new Date());
			return tester;
		}
		return null;
	}


	public static void main(String[] args) throws IOException, JATEException {
		
		TestCValueJob cValueJob =new TestCValueJob();
		try {
			cValueJob.runCValueJob(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
