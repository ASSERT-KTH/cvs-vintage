package org.columba.mail.gui.message;

import java.awt.Insets;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.gui.util.URLController;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BodyTextViewer extends JTextPane {

	private static final String GLOBAL_CSS =
		"p {font-family:DialogInput; font-size:12pt}";
	private static final String CSS =
		"<style type=\"text/css\"><!--" + GLOBAL_CSS + "--></style>";

	private DocumentParser parser;

	public BodyTextViewer() {
		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);
		setEditorKit(new HTMLEditorKit());

		parser = new DocumentParser();
		
		
	}

	

	public void setBodyText(String bodyText, boolean html) {
		if (html) {
			try {

				String validated = parser.validateHTMLString(bodyText);

				setText(validated);

				setCaretPosition(0);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			StringBuffer buf = new StringBuffer(bodyText);

			transformToHTML(buf);

			try {
				String r = parser.substituteURL(buf.toString());
				r = parser.substituteEmailAddress(r);
				setText(r);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			setCaretPosition(0);
		}
	}

	protected void transformToHTML(StringBuffer buf) {
		buf.insert(0, "<HTML><HEAD>" + CSS + "</HEAD><BODY ><P>");

		int pos = 0;
		boolean preformat = false;
		while (pos < buf.length()) {
			char ch = buf.charAt(pos);

			if (ch == '\n') {

				buf.replace(pos, pos + 1, "<br>");

			}

			pos++;
		}

		buf.append("</P></BODY></HTML>");

	}

	
	

}
