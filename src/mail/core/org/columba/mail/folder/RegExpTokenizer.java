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
package org.columba.mail.folder;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RegExpTokenizer extends Tokenizer {

	private static final String fileRegexp = "\\b\\w{2,}\\b";

	private Pattern pattern;
	private PatternMatcher matcher;

	private PatternMatcherInput input;

	/**
	 * Constructor for RegExpTokenizer.
	 */
	public RegExpTokenizer(Reader reader) {
		super();

		try {
			pattern = new Perl5Compiler().compile(fileRegexp);
		} catch (MalformedPatternException e) {
			e.printStackTrace();
		}
		matcher = new Perl5Matcher();

		try {
			StringBuffer buffer = new StringBuffer();

			char[] charBuffer = new char[100];

			int read = reader.read(charBuffer);

			while (read != -1) {
				buffer.append(charBuffer, 0, read);
				read = reader.read(charBuffer);
			}

			input = new PatternMatcherInput(buffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @see org.apache.lucene.analysis.TokenStream#next()
	 */
	public Token next() throws IOException {
		if (matcher.contains(input, pattern)) {
			return new Token(input.match(), 0, 0);
		}

		return null;
	}

	/**
	 * @see org.apache.lucene.analysis.TokenStream#close()
	 */
	public void close() throws IOException {
	}

}
