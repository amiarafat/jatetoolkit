package uk.ac.shef.dcs.oak.jate.core.nlptools;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import uk.ac.shef.dcs.oak.jate.JATEProperties;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * A singleton class which controls creation and dispatches of OpenNLP tools
 *
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */


public class NLPToolsControllerOpenNLP {

	private static NLPToolsControllerOpenNLP _ref;
	private POSTagger _posTagger;
	private Chunker _npChunker;
	private SentenceDetector _sentDetect;
	private Tokenizer _tokenizer;

	private NLPToolsControllerOpenNLP() throws IOException {
        FileInputStream posStream = new FileInputStream(System.getProperty("user.home") + "//"
				+ JATEProperties.getInstance().getNLPPath()+"/en-pos-maxent.bin");
		POSModel posModel = new POSModel(posStream);
		_posTagger = new POSTaggerME(posModel);
		posStream.close();

        FileInputStream chunkerStream = new FileInputStream(System.getProperty("user.home") + "//"
				+ JATEProperties.getInstance().getNLPPath()+"/en-chunker.bin");
		ChunkerModel chunkerModel = new ChunkerModel(chunkerStream);
		_npChunker = new ChunkerME(chunkerModel);
		chunkerStream.close();

        FileInputStream tokenizerStream = new FileInputStream(System.getProperty("user.home") + "//"
				+ JATEProperties.getInstance().getNLPPath()+"/en-token.bin");
		TokenizerModel tokenizerModel = new TokenizerModel(tokenizerStream);
		_tokenizer = new TokenizerME(tokenizerModel);
		tokenizerStream.close();

        FileInputStream sentenceStream = new FileInputStream(System.getProperty("user.home") + "//"
				+ JATEProperties.getInstance().getNLPPath()+"/en-sent.bin");
		SentenceModel sentModel = new SentenceModel(sentenceStream);
        _sentDetect = new SentenceDetectorME(sentModel);
        sentenceStream.close();

	}

	public static NLPToolsControllerOpenNLP getInstance() throws IOException {
		if(_ref ==null) _ref=new NLPToolsControllerOpenNLP();
		return _ref;
	}

	public Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

	public POSTagger getPosTagger() {
		return _posTagger;
	}

	public Chunker getPhraseChunker() {
		return _npChunker;
	}

	public SentenceDetector getSentenceSplitter() {
		return _sentDetect;
	}

	public Tokenizer getTokeniser() {
		return _tokenizer;
	}
}
