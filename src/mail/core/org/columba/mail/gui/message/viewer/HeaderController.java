// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message.viewer;

import java.text.DateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JComponent;

import org.columba.core.xml.XmlElement;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.util.AddressListRenderer;
import org.columba.mail.main.MailInterface;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;

/**
 * @author fdietz
 *  
 */
public class HeaderController implements Viewer {

	private HeaderView view;

	//  contains headerfields which are to be displayed
	private Map keys;

	private boolean visible;

	public HeaderController() {

		view = new HeaderView();

		visible = false;

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#view(org.columba.mail.folder.Folder,
	 *      java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(MessageFolder folder, Object uid,
			MailFrameMediator mediator) throws Exception {
		// add headerfields which are about to show up
		XmlElement headerviewerElement = MailInterface.config.get("options")
				.getElement("/options/headerviewer");
		String list = headerviewerElement.getAttribute("headerfields");

		StringTokenizer tok = new StringTokenizer(list, " ");
		String[] headers = new String[tok.countTokens()];

		for (int i = 0; i < headers.length; i++) {
			headers[i] = tok.nextToken();
		}

		Header header = folder.getHeaderFields(uid, headers);

		keys = initHeaderFields(header);
		boolean hasAttachment = ((Boolean) folder.getAttribute(uid,
				"columba.attachment")).booleanValue();

		view.getStatusPanel().setStatus(hasAttachment);

		visible = true;

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#getView()
	 */
	public JComponent getView() {
		return view;
	}

	protected Map initHeaderFields(Header header) {
		Enumeration tok = header.getKeys();
		BasicHeader bHeader = new BasicHeader(header);

		keys = new HashMap();

		while (tok.hasMoreElements()) {
			String key = (String) tok.nextElement();
			String str = null;

			//          message doesn't contain this headerfield
			if (header.get(key) == null) {
				continue;
			}

			// headerfield is empty
			if (((String) header.get(key)).length() == 0) {
				continue;
			}

			if (key.equals("Subject")) {
				str = bHeader.getSubject();

				// substitute special characters like:
				//  <,>,&,\t,\n,"
				str = HtmlParser.substituteSpecialCharactersInHeaderfields(str);
			} else if (key.equals("To")) {
				str = AddressListRenderer
						.renderToHTMLWithLinks(bHeader.getTo()).toString();
			} else if (key.equals("Reply-To")) {
				str = AddressListRenderer.renderToHTMLWithLinks(
						bHeader.getReplyTo()).toString();
			} else if (key.equals("Cc")) {
				str = AddressListRenderer
						.renderToHTMLWithLinks(bHeader.getCc()).toString();
			} else if (key.equals("Bcc")) {
				str = AddressListRenderer.renderToHTMLWithLinks(
						bHeader.getBcc()).toString();
			} else if (key.equals("From")) {
				str = AddressListRenderer.renderToHTMLWithLinks(
						new Address[] { (Address) bHeader.getFrom() })
						.toString();
			} else if (key.equals("Date")) {
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,
						DateFormat.MEDIUM);
				str = df.format(bHeader.getDate());

				// substitute special characters like:
				//  <,>,&,\t,\n,"
				str = HtmlParser.substituteSpecialCharactersInHeaderfields(str);
			} else {
				str = (String) header.get(key);

				// substitute special characters like:
				//  <,>,&,\t,\n,"
				str = HtmlParser.substituteSpecialCharactersInHeaderfields(str);
			}

			keys.put(key, str);
		}

		return keys;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#isVisible()
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		view.getHeaderTextPane().setHeader(keys);

	}
}