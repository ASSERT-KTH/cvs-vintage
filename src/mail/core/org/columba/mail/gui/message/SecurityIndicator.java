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

package org.columba.mail.gui.message;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.mail.util.MailResourceLoader;

public class SecurityIndicator extends JPanel {
	public static final int DECRYPTION_SUCCESS = 0;
	public static final int DECRYPTION_FAILURE = 1;
	public static final int VERIFICATION_SUCCESS = 2;
	public static final int VERIFICATION_FAILURE = 3;
	public static final int NO_KEY = 4;
	public static final int NOOP = 5;

	protected JLabel icon;
	protected JLabel text;
	protected JPanel left;
	
	public SecurityIndicator() {
		super();

		setLayout(new BorderLayout());

		left = new JPanel();
		left.setLayout( new BorderLayout() );
		left.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		
		icon = new JLabel();
		left.add(icon, BorderLayout.NORTH);

		add(left, BorderLayout.WEST);
		text = new JLabel();
		add(text, BorderLayout.CENTER);

		setValue(NOOP, "");
				
		updateUI();
	}

	public void updateUI() {
		super.updateUI();
		
		setBackground(Color.white);
		if (icon != null)
			icon.setBackground(Color.white);
		if (text != null)
			text.setBackground(Color.white);
			
		if ( left != null )
			left.setBackground(Color.white);
			
	}

	protected String transformToHTML(String title, String message) {
		// convert special characters
		String html=null;
		if ( message!=null)
			html = HtmlParser.substituteSpecialCharacters(message);

		StringBuffer buf = new StringBuffer();

		buf.append("<html><body><p>");
		buf.append("<b>" + title + "</b><br>");
		buf.append(html);
		buf.append("</p></body></html>");

		return buf.toString();
	}

	public void setValue(int value, String message) {

		
		switch (value) {
			case DECRYPTION_SUCCESS :
				{
					icon.setIcon(
						ImageLoader.getImageIcon("pgp-signature-ok.png"));
					icon.setToolTipText(
						MailResourceLoader.getString(
							"menu",
							"mainframe",
							"security_encrypt_success"));
					text.setText(
						transformToHTML(
							MailResourceLoader.getString(
								"menu",
								"mainframe",
								"security_encrypt_success"),
							message));
					break;
				}
			case DECRYPTION_FAILURE :
				{
					icon.setIcon(
						ImageLoader.getImageIcon("pgp-signature-bad.png"));
					icon.setToolTipText(
						MailResourceLoader.getString(
							"menu",
							"mainframe",
							"security_encrypt_fail"));
					text.setText(
						transformToHTML(
							MailResourceLoader.getString(
								"menu",
								"mainframe",
								"security_encrypt_fail"),
							message));
					break;
				}
			case VERIFICATION_SUCCESS :
				{
					icon.setIcon(
						ImageLoader.getImageIcon("pgp-signature-ok.png"));
					icon.setToolTipText(
						MailResourceLoader.getString(
							"menu",
							"mainframe",
							"security_verify_success"));
					text.setText(
						transformToHTML(
							MailResourceLoader.getString(
								"menu",
								"mainframe",
								"security_verify_success"),
							message));

					break;
				}
			case VERIFICATION_FAILURE :
				{
					icon.setIcon(
						ImageLoader.getImageIcon("pgp-signature-bad.png"));
					icon.setToolTipText(
						MailResourceLoader.getString(
							"menu",
							"mainframe",
							"security_verify_fail"));
					text.setText(
						transformToHTML(
							MailResourceLoader.getString(
								"menu",
								"mainframe",
								"security_verify_fail"),
							message));
					break;
				}
			case NO_KEY :
				{
					icon.setIcon(
						ImageLoader.getImageIcon("pgp-signature-nokey.png"));
					icon.setToolTipText(
						MailResourceLoader.getString(
							"menu",
							"mainframe",
							"security_verify_nokey"));
					text.setText(
						transformToHTML(
							MailResourceLoader.getString(
								"menu",
								"mainframe",
								"security_verify_nokey"),
							message));
					break;
				}
			case NOOP :
				{
					text.setText("");
					icon.setIcon(null);
					break;
				}

		}

		updateUI();
	}
}
