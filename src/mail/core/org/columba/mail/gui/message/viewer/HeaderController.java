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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JComponent;

import org.columba.core.config.DefaultItem;
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
 * Shows the headers of a RFC822 message in a lightgray box in the top of
 * the message viewer.
 * 
 * @author fdietz
 */
public class HeaderController implements Viewer {

	private HeaderView view;

	//  contains headerfields which are to be displayed
	private Map map;

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
		DefaultItem item = new DefaultItem(headerviewerElement);
		int style = item.getInteger("style", 0);

		map = new LinkedHashMap();
		Header header = null;
		String[] headers = null;
		switch (style) {
		case 0:
			// default
			headers = new String[] { "Subject", "Date", "From", "To",
					"Reply-To", "Cc", "Bcc" };

			// get header from folder
			header = folder.getHeaderFields(uid, headers);

			// transform headers if necessary
			for (int i = 0; i < headers.length; i++) {
				String key = headers[i];
				Object value = transformHeaderField(header, key);
				if (value != null)
					map.put(key, value);
			}

			break;
		case 1:
			// custom headers
			String list = headerviewerElement.getAttribute("headerfields");

			StringTokenizer tok = new StringTokenizer(list, " ");
			headers = new String[tok.countTokens()];

			for (int i = 0; i < headers.length; i++) {
				headers[i] = tok.nextToken();
			}

			// get header from folder
			header = folder.getHeaderFields(uid, headers);

			// transform headers if necessary
			for (int i = 0; i < headers.length; i++) {
				String key = headers[i];
				Object value = transformHeaderField(header, key);
				if (value != null)
					map.put(key, value);
			}

			break;
		case 2:
			// show all headers
			header = folder.getAllHeaderFields(uid);
			
			// transform headers if necessary
			Enumeration enum = header.getKeys();		
			map = new LinkedHashMap();
			while (enum.hasMoreElements()) {
				String key = (String) enum.nextElement();
				Object value = transformHeaderField(header, key);
				if (value != null)
					map.put(key, value);
			}
			
			break;
		}

		//map = initHeaderFields(header);
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

	protected Object transformHeaderField(Header header, String key) {
		BasicHeader bHeader = new BasicHeader(header);
		String str = null;

		//          message doesn't contain this headerfield
		if (header.get(key) == null) {
			return null;
		}

		// headerfield is empty
		if (((String) header.get(key)).length() == 0) {
			return null;
		}

		if (key.equals("Subject")) {
			str = bHeader.getSubject();

			// substitute special characters like:
			//  <,>,&,\t,\n,"
			str = HtmlParser.substituteSpecialCharactersInHeaderfields(str);
		} else if (key.equals("To")) {
			str = AddressListRenderer.renderToHTMLWithLinks(bHeader.getTo())
					.toString();
		} else if (key.equals("Reply-To")) {
			str = AddressListRenderer.renderToHTMLWithLinks(
					bHeader.getReplyTo()).toString();
		} else if (key.equals("Cc")) {
			str = AddressListRenderer.renderToHTMLWithLinks(bHeader.getCc())
					.toString();
		} else if (key.equals("Bcc")) {
			str = AddressListRenderer.renderToHTMLWithLinks(bHeader.getBcc())
					.toString();
		} else if (key.equals("From")) {
			str = AddressListRenderer.renderToHTMLWithLinks(
					new Address[] { (Address) bHeader.getFrom() }).toString();
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

		return str;
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
		view.getHeaderTextPane().setHeader(map);

	}
}