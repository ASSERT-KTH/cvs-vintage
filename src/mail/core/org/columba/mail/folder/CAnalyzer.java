package org.columba.mail.folder;

import java.io.Reader;
import java.util.Hashtable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CAnalyzer extends Analyzer {


	/**
	 * Constructor for CAnalyzer.
	 */
	public CAnalyzer() {
		super();
	}
	/*
	 * An array containing some common words that
	 * are not usually useful for searching.
	 */
	private static final String[] STOP_WORDS =
		{
			"a",
			"and",
			"are",
			"as",
			"at",
			"be",
			"but",
			"by",
			"for",
			"if",
			"in",
			"into",
			"is",
			"it",
			"no",
			"not",
			"of",
			"on",
			"or",
			"s",
			"such",
			"t",
			"that",
			"the",
			"their",
			"then",
			"there",
			"these",
			"they",
			"this",
			"to",
			"was",
			"will",
			"with" };

	/*
	 * Stop table
	 */
	final static private Hashtable stopTable =
		StopFilter.makeStopTable(STOP_WORDS);

	/**
	 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.io.Reader)
	 * @deprecated
	 */
	public TokenStream tokenStream(String field, Reader reader) {
		TokenStream result = new RegExpTokenizer(reader);
		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, stopTable);

		return result;
	}

	/**
	 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.io.Reader)
	 * @deprecated
	 */
	public TokenStream tokenStream(Reader reader) {
		return tokenStream("null",reader);
	}

}
