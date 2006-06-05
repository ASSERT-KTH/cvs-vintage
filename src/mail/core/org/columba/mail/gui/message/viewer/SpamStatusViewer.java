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
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.columba.core.command.CommandProcessor;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.command.ToggleMarkCommand;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.IMessageController;

/**
 * IViewer displaying spam status information.
 * 
 * @author fdietz
 *  
 */
public class SpamStatusViewer extends JPanel implements ICustomViewer,
		ActionListener {


	private boolean visible;

	private IMessageController mediator;

	private JLabel label;

	private JButton button;

	private JPanel panel;

	public SpamStatusViewer(IMessageController mediator) {
		super();

		this.mediator = mediator;
		setBackground(Color.white);

		panel = new JPanel();
		label = new JLabel("");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		button = new JButton("No Spam");

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
		panel.setBackground(new Color(0xFABB48));
		panel.setLayout(new BorderLayout());

		Border border = BorderFactory.createLineBorder(Color.gray);

		panel.setBorder(BorderFactory.createCompoundBorder(border,
				BorderFactory.createEmptyBorder(2, 5, 2, 2)));

		add(panel, BorderLayout.CENTER);

		panel.add(label, BorderLayout.WEST);

		panel.add(button, BorderLayout.EAST);

		button.addActionListener(this);

		visible = false;
	}

	protected void layoutComponents(boolean isSpam) {

		if (isSpam) {
			panel.removeAll();

			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
			panel.setBackground(new Color(1.0f, 0.8f, 0.5f));
			panel.setLayout(new BorderLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

			add(panel, BorderLayout.CENTER);

			panel.add(label, BorderLayout.WEST);

			panel.add(button, BorderLayout.EAST);

		} else {
			removeAll();
		}

		revalidate();
		updateUI();
	}

	/**
	 * @see javax.swing.JComponent#updateUI()
	 */
	public void updateUI() {
		super.updateUI();

		setBorder(new MessageBorder(new Color(255, 176, 100), 1, true));
		
		Color color = new Color(255, 255, 160);
		
		if (panel != null)
			panel.setBackground(color);


	}

	private  void setSpam(boolean isSpam) {

		if (label != null) {
			if (isSpam == true)
				label.setText("Message is marked as spam");
			else
				label.setText("");

			//layoutComponents(isSpam);
		}
	}

	/**
	 * @see org.columba.mail.gui.message.status.Status#show(org.columba.mail.folder.Folder,
	 *      java.lang.Object)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator)
			throws Exception {
		Boolean spam = (Boolean) folder.getAttribute(uid, "columba.spam");

		visible = spam.booleanValue();

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#getView()
	 */
	public JComponent getView() {
		return label;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#isVisible()
	 */
	public boolean isVisible() {
		// only show view if message is marked as spam
		return visible;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		// get selected message
		IMailFolderCommandReference r = mediator.getSelectedReference();

		// mark as not spam
		r.setMarkVariant(MarkMessageCommand.MARK_AS_NOTSPAM);
		
		ToggleMarkCommand c = new ToggleMarkCommand(r);
		CommandProcessor.getInstance().addOp(c);
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		setSpam(visible);

	}
}