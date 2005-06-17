package org.columba.mail.gui.message.viewer;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;

/**
 * Provides text processing helper methods for the text viewer component.
 *  
 * @author fdietz
 */
public class MessageParser {

	private static DocumentParser parser = new DocumentParser();
	
	/**
	 * @param bodyText
	 * @throws Exception
	 */
	public static String transformTextToHTML(String bodyText, String css, boolean enableSmilies) throws Exception {
		String body = null;

		// substitute special characters like:
		// <,>,&,\t,\n
		body = HtmlParser.substituteSpecialCharacters(bodyText);

		// parse for urls and substite with HTML-code
		body = HtmlParser.substituteURL(body);

		// parse for email addresses and substite with HTML-code
		body = HtmlParser.substituteEmailAddress(body);

		// parse for quotings and color the darkgray
		body = parser.markQuotings(body);

		// add smilies
		if (enableSmilies == true) {
			body = parser.addSmilies(body);
		}

		// encapsulate bodytext in html-code
		body = transformToHTML(new StringBuffer(body), css);

		return body;
	}

	/*
	 * 
	 * encapsulate bodytext in HTML code
	 * 
	 */
	private static String transformToHTML(StringBuffer buf, String css) {
		// prepend
		buf.insert(0, "<HTML><HEAD>" + css
				+ "</HEAD><BODY class=\"bodytext\"><P>");

		// append
		buf.append("</P></BODY></HTML>");

		return buf.toString();
	}
	
	/**
	 * @param bodyPart
	 * @param bodyStream
	 * @return
	 */
	public static InputStream decodeBodyStream(Charset charset, MimePart bodyPart,
			InputStream bodyStream) throws Exception {

		// default encoding is plain
		int encoding = MimeHeader.PLAIN;

		if (bodyPart != null) {
			encoding = bodyPart.getHeader().getContentTransferEncoding();
		}

		switch (encoding) {
		case MimeHeader.QUOTED_PRINTABLE: {
			bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

			break;
		}

		case MimeHeader.BASE64: {
			bodyStream = new Base64DecoderInputStream(bodyStream);

			break;
		}
		}

		bodyStream = new CharsetDecoderInputStream(bodyStream, charset);

		return bodyStream;
	}

	/**
	 * @param mediator
	 * @param bodyPart
	 * @return
	 */
	public static Charset extractCharset(Charset charset, MimePart bodyPart) {

		// no charset specified -> automatic mode
		// -> try to determine charset based on content parameter
		if (charset == null) {
			String charsetName = null;

			if (bodyPart != null) {
				charsetName = bodyPart.getHeader().getContentParameter(
						"charset");
			}

			if (charsetName == null) {
				// There is no charset info -> the default system charset is
				// used
				charsetName = System.getProperty("file.encoding");
				charset = Charset.forName(charsetName);
			} else {
				try {
					charset = Charset.forName(charsetName);
				} catch (UnsupportedCharsetException e) {
					charsetName = System.getProperty("file.encoding");
					charset = Charset.forName(charsetName);
				} catch (IllegalCharsetNameException e) {
					charsetName = System.getProperty("file.encoding");
					charset = Charset.forName(charsetName);
				}
			}

			// ((CharsetOwnerInterface) mediator).setCharset(charset);

		}
		return charset;
	}

	
}
