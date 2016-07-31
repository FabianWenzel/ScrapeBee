package de.scrapebee;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.tokensregex.CoreMapSequenceMatchAction.AnnotateAction;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchRules.AnnotationMatchedFilter;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.Triple;
import edu.stanford.nlp.util.logging.StanfordRedwoodConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class NLP {

	private static AbstractSequenceClassifier<CoreLabel> germanClassifier;
	private static AbstractSequenceClassifier<CoreLabel> englishClassifier;

	/**
	 * Lädt die Classifier
	 */
	public static void init() {
		try {
			germanClassifier = CRFClassifier
					.getClassifier(SystemNames.CLASSIFIER_GERMAN);
			englishClassifier = CRFClassifier
					.getClassifier(SystemNames.CLASSIFIER_ENGLISH);
		} catch (ClassCastException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

	}
	/**
	 * Tokenizer von NLP
	 * http://nlp.stanford.edu/software/tokenizer.shtml
	 * http://www.programcreek.com/java-api-examples/index.php?api=edu.stanford.nlp.process.PTBTokenizer
	 * @param text Inhalt
	 * @param language Sprache
	 * @return Tokens
	 */
	public static List<String> tokenize(String text, String language) {
		List<String> result = new ArrayList<String>();
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(
				new StringReader(text)).tokenize();

		for (CoreLabel label : rawWords) {
			result.add(label.originalText());
		}

		return result;
	}

	/**
	 * Liefert Standort zu Inhalt anhand spezifischer Sprache
	 * @param text Inhalt
	 * @param language Sprache
	 * @return Liste mit Standorten
	 * @throws Exception
	 */
	public static List<String> getLocation(String text, String language)
			throws Exception {

		AbstractSequenceClassifier<CoreLabel> classifier;

		if (language.equals(SystemNames.LANG_DE)) {
			classifier = germanClassifier;
		} else {
			classifier = englishClassifier;
		}

		List<String> result = new ArrayList<String>();

		

		// This prints out all the details of what is stored for each token
		int i = 0;
		for (List<CoreLabel> lcl : classifier.classify(text)) {
			for (CoreLabel cl : lcl) {
				if (cl.getString(CoreAnnotations.AnswerAnnotation.class,
						"I-LOC").contains("I-LOC")) {
					System.out.print(i++ + ": ");
					System.out.print(cl.getString(
							CoreAnnotations.AnswerAnnotation.class, "I-LOC")
							+ " ");
					System.out.println(cl.originalText());
					result.add(cl.originalText());
				}

			}
		}

		System.out.println("---");
		return result;
	}
}
