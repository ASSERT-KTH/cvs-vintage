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

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.columba.core.io.StreamUtils;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;

/**
 * @author fdietz
 *  
 */
public class ImageViewer extends JPanel implements IMimePartViewer {

	private MailFrameMediator mediator;

	private byte[] data;

	private ImageIcon image;

	/**
	 *  
	 */
	public ImageViewer(MailFrameMediator mediator) {
		super();

		this.mediator = mediator;

		setLayout(new BorderLayout());

		setBackground(UIManager.getColor("TextArea.background"));

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IMimePartViewer#view(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object, java.lang.Integer[],
	 *      org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, Integer[] address,
			MailFrameMediator mediator) throws Exception {

		System.out.print("image viewer=");
		printAddress(address);

		MimePart bodyPart = folder.getMimePartTree(uid).getFromAddress(address);

		InputStream bodyStream = folder.getMimePartBodyStream(uid, address);

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

		data = StreamUtils.readInByteArray(bodyStream);
		System.out.println("---> retrieving image data");
	}

	private void printAddress(Integer[] address) {

		for (int i = 0; i < address.length; i++) {
			System.out.print(address[i].toString());
		}

		System.out.println();
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#updateGUI()
	 */
	public void updateGUI() throws Exception {

		removeAll();

		image = new ImageIcon(Toolkit.getDefaultToolkit().createImage(data));
		//image = new ImageIcon(data);

		JLabel label = new JLabel("test");
		label.setIconTextGap(10);
		label.setIcon(image);

		System.out.println("image-size=" + image.getIconWidth());

		add(label, BorderLayout.CENTER);
		
		revalidate();
		
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#getView()
	 */
	public JComponent getView() {
		return this;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}
}