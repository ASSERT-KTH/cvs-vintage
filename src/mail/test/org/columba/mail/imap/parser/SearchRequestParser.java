/*
 * Created on 06.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import java.io.DataOutputStream;
import java.util.List;

import junit.framework.TestCase;

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
		c.setHeaderItem("Body");
		c.setCriteria("contains");
		c.setPattern("Grüsse");
		c = rule.addEmptyCriteria();
		c.setHeaderItem("Body");
		c.setCriteria("contains");
		c.setPattern("Grüsse 2");

		SearchRequestBuilder e = new SearchRequestBuilder();
		e.setCharset("UTF-8");
		List list = e.generateSearchArguments(rule);
		Arguments args = e.generateSearchArguments(rule, list);

		String str = null;
		DataOutputStream os = new DataOutputStream(System.out);
		TestArgumentWriter writer = new TestArgumentWriter(os);
		writer.write(args);
	}

}

class TestArgumentWriter extends ArgumentWriter {

	public TestArgumentWriter( DataOutputStream o) {
		super(o);
	}

	protected void writeBytes(byte[] data) throws Exception {
		output.write('{');
		output.writeBytes(Integer.toString(data.length));
		output.writeBytes("}\r\n");
		output.flush();

		/*
		for (;;) {
			IMAPResponse r = protocol.getResponse(null);
			if (r.isCONTINUATION())
				break;
		}
		*/
		output.write(data);
	}
}
