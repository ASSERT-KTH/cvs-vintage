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
package org.columba.mail.imap.parser;

import org.columba.mail.imap.IMAPResponse;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MimePartTreeParser {

	public static MimePartTree parse(IMAPResponse[] responses) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < responses.length - 1; i++) {
			if (responses[i] == null)
				continue;

			buf.append(responses[i].getSource() + "\n");
		}

		MimePartTree mptree =
			parseBodyStructure(buf.toString());

		return mptree;
	}

	public static MimePartTree parseBodyStructure(String input) {
		int start = input.indexOf("BODYSTRUCTURE");

		if (start == -1) {
			System.out.println("String from Server / not expected: " + input);
			return null;
		}

		int openParenthesis = input.indexOf("(", start);

		String bodystructure =
			input.substring(
				openParenthesis + 1,
				BodystructureTokenizer.getClosingParenthesis(
					input,
					openParenthesis));

		return new MimePartTree(parseBS(bodystructure));
	}

	protected static MimePart parseBS(String input) {
		MimePart result;

		if (input.charAt(0) == '(') {
			result = new MimePart(new MimeHeader("multipart", null));

			BodystructureTokenizer tokenizer =
				new BodystructureTokenizer(input);
			BodystructureTokenizer subtokenizer;

			Item nextItem = tokenizer.getNextItem();
			Item valueItem;

			while (nextItem != null) {
				if (nextItem.getType() != Item.PARENTHESIS)
					break;

				result.addChild(parseBS((String) nextItem.getValue()));
				nextItem = tokenizer.getNextItem();
			}

			// Parse the Rest of the Multipart

			// Subtype / nextItem is already filled from break in while-block
			result.getHeader().contentSubtype =
				((String) nextItem.getValue()).toLowerCase();

			// Search for any ContentParameters
			nextItem = tokenizer.getNextItem();
			if (nextItem.getType() == Item.PARENTHESIS) {
				subtokenizer =
					new BodystructureTokenizer((String) nextItem.getValue());

				nextItem = subtokenizer.getNextItem();
				while (nextItem != null) {
					valueItem = subtokenizer.getNextItem();

					result.getHeader().putContentParameter(
						((String) nextItem.getValue()).toLowerCase(),
						(String) valueItem.getValue());

					nextItem = subtokenizer.getNextItem();
				}
			}

			// ID
			nextItem = tokenizer.getNextItem();
			if (nextItem == null)
				return result;
			if (nextItem.getType() == Item.STRING)
				result.getHeader().contentID =
					((String) nextItem.getValue()).toLowerCase();

			// Description
			nextItem = tokenizer.getNextItem();
			if (nextItem == null)
				return result;
			if (nextItem.getType() == Item.STRING)
				result.getHeader().contentDescription =
					((String) nextItem.getValue()).toLowerCase();

		} else {

			result = parseMimeStructure(input);
		}

		return result;
	}

	private static ColumbaHeader parseEnvelope(String envelope) {

		ColumbaHeader result = new ColumbaHeader();

		BodystructureTokenizer tokenizer = new BodystructureTokenizer(envelope);
		Item nextItem;

		// Date
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			result.set("Date", (String) nextItem.getValue());

		// Subject
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			result.set("Subject", (String) nextItem.getValue());

		// From
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.PARENTHESIS);

		// Sender
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.PARENTHESIS);

		// Reply-To
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.PARENTHESIS);

		// To
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.PARENTHESIS);

		// Cc
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.PARENTHESIS);

		// Bcc
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.PARENTHESIS);

		// In-Reply-To
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			result.set("In-Reply-To", (String) nextItem.getValue());

		// Message-ID
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			result.set("Message-ID", (String) nextItem.getValue());

		return result;
	}

	private static MimePart parseMimeStructure(String structure) {
		MimeHeader header = new MimeHeader();
		MimePart result = new MimePart(header);
		BodystructureTokenizer tokenizer =
			new BodystructureTokenizer(structure);
		BodystructureTokenizer subtokenizer;
		Item nextItem, valueItem;

		// Content-Type    	
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			header.contentType = ((String) nextItem.getValue()).toLowerCase();

		// ContentSubtype
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			header.contentSubtype =
				((String) nextItem.getValue()).toLowerCase();

		// are there some Content Parameters ?
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.PARENTHESIS) {
			subtokenizer =
				new BodystructureTokenizer((String) nextItem.getValue());

			nextItem = subtokenizer.getNextItem();
			while (nextItem != null) {
				valueItem = subtokenizer.getNextItem();

				header.putContentParameter(
					((String) nextItem.getValue()).toLowerCase(),
					(String) valueItem.getValue());

				nextItem = subtokenizer.getNextItem();
			}
		}

		// ID
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			header.contentID = ((String) nextItem.getValue()).toLowerCase();

		// Description
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			header.contentDescription =
				((String) nextItem.getValue()).toLowerCase();

		// Encoding
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.STRING)
			header.contentTransferEncoding =
				((String) nextItem.getValue()).toLowerCase();

		// Size
		nextItem = tokenizer.getNextItem();
		if (nextItem.getType() == Item.NUMBER)
			header.size = (Integer) nextItem.getValue();

		// Is this a Message/RFC822 Part ?
		if ((header.contentType.equals("message"))
			& (header.contentSubtype.equals("rfc822"))) {

			Message subMessage = new Message();

			// Envelope

			nextItem = tokenizer.getNextItem();
			if (nextItem.getType() == Item.PARENTHESIS) {
				subMessage.setHeader(
					parseEnvelope((String) nextItem.getValue()));
			}

			// Bodystrucuture of Sub-Message

			nextItem = tokenizer.getNextItem();
			if (nextItem.getType() == Item.PARENTHESIS) {
				MimePart subStructure = parseBS((String) nextItem.getValue());
				subStructure.setParent(result);
				subMessage.setMimePartTree(new MimePartTree(subStructure));
			}

			result.setContent(subMessage);

			// Number of lines			
			nextItem = tokenizer.getNextItem();

		}
		// Is this a Text - Part ?
		else if (header.contentType.equals("text")) {
			// Number of lines
			nextItem = tokenizer.getNextItem();
			// DONT CARE
		}

		// Are there Extensions ?
		// MD5
		nextItem = tokenizer.getNextItem();
		if (nextItem == null)
			return new MimePart(header);
		// DONT CARE

		// are there some Disposition Parameters ?
		nextItem = tokenizer.getNextItem();
		if (nextItem == null)
			return new MimePart(header);

		if (nextItem.getType() == Item.STRING) {
			header.contentDisposition =
				((String) nextItem.getValue()).toLowerCase();
		} else if (nextItem.getType() == Item.PARENTHESIS) {
			subtokenizer =
				new BodystructureTokenizer((String) nextItem.getValue());

			nextItem = subtokenizer.getNextItem();
			header.contentDisposition =
				((String) nextItem.getValue()).toLowerCase();

			nextItem = subtokenizer.getNextItem();
			// Are there Disposition Parameters?
			if (nextItem.getType() == Item.PARENTHESIS) {
				subtokenizer =
					new BodystructureTokenizer((String) nextItem.getValue());

				nextItem = subtokenizer.getNextItem();

				while (nextItem != null) {
					valueItem = subtokenizer.getNextItem();

					header.putDispositionParameter(
						((String) nextItem.getValue()).toLowerCase(),
						(String) valueItem.getValue());

					nextItem = subtokenizer.getNextItem();
				}
			}
		}

		// WE DO NOT GATHER FURTHER INFORMATION

		return result;
	}

}

