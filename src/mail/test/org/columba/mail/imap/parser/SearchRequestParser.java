/*
 * Created on 06.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

import junit.framework.TestCase;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.imap.SearchRequestBuilder;
import org.columba.mail.imap.protocol.ArgumentWriter;
import org.columba.mail.imap.protocol.Arguments;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SearchRequestParser extends TestCase {

	/**
	 * Constructor for SearchRequestParser.
	 * @param arg0
	 */
	public SearchRequestParser(String arg0) {
		super(arg0);
	}

	public void testBody() throws Exception {
		FilterRule rule = new FilterRule();
		rule.setCondition("matchall");
		FilterCriteria c = rule.addEmptyCriteria();
		c.setHeaderItem("Size");
		c.setCriteria("smaller");
		c.setPattern("10");
		c = rule.addEmptyCriteria();
		c.setHeaderItem("Body");
		c.setCriteria("contains");
		c.setPattern("Grüsse 2");

		SearchRequestBuilder e = new SearchRequestBuilder();
		e.setCharset("UTF-8");
		List list = e.generateSearchArguments(rule);
		Arguments args = e.generateSearchArguments(rule, list);

		String str = null;

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		TestArgumentWriter writer = new TestArgumentWriter(byteStream);
		writer.write(args);

		String request = byteStream.toString();
		ColumbaLogger.log.debug("request=" + request);
		
	}

}

class TestArgumentWriter extends ArgumentWriter {

	public TestArgumentWriter(OutputStream o) {
		super(o);
	}

	protected void writeBytes(byte[] data) throws Exception {
		output.write(openingCurlyBracket);
		output.write(Integer.toString(data.length).getBytes("ISO-8859-1"));
		output.write(closingCurlyBracket);
		output.write(newline);

		output.flush();

		output.write(data);
	}
}
