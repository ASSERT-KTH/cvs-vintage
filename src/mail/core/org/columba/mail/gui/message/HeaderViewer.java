package org.columba.mail.gui.message;

import java.awt.Insets;
import java.net.URL;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class HeaderViewer extends JTextPane {

	// background: ebebeb
	// frame: d5d5d5
	private static final String LEFT_COLUMN_PROPERTIES =
		"border=\"0\" nowrap font=\"dialog\" align=\"right\" valign=\"top\" width=\"65\"";
	private static final String RIGHT_COLUMN_PROPERTIES =
		"border=\"0\" align=\"left\" valign=\"top\" width=\"100%\"";
	private static final String OUTTER_TABLE_PROPERTIES =
		"border=\"1\" cellspacing=\"1\" cellpadding=\"1\" align=\"left\" width=\"100%\" style=\"border-width:1px; border-style:solid;  background-color:#ebebeb\"";
	private static final String INNER_TABLE_PROPERTIES =
		"border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" width=\"100%\"";
	private static final String GLOBAL_CSS =
		"td {font-family:Dialog; font-size:12pt}";
	private static final String CSS =
		"<style type=\"text/css\"><!--" + GLOBAL_CSS + "--></style>";

	String[] keys;

	DocumentParser parser;

	public HeaderViewer() {
		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);
		HTMLEditorKit editorKit = new HTMLEditorKit();
		setEditorKit(editorKit);

		URL baseUrl = DiskIO.getResourceURL( "org/columba/core/images/");
		ColumbaLogger.log.debug(baseUrl.toString());
		((HTMLDocument)getDocument()).setBase(baseUrl);

		parser = new DocumentParser();

		keys = new String[7];
		keys[0] = "Subject";
		keys[1] = "Date";
		keys[2] = "Reply-To";
		keys[3] = "From";
		keys[4] = "To";
		keys[5] = "Cc";
		keys[6] = "Bcc";
	}

	void setHeader(HeaderInterface header) throws Exception {
		// rahmen #949494
		// background #989898
		// #a0a0a0
		// hell #d5d5d5

		StringBuffer buf = new StringBuffer();
		buf.append(
			"<HTML><HEAD>"
				+ CSS
				+ "</HEAD><BODY ><TABLE "
				+ OUTTER_TABLE_PROPERTIES
				+ ">");
		for (int i = 0; i < keys.length; i++) {
			if (header.get(keys[i]) == null)
				continue;

			if (((String) header.get(keys[i])).length() == 0)
				continue;

			buf.append("<TR><TD " + LEFT_COLUMN_PROPERTIES + ">");
			buf.append("<B>" + keys[i] + " : </B>");
			buf.append("</TD><TD " + RIGHT_COLUMN_PROPERTIES + ">");
			buf.append(
				" "
					+ parser.substituteEmailAddress(
						(String) header.get(keys[i])));
			buf.append("</TD></TR>");
		}
		
		buf.append("</TABLE></BODY></HTML>");
		

		setText(buf.toString());

	}

	/*
	protected String parseHeader(String headerField, String value) {
	
		boolean addressList = false;
	
		for (int i = 2; i < keys.length; i++) {
			if (headerField.equalsIgnoreCase(keys[i]))
				addressList |= true;
		}
	
		if (addressList) {
			Vector v = ListParser.parseString(value);
			StringBuffer buf = new StringBuffer();
	
			for (int i = 0; i < v.size(); i++) {
				String s = ((String) v.get(i)).trim();
	
				buf.append("<A href=\"" + s + "\">" + s + "</A>");
				if (i != v.size() - 1)
					buf.append(" ,");
			}
	
			return buf.toString();
		} else
			return value;
	
	}
	*/
}
