package uk.ac.shef.dcs.oak.jate.test;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.WeirdnessAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.WeirdnessFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.core.npextractor.WordExtractor;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;

/**
 * Author: Ziqi Zhang Organisation: Department of Computer Science, University
 * of Sheffield Email: z.zhang@dcs.shef.ac.uk Time: 13:58:47
 */

public class TestWeirdness extends Mapper<Text, Text, Text, Text> {

	private static final Log log = LogFactory.getLog(TestWeirdness.class);

	public void map(Text key, Text value, Context context) {
		FileSplit inputSplit = (FileSplit) context.getInputSplit();

		Path hdfsPath = inputSplit.getPath();

		Path parent = hdfsPath.getParent();
		Configuration conf = context.getConfiguration();
		String refCorpusPath = conf.get("refCorpusPath");
		try {

			System.out.println("Started " + TestWeirdness.class + "at: "
					+ new Date()
					+ "... For detailed progress see log file jate.log.");

			// creates instances of required processors and resources

			// stop word list
			StopList stop = new StopList(true);

			// lemmatiser
			Lemmatizer lemmatizer = new Lemmatizer();

			// noun phrase extractor, which produces candidate terms as noun
			// phrases
			CandidateTermExtractor npextractor = new NounPhraseExtractorOpenNLP(
					stop, lemmatizer);
			// we also need a word extractor which produces each single word
			// found in the corpus as required by the algorithm
			CandidateTermExtractor wordextractor = new WordExtractor(stop,
					lemmatizer);

			// counters
			TermFreqCounter npcounter = new TermFreqCounter();
			WordCounter wordcounter = new WordCounter();

			// create global resource index builder, which indexes global
			// resources, such as documents and terms and their
			// relations
			GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
			// build the global resource index of noun phrases
			GlobalIndexMem termDocIndex = builder.build(new CorpusImpl(
					parent.toString()), npextractor);
			// build the global resource index of words
			GlobalIndexMem wordDocIndex = builder.build(new CorpusImpl(
					parent.toString()), wordextractor);

			// build a feature store required by the tfidf algorithm, using the
			// processors instantiated above
			FeatureCorpusTermFrequency termCorpusFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(termDocIndex);
			FeatureCorpusTermFrequency wordFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(wordDocIndex);
			FeatureRefCorpusTermFrequency bncRef = new FeatureBuilderRefCorpusTermFrequency(
					refCorpusPath).build(null);

			AlgorithmTester tester = new AlgorithmTester();
			tester.registerAlgorithm(new WeirdnessAlgorithm(),
					new WeirdnessFeatureWrapper(wordFreq, termCorpusFreq,
							bncRef));
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

		TestWeirdnessJob weirdnessJob = new TestWeirdnessJob();
		try {
			weirdnessJob.runWeirdnessJob(args[0], args[1], args[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
