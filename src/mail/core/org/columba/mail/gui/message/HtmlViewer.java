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

package org.columba.mail.gui.message;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.File;
import java.io.StringReader;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.columba.mail.gui.util.URLController;
//import sun.security.krb5.internal.crypto.c;
//import sun.security.krb5.internal.crypto.e;
//import sun.security.krb5.internal.crypto.g;

public class HtmlViewer
	extends JTextPane
	implements DocumentViewer, HyperlinkListener {
	boolean active = false;

	private JMenuItem menuItem;
	private JPopupMenu popup;

	private File tempFile = null;

	public HtmlViewer() {
		super();
		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);
		setEditorKit(new HTMLEditorKit());
		addHyperlinkListener(this);
	}

	public void setHeader(Component header) {

	}

	private String validateHTMLString(String input) {
		StringBuffer output = new StringBuffer(input);
		int index = 0;

		String lowerCaseInput = input.trim();

		// Check for missing  <html> tag
		if (lowerCaseInput.indexOf("<html>") == -1) {
			if (lowerCaseInput.indexOf("<!doctype") != -1)
				index =
					lowerCaseInput.indexOf(
						"\n",
						lowerCaseInput.indexOf("<!doctype"))
						+ 1;
			output.insert(index, "<html>");
		}

		// Check for missing  </html> tag
		if (lowerCaseInput.indexOf("</html>") == -1) {
			output.append("</html>");
		}

		return output.toString();
	}

	public void clearDoc() {
	}

	public void setDoc(String str) {

		HTMLDocument newdoc =
			(HTMLDocument) getEditorKit().createDefaultDocument();

		try {

			String validated = validateHTMLString(str);
			//System.out.println("validate:\n"+validated);

			newdoc.getParser().parse(
				new StringReader(validated),
				newdoc.getReader(0),
				true);
			setDocument(newdoc);

			//setText( str );

			//setCaretPosition(0);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void print(Graphics g) {
		setMargin(new Insets(0, 0, 0, 0));
		super.print(g);
		setMargin(new Insets(5, 5, 5, 5));
	}

	public String getDoc() {
		return super.getText();
	}

	public void setActive(boolean b) {
		active = true;
	}

	public boolean isActive() {
		return active;
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (!isEnabled())
			return;

		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) e.getSource();
			if (e instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
				URL url = e.getURL();
				if (url == null) {
					// found email address

					System.out.println("found email address");
				} else {
					URLController c = new URLController();
					c.open(url);
				}
			}
		}

	}
}
