//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.folder.search;

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
