// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

/** Document the purpose of this class.
 *
 * @version 1.0
 * @author Timo Stich
 */

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