class BodystructureTokenizer {

	private String s;
	private Interval i;

	public BodystructureTokenizer(String s) {
		this.s = s;
		i = new Interval();
	}

	public Item getNextItem() {
		Item result = new Item();

		// Search for next Item
		i.a = i.b + 1;
		// ..but Check Bounds!!
		if (i.a >= s.length())
			return null;
		while (s.charAt(i.a) == ' ') {
			i.a++;
			if (i.a >= s.length())
				return null;
		}

		// Quoted

		if (s.charAt(i.a) == '\"') {
			i.b = s.indexOf("\"", i.a + 1);

			result.setType(Item.STRING);
			result.setValue(s.substring(i.a + 1, i.b));
		}

		// Parenthesized

		else if (s.charAt(i.a) == '(') {
			i.b = getClosingParenthesis(s, i.a);

			result.setType(Item.PARENTHESIS);
			result.setValue(s.substring(i.a + 1, i.b));
		}

		// NIL or Number

		else {
			i.b = s.indexOf(" ", i.a + 1);
			if (i.b == -1)
				i.b = s.length();

			String item = s.substring(i.a, i.b);
			i.b--;

			if (item.equals("NIL")) {
				result.setType(Item.NIL);
			} else {
				result.setValue(new Integer(item));
				result.setType(Item.NUMBER);
			}
		}

		return result;
	}

	static public int getClosingParenthesis(String s, int openPos) {
		int nextOpenPos = s.indexOf("(", openPos + 1);
		int nextClosePos = s.indexOf(")", openPos + 1);

		while ((nextOpenPos < nextClosePos) & (nextOpenPos != -1)) {
			nextClosePos = s.indexOf(")", nextClosePos + 1);
			nextOpenPos = s.indexOf("(", nextOpenPos + 1);
		}
		return nextClosePos;
	}
}

class Item {
	public static final int STRING = 0;
	public static final int PARENTHESIS = 1;
	public static final int NIL = 2;
	public static final int NUMBER = 3;

	private Object value;
	private int type;
	/**
	 * Returns the type.
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the value.
	 * @return Object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Sets the value.
	 * @param value The value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

}

class Interval {
	public int a, b;
	public int type;

	public Interval(int a, int b) {
		this.a = a;
		this.b = b;
	}

	public Interval() {
		a = -1;
		b = -1;
	}

	public void reset() {
		a = -1;
		b = -2;
	}
}
