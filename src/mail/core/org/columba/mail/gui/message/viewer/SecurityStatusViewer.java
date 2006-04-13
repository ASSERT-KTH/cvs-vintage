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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.filter.SecurityStatusEvent;
import org.columba.mail.gui.message.filter.SecurityStatusListener;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.mail.util.MailResourceLoader;

/**
 * IViewer displays security status information.
 * 
 * @author fdietz
 * 
 */
public class SecurityStatusViewer extends JPanel implements ICustomViewer,
		SecurityStatusListener {

	public static final int DECRYPTION_SUCCESS = 0;

	public static final int DECRYPTION_FAILURE = 1;

	public static final int VERIFICATION_SUCCESS = 2;

	public static final int VERIFICATION_FAILURE = 3;

	public static final int NO_KEY = 4;

	public static final int NOOP = 5;

	protected JLabel icon;

	protected JLabel text;

	protected JPanel left;

	private boolean visible;

	public SecurityStatusViewer() {
		super();

		setLayout(new BorderLayout());

		left = new JPanel();
		left.setLayout(new BorderLayout());
		left.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

		icon = new JLabel();
		left.add(icon, BorderLayout.NORTH);

		add(left, BorderLayout.WEST);
		text = new JLabel();
		add(text, BorderLayout.CENTER);

		setValue(SecurityStatusViewer.NOOP, "");

		updateUI();

		visible = false;
	}

	public void updateUI() {
		super.updateUI();

		setBorder(new MessageBorder(new Color(255, 255, 60), 1, true));

		Color color = new Color(255, 255, 160);

		setBackground(color);

		if (icon != null) {
			icon.setBackground(color);
		}

		if (text != null) {
			text.setBackground(color);
		}

		if (left != null) {
			left.setBackground(color);
		}
	}

	private void setValue(int value, String message) {
		ImageIcon image = null;

		switch (value) {
		case SecurityStatusViewer.DECRYPTION_SUCCESS: {
			image = ImageLoader.getMiscIcon("signature-ok.png");

			icon.setToolTipText(MailResourceLoader.getString("menu",
					"mainframe", "security_decrypt_success"));
			text.setText(transformToHTML(MailResourceLoader.getString("menu",
					"mainframe", "security_decrypt_success"), message));

			break;
		}

		case SecurityStatusViewer.DECRYPTION_FAILURE: {
			image = ImageLoader.getMiscIcon("signature-bad.png");
			icon.setToolTipText(MailResourceLoader.getString("menu",
					"mainframe", "security_encrypt_fail"));
			text.setText(transformToHTML(MailResourceLoader.getString("menu",
					"mainframe", "security_encrypt_fail"), message));

			break;
		}

		case SecurityStatusViewer.VERIFICATION_SUCCESS: {
			image = ImageLoader.getMiscIcon("signature-ok.png");
			icon.setToolTipText(MailResourceLoader.getString("menu",
					"mainframe", "security_verify_success"));
			text.setText(transformToHTML(MailResourceLoader.getString("menu",
					"mainframe", "security_verify_success"), message));

			break;
		}

		case SecurityStatusViewer.VERIFICATION_FAILURE: {
			image = ImageLoader.getMiscIcon("signature-bad.png");

			icon.setToolTipText(MailResourceLoader.getString("menu",
					"mainframe", "security_verify_fail"));
			text.setText(transformToHTML(MailResourceLoader.getString("menu",
					"mainframe", "security_verify_fail"), message));

			break;
		}

		case SecurityStatusViewer.NO_KEY: {
			image = ImageLoader.getMiscIcon("signature-nokey.png");
			icon.setToolTipText(MailResourceLoader.getString("menu",
					"mainframe", "security_verify_nokey"));
			text.setText(transformToHTML(MailResourceLoader.getString("menu",
					"mainframe", "security_verify_nokey"), message));

			break;
		}

		case SecurityStatusViewer.NOOP: {
			text.setText("");
			icon.setIcon(null);

			break;
		}
		}

		if (image != null) {
			// scale image
			image = new ImageIcon(image.getImage().getScaledInstance(16, 16,
					Image.SCALE_SMOOTH));

			icon.setIcon(image);
		}

		updateUI();
	}

	protected String transformToHTML(String title, String message) {
		// convert special characters
		String html = null;

		if (message != null) {
			html = HtmlParser.substituteSpecialCharacters(message);
		}

		StringBuffer buf = new StringBuffer();

		buf.append("<html><body><p>");
		// buf.append("<b>" + title + "</b><br>");
		buf.append(title + "<br>");
		buf.append(html);
		buf.append("</p></body></html>");

		return buf.toString();
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#view(IMailbox,
	 *      java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator)
			throws Exception {

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#getView()
	 */
	public JComponent getView() {
		return this;
	}

	/**
	 * @see org.columba.mail.gui.message.filter.SecurityStatusListener#statusUpdate(org.columba.mail.gui.message.filter.SecurityStatusEvent)
	 */
	public void statusUpdate(SecurityStatusEvent event) {
		String message = event.getMessage();
		int status = event.getStatus();

		setValue(status, message);

		if (status == NOOP)
			visible = false;
		else
			visible = true;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#isVisible()
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#updateGUI()
	 */
	public void updateGUI() throws Exception {

	}
}
