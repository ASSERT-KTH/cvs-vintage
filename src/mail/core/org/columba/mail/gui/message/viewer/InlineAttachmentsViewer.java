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
import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * @author fdietz
 *
 */
public class InlineAttachmentsViewer extends JPanel implements Viewer {

	private MimeTree mimePartTree;
	private Vector attachments;
	
	/**
	 * 
	 */
	public InlineAttachmentsViewer() {
		super();
		
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#view(org.columba.mail.folder.IMailbox, java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator)
			throws Exception {
		
		mimePartTree = folder.getMimePartTree(uid);
		LinkedList list = mimePartTree.getAllLeafs();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			MimePart mp = (MimePart) it.next();
			MimeHeader h = mp.getHeader();
		}
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		removeAll();
		
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		
		LinkedList list = mimePartTree.getAllLeafs();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			MimePart mp = (MimePart) it.next();		
			
			JPanel centerPanel = createAttachmentPanel();
			
			add( centerPanel );
		}
	}

	/**
	 * @return
	 */
	private JPanel createAttachmentPanel() {
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JButton openButton = new JButton("Open");
		JButton saveButton = new JButton("Save");
		buttonPanel.add(openButton);
		buttonPanel.add(saveButton);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(buttonPanel, BorderLayout.WEST);
		
		JPanel centerPanel = new JPanel();
		centerPanel.add(topPanel, BorderLayout.NORTH);
		
		JPanel viewerPanel = new JPanel();
		centerPanel.add(viewerPanel, BorderLayout.CENTER);
		return centerPanel;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#getView()
	 */
	public JComponent getView() {
		return this;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

}
