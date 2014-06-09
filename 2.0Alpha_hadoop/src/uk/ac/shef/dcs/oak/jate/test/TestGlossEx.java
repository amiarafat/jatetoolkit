package uk.ac.shef.dcs.oak.jate.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.oak.jate.core.algorithm.AbstractFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.Algorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.FrequencyAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.FrequencyFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.GlossExAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.GlossExFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.core.npextractor.WordExtractor;
import uk.ac.shef.dcs.oak.jate.io.ResultWriter2File;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.model.Term;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;
import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestGlossEx extends Mapper<Text, Text, Text, Text> {

	private static final Log log = LogFactory.getLog(TestGlossEx.class);

	public void map(Text key, Text value, Context context) {
		byte[] fileContent = value.getBytes();
		String fileContentString = new String(fileContent);
		log.debug("file contents " + fileContentString);
		try {

			System.out.println("Started " + TestGlossEx.class + "at: "
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
					fileContentString), npextractor);
			// build the global resource index of words
			GlobalIndexMem wordDocIndex = builder.build(new CorpusImpl(
					fileContentString), wordextractor);

			// build a feature store required by the termex algorithm, using the
			// processors instantiated above
			FeatureCorpusTermFrequency termCorpusFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(termDocIndex);
			FeatureCorpusTermFrequency wordFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(wordDocIndex);
			FeatureRefCorpusTermFrequency bncRef = new FeatureBuilderRefCorpusTermFrequency(
					fileContentString).build(null);

			AlgorithmTester tester = new AlgorithmTester();
			tester.registerAlgorithm(new GlossExAlgorithm(),
					new GlossExFeatureWrapper(termCorpusFreq, wordFreq, bncRef));
			tester.execute(termDocIndex, context);
		} catch (IOException e) {
			log.error("Got an IOException", e);
		} catch (InterruptedException e) {
			log.error("Got an InterruptedException", e);
		} catch (JATEException e) {
			log.error("Got a JateException", e);
		}
		System.out.println("Ended at: " + new Date());
	}

	public static void main(String[] args) throws IOException, JATEException {

		TestGlossExJob glossExJob = new TestGlossExJob();
		try {
			glossExJob.runGlossExJob(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
