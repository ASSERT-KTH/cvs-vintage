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

package org.columba.mail.coder;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.Interval;

public class EncodedWordDecoder {

	private Decoder qDecoder, bDecoder;

	public EncodedWordDecoder() {
		qDecoder = new QuotedPrintableDecoder();
		bDecoder = new Base64Decoder();
	}

	public String decode(String input) {
		StringBuffer output = new StringBuffer();
		Interval nextItem;

		int end = 0;

		try {
			nextItem = getNextEncodedWord( input, 0 );
			if( nextItem == null) return input;

			while (nextItem != null) {
				output.append(input.substring(end, nextItem.a));
				output.append(decodeEncodedWord(input.substring(nextItem.a, nextItem.b)));
				end = nextItem.b;
				nextItem = getNextEncodedWord( input, end );
			}

			if (end < input.length())
				output.append(input.substring(end));

			return output.toString();
		} catch (UnsupportedEncodingException e ) {
			ColumbaLogger.log.debug( e );
		}		
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(input);
		}

		return input;
	}

	private Interval getNextEncodedWord(String input, int startPos) {
		int end;
		int markPos;
		int	start = input.indexOf("=?", startPos);

		if(start == -1 ) return null;

		markPos = input.indexOf("?", start + 2); // second ?
		if (markPos == -1) return null;

		markPos = input.indexOf("?", markPos + 1); // third ?
		if (markPos == -1) return null;
		
		end = input.indexOf("?=", markPos + 1);
		if (end == -1) return null;

		return new Interval( start,end+2 );
	}

	private String decodeEncodedWord(String encodedWord)
		throws UnsupportedEncodingException {
		String charset;
		char encoding;
		String codedWord;
		StringTokenizer items;

		items = new StringTokenizer(encodedWord, "?");

		if (items.countTokens() < 4)
			return encodedWord;

		items.nextToken(); // The beginning "="

		charset = items.nextToken().toLowerCase();
		encoding = items.nextToken().toLowerCase().charAt(0);
		codedWord = items.nextToken();

		switch (encoding) {
			case ('q') :
				{
					return qDecoder.decode(
						codedWord.replace('_', ' '),
						charset);
				}
			case ('b') :
				{
					return bDecoder.decode(codedWord, charset);
				}
		}

		return encodedWord;
	}

}
