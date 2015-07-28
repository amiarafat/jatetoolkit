# Introduction #
This guide introduces how to use JATE to:
  * extract terms from corpus using pre-implemented algorithms. See [Extracting terms using Jate](JATEIntro#Extracting_terms_using_JATE.md).
  * extend JATE by implementing your own algorithms. See [Implementing new algorithms](JATEIntro#Implementing_new_algorithms.md).

JATE (Java Automatic Term Extraction) is a toolkit for developing and experimenting Automatic Term Extractions/Recognition algorithms in Java. The motivation of this toolkit is to make available several state-of-the-art algorithms of ATE/ATR to developers and users of ATE/ATR, and encourage developers of ATE/ATR methods to develop their methods under a uniform framework to enable comparative studies.

JATE is implemented based on the common ground of most ATE/ATR algorithms, which typically follows the steps as below:

  * Extracting candidate terms from a corpus using linguistic tools
  * Extracting statistical features of candidates from the corpus
  * Apply ATE/ATR algorithms to score the domain representativeness of candidate terms based on their statistical features. The scores give an indication of the likelihood of a candidate term being a good domain specific term.

Following this, JATE can be divided into three main components:
  * Classes and utilities for extracting candidate terms
  * Classes for representing and building candidates’ features
  * Classes implementing state-of-the-art algorithms using the features

Currently JATEv1.1 has implemented 8 algorithms listed below:
  * Simple term frequency – the score of a candidate term is simply the frequency of that candidate noted in the corpus
  * TF.IDF – the score of a candidate term is the TF.IDF score of that candidate in the corpus
  * Weirdness – see Ahmad et a.l, 1999, Surrey Participation in TREC8: Weirdness Indexing for Logical Document Extrapolation
  * C-value – see Frantzi et al., 2000, Automatic recognition of multi-word terms:. the C-value/NC-value method
  * GlossEx – see Kozakov, et al., 2004, Glossary extraction and utilization in the information search and delivery system for IBM Technical Support
  * TermEx – see Sclano et al., 2007, TermExtractor: a Web application to learn the shared terminology of emergent web communities
  * RIDF - Residual IDF, see Church, K. and Gale, W. 1995a. Inverse Document Frequency (IDF): A Measure of Deviation from Poisson. In Proceedings of the 3rd Workshop on Very Large Corpora. Cambridge, Massachusetts, USA, pp.121-30.
  * Average Term Frequency in Corpus - calculated by dividing the total frequency of a term in a corpus by its document frequency.

**Please note that we do not claim to have replicated each ATE/ATR method described in the reference**. We have made every effort to replicate the computation algorithm; however, you should expect to obtain slightly different results since we may have used different linguistic tools, and reference resources used by some algorithms.


# Extracting terms using JATE #

  * Configuration
**Before running a test, make sure you reset the configurations in the jate.properties file in your class path**. There are a couple of properties that point particular folders in the JATE distribution. Make sure they are valid on your file system.
  * An example: uk.ac.shef.dcs.oak.jate.test.AlgorithmTester
The "main" method in the testing class "uk.ac.shef.dcs.oak.jate.test.AlgorithmTester" contains explanations of how to use existing JATE algorithms for ATE/ATR. The code of this method is copied below, provided with more detailed explanations:
```
public static void main(String[] args) {
   //[corpus_path]: input folder containing documents (raw text) you want to process
   //[reference_corpus_path]: required *only* by some algorithms (GlossEx, Weirdness and TermEx). Point to a single file that contains
   //              frequency statistics of words found in a reference corpus. For the requirement format, see an example in the distribution:
   //              "nlp_resources/bnc_unifrqs.normal".
   //[output_folder]: where you want the output to be written to.
   if (args.length < 3) System.out.println("Usage: java AlgorithmTester [corpus_path] [reference_corpus_path] [output_folder]");
   
   else {
      try {
         System.out.println(new Date());

         //##########################################################
         //#         Step 1. Extract candidate terms/words from     #
         //#         documents, and index the terms/words, docs     #
         //#         and their relations (occur-in, containing)     #
         //##########################################################

         //stop words and lemmatizer are used for processing the extraction of candidate terms
         //(used by CandidateTermExtractor, see below)
	 StopList stop = new StopList(true);
	 Lemmatizer lemmatizer = new Lemmatizer();

         //Use an instance of CandidateTermExtractor to extract candidate terms. 
         //Three types of CandidateTermExtractorare implemented. Choose the most appropriate one for your needs.
         //1. An OpenNLP noun phrase extractor that extracts noun phrases as candidate terms. If terms in your
         //   domain are mostly NPs, use this one. If OpenNLP isnt suitable for your domain, you may plug-in
         //   your own NLP tools.
 	 CandidateTermExtractor npextractor = new NounPhraseExtractorOpenNLP(stop, lemmatizer);

         //2. A generic N-gram extractor that extracts n(default is 5, see the property file) grams. This doesn't
         //   depend on tagging or parsing, but generates more much more candidates than NounPhraseExtractorOpenNLP
         //CandidateTermExtractor npextractor = new NGramExtractor(stop, lemmatizer);
         
         //3. A word extractor that extracts single words as candidate terms. If you task extracts single words as terms,
         //   you could use this instead. 
         //CandidateTermExtractor wordextractor = new WordExtractor(stop, lemmatizer);

         //An instance of WordExtractor is also needed to build word frequency data, which are required by some algorithms.
         //See javadoc for regarding the parameters for the constructor.
	 CandidateTermExtractor wordextractor = new WordExtractor(stop, lemmatizer, false, 1);

         //Next start building candidate term and word data and index them.
         GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
	 GlobalIndexMem wordDocIndex = builder.build(new CorpusImpl(args[0]), wordextractor);
	 GlobalIndexMem termDocIndex = builder.build(new CorpusImpl(args[0]), npextractor);

         //Optionally, you can save the index data as HSQL databases on file system
         //GlobalIndexWriterHSQL.persist(wordDocIndex, "D:/output/worddb");
         //GlobalIndexWriterHSQL.persist(termDocIndex, "D:/output/termdb");


         //##########################################################
         //#         Step 2. Build various statistical features     #
         //#         used by term extraction algorithms. This will  #
         //#         need the indexes built above, and counting the #
         //#         frequencies of terms                           #
         //##########################################################

         //A WordCounter instance is required to count number of words in corpora/documents. This
         //will be used as features by some algorithms.
         WordCounter wordcounter = new WordCounter();

         //Next we need to count frequencies of candidate terms. There are different kinds of term frequency
         // data that are required by different algorithms. In JATE these are called (statistical) "features",
         // see "uk.ac.shef.dcs.oak.jate.core.feature.AbstractFeature" and its implementations.
         //These features must be built using a feature builder, see "uk.ac.shef.dcs.oak.jate.core.feature.AbstractFeatureBuilder"
         // and its simplementations. Generally, each kind of "AbstractFeature" will have an implementation of the
         // "AbstractFeatureBuilder". 
         //The building of these features typically requires counting term frequencies in certain ways, which is 
         // a computationally extensive process. Two alternative sets of "AbstractFeatureBuilder" classes are 
         // implemented. The first set contains classes named as "FeatureBuilderXYZMultiThread", which splits the corpus
         // into segments and run several threads are run in parallel then aggregate results; the second set contain
         // classes named as "FeatureBuilderXYZ" which are the single threaded options. 
         //Also note that counting in JATE is case-sensitive: JATE creats a one-to-many mapping from canonical term forms to
         // variants found in a corpus. When counting, each variant is searched in the corpus, and the frequency adds up to a sum
         // as the total frequency for the canonical form. Scoring and ranking are based on the canonical forms also. 

         /* Option 1: Due to use of multi-threading, this can significantly occupy your CPU and memory resources. It is
            better to use this way on dedicated server machines, and only for very large corpus.

            NOTE: YOU DO NOT need all the following features for every single algorithm. This class creates all for the purpose
            of showcase all algorithms.
          */
         /*
         FeatureCorpusTermFrequency wordFreq =
            new FeatureBuilderCorpusTermFrequencyMultiThread(wordcounter, lemmatizer).build(wordDocIndex);
	 FeatureDocumentTermFrequency termDocFreq =
            new FeatureBuilderDocumentTermFrequencyMultiThread(wordcounter, lemmatizer).build(termDocIndex);
	 FeatureTermNest termNest =
            new FeatureBuilderTermNestMultiThread().build(termDocIndex);
	 FeatureRefCorpusTermFrequency bncRef =
	    new FeatureBuilderRefCorpusTermFrequency(args[1]).build(null);
	 FeatureCorpusTermFrequency termCorpusFreq =
            new FeatureBuilderCorpusTermFrequencyMultiThread(wordcounter, lemmatizer).build(termDocIndex);
         */
 
         /* Option #2 */                
         //If you use single-threaded feature builders, you need to create ONE instance of TermFreqCounter.
         //Before with multi-threaded feature builders, one instance of TermFreqCounter is created for each 
         //thread.
         TermFreqCounter npcounter = new TermFreqCounter();
         FeatureCorpusTermFrequency wordFreq =
		new FeatureBuilderCorpusTermFrequency(npcounter, wordcounter, lemmatizer).build(wordDocIndex);
  	 FeatureDocumentTermFrequency termDocFreq =
		new FeatureBuilderDocumentTermFrequency(npcounter, wordcounter, lemmatizer).build(termDocIndex);
	 FeatureTermNest termNest =
		new FeatureBuilderTermNest().build(termDocIndex);
	 FeatureRefCorpusTermFrequency bncRef =
		new FeatureBuilderRefCorpusTermFrequency(args[1]).build(null);
	 FeatureCorpusTermFrequency termCorpusFreq =
		new FeatureBuilderCorpusTermFrequency(npcounter, wordcounter, lemmatizer).build(termDocIndex);
         


         //##########################################################
         //#         Step 3. For each algorithm you want to test    #
         //#         create an instance of the algorithm class,     #
         //#         and also an instance of its feature wrapper.   #
         //##########################################################


	 AlgorithmTester tester = new AlgorithmTester();

         //NOTE that each algorithm will need its own "FeatureWrapper" (in uk.ac.shef.dcs.oak.jate.core.algorithm)
         //Each feature wrapper may require different kinds of "AbstractFeature". You can find details of these in
         //the javadoc. The purpose of "FeatureWrapper" is to encapsulate the underlying feature (stores) and provide
         //access to only the features required by the corresponding algorithm

 	 tester.registerAlgorithm(new TFIDFAlgorithm(), new TFIDFFeatureWrapper(termCorpusFreq));
	 tester.registerAlgorithm(new GlossExAlgorithm(), new GlossExFeatureWrapper(termCorpusFreq, wordFreq, bncRef));
	 tester.registerAlgorithm(new WeirdnessAlgorithm(), new WeirdnessFeatureWrapper(wordFreq, termCorpusFreq, bncRef));
	 tester.registerAlgorithm(new CValueAlgorithm(), new CValueFeatureWrapper(termCorpusFreq, termNest));
	 tester.registerAlgorithm(new TermExAlgorithm(), new TermExFeatureWrapper(termDocFreq, wordFreq, bncRef));
         tester.registerAlgorithm(new RIDFAlgorithm(), new RIDFFeatureWrapper(termCorpusFreq));
         tester.registerAlgorithm(new AverageCorpusTFAlgorithm(), new AverageCorpusTFFeatureWrapper(termCorpusFreq));
         tester.registerAlgorithm(new FrequencyAlgorithm(), new FrequencyFeatureWrapper(termCorpusFreq));

	 tester.execute(termDocIndex, args[2]);
	 System.out.println(new Date());

      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
}

```

  * The output
JATE will generate output in the output folder provided by you. For each algorithm, a ".txt" file named after the algorithm. The file lists one term canonical form per line, in the following form:

"term | var1 | var2      1.1"

where "term" is the canonical term form; "var1 | var2" are various forms of the term found in the corpus; "1.1" is the score given by the algorithm.


# Implementing new algorithms #

**If you haven't learnt how to use pre-implemented algorithms in JATE for ATE/ATR please read the previous section**. It explains the basic principles of ATE/ATR adopted in JATE and is essential to understanding how to build your own algorithms.

As mentioned above, ATE/ATR in JATE depends on three components:
  1. Extracting candidate terms from documents in a corpus and index them;
  1. Build statistical features of candidate terms required by an algorithm;
  1. Execute the algorithm with the features, which computes a score for each candidate term and rank them.

To implement your own algorithm, you also need to work on these components. Depending on your needs, you may find that you only need to extend some of these components. Here are some general guidelines:

  * Extracting candidate terms from documents in a corpus and index them
If you need to extract candidate terms in different ways, create a new class that extends **uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor** and use it in your program. You may find example implementations in the following three classes: **NGramExtractor, NounPhraseExtractorOpenNLP, WordExtractor**.

  * Build statistical features of candidate terms required by an algorithm
If your algorithm requires new kinds of features that are not yet implemented, you need to develop new subclasses of **uk.ac.shef.dcs.oak.jate.core.feature.AbstractFeature** and corresponding feature builder classes that extends **uk.ac.shef.dcs.oak.jate.core.feature.AbstractFeatureBuilder**. Example implementations are available in that package.

  * New algorithms
Finally you need to implement new algorithms, and corresponding feature wrapper for your algorithm class. Your algorithm should be a subclass of **uk.ac.shef.dcs.oak.jate.core.algorithm.Algorithm** and your feature wrapper should be a subclass of "AbstractFeatureWrapper" in the same package. See existing implementations for examples.