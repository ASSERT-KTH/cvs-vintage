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
//$Log: DefaultWizardDialog.java,v $
//Revision 1.2  2003/02/04 19:02:13  fdietz
//[bug]account-wizard never received focus - using DialogStore now to handle it correctly
//
//Revision 1.1  2003/02/03 14:58:41  fdietz
//[intern]wizard fixes
//
package org.columba.core.gui.util.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;

import org.columba.core.gui.util.DialogStore;
import org.columba.core.logging.ColumbaLogger;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DefaultWizardDialog implements ActionListener {

	private JButton nextButton;
	private JButton prevButton;
	private JButton cancelButton;
	private JButton helpButton;
	private JButton finishButton;

	protected int position;

	public static int FIRST = 0;
	public static int LAST = 1;
	public static int MIDDLE = 2;

	public static Dimension WINDOW_DIMENSION = new Dimension(640, 480);

	protected JDialog dialog;
	/**
	 * Constructor for DefaultWizardDialog.
	 * @throws HeadlessException
	 */
	public DefaultWizardDialog() throws HeadlessException {
		dialog = DialogStore.getDialog();

		DefaultWizardPanel p = getSequence().getFirstPanel();

		helpButton = new JButton("Help");
		helpButton.setEnabled(false);

		nextButton = new JButton("Next >");
		nextButton.setActionCommand("NEXT");
		nextButton.addActionListener(this);

		finishButton = new JButton("Finish");
		finishButton.setActionCommand("FINISH");
		finishButton.addActionListener(this);

		finishButton.setEnabled(true);
		prevButton = new JButton("< Prev");
		prevButton.setActionCommand("PREV");
		prevButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);
		init(p);

		dialog.getContentPane().add(p, BorderLayout.CENTER);

		//nextButton.setEnabled(true);

		updateWindow(p);

		dialog.setLocationRelativeTo(null);

		dialog.setVisible(true);

	}

	public WizardPanelSequence getSequence() {
		return null;
	}

	public JPanel createTopPanel(
		String title,
		String description,
		ImageIcon icon) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(300, 60));
		panel.setLayout(new BorderLayout());
		panel.setBorder(
			new CompoundBorder(
				new WizardBottomBorder(),
				BorderFactory.createEmptyBorder(12, 12, 11, 11)));

		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.white);

		GridBagLayout layout = new GridBagLayout();
		leftPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();

		JLabel titleLabel = new JLabel(title);
		//titleLabel.setAlignmentY(0);
		Font font = UIManager.getFont("Label.font");
		font = font.deriveFont(Font.BOLD);
		titleLabel.setFont(font);
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(titleLabel, c);
		leftPanel.add(titleLabel);

		c.gridy = 1;
		c.insets = new Insets(0, 20, 0, 0);
		JLabel descriptionLabel = new JLabel(description);
		layout.setConstraints(descriptionLabel, c);
		leftPanel.add(descriptionLabel);

		panel.add(leftPanel, BorderLayout.WEST);

		JLabel iconLabel = new JLabel(icon);
		panel.add(iconLabel, BorderLayout.EAST);

		return panel;
	}

	public JPanel createLeftPanel() {
		JPanel panel = new JPanel();

		panel.setBackground(UIManager.getColor("textHighlight"));
		panel.setPreferredSize(new Dimension(100, 200));

		JLabel title =
			new JLabel(
				(ImageIcon) UIManager.getIcon(
					"org/columba/core/images/Zoom24.gif"));
		//title.setForeground( Color.white );
		title.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return panel;
	}

	protected void init(DefaultWizardPanel p) {

		dialog.getContentPane().setLayout(new BorderLayout());

		dialog.getContentPane().add(
			createTopPanel(p.getTitle(), p.getDescription(), p.getIcon()),
			BorderLayout.NORTH);

		//add(createPanel(listener), BorderLayout.CENTER);

		dialog.getContentPane().add(p, BorderLayout.CENTER);

		//nextButton.setEnabled(false);
		//getRootPane().setDefaultButton(nextButton);

		if (getSequence().isLast(p)) {

			dialog.getRootPane().setDefaultButton(finishButton);

		} else {
			//nextButton.setEnabled( true );

			dialog.getRootPane().setDefaultButton(nextButton);
			nextButton.setEnabled(true);
			//nextButton.requestFocusInWindow();
		}

		if (getSequence().isFirst(p))
			prevButton.setEnabled(false);
		else
			prevButton.setEnabled(true);

		JPanel lower = new JPanel();
		lower.setLayout(new BorderLayout());
		//lower.setLayout(new BoxLayout(lower, BoxLayout.X_AXIS));	

		lower.setBorder(
			new CompoundBorder(
				new WizardTopBorder(),
				BorderFactory.createEmptyBorder(17, 12, 11, 11)));

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridLayout(1, 4, 10, 10));
		lower.add(innerPanel, BorderLayout.EAST);

		innerPanel.add(helpButton);

		innerPanel.add(prevButton);

		if (getSequence().isLast(p))
			innerPanel.add(finishButton);
		else
			innerPanel.add(nextButton);

		innerPanel.add(cancelButton);

		dialog.getContentPane().add(lower, BorderLayout.SOUTH);

	}

	public void select() {
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("NEXT")) {
			dialog.getContentPane().removeAll();
			DefaultWizardPanel p = getSequence().getNextPanel();

			init(p);

			updateWindow(p);

		} else if (action.equals("PREV")) {
			dialog.getContentPane().removeAll();
			DefaultWizardPanel p = getSequence().getPreviousPanel();

			init(p);
			updateWindow(p);
			

		} else if (action.equals("CANCEL")) {

			dialog.setVisible(false);
		}
	}

	protected void updateWindow(DefaultWizardPanel p) {
		//pack();

		dialog.setVisible(false);
		Dimension d = WINDOW_DIMENSION;
		Dimension size = dialog.getSize();

		if ((size.width < d.width) || (size.height < d.height)) {
			dialog.setSize(d);
			dialog.setLocationRelativeTo(null);
		}

		dialog.validate();
		dialog.repaint();

		JComponent c = p.getFocusComponent();
		if (c != null) {

			boolean b = c.requestFocusInWindow();
			ColumbaLogger.log.debug("focus=" + b);
		} else {
			if (getSequence().isLast(p))
				finishButton.requestFocusInWindow();
			else
				nextButton.requestFocusInWindow();
		}

		dialog.setVisible(true);

	}

	/*
	public void insertUpdate(DocumentEvent e) {
		update();
	}
	
	public void removeUpdate(DocumentEvent e) {
		update();
	}
	
	public void changedUpdate(DocumentEvent e) {
		update();
		//Plain text components don't fire these events
	}
	
	public void update() {
		JTextComponent tc;
		String str;
		boolean hit = false;
	
		// FIXME
		
		for (int i = 0; i < getSequence().count(); i++) {
			tc = (JTextComponent) v.get(i);
			str = tc.getText();
		
			if (str.length() == 0)
				hit = true;
		}
		
		if (hit == true) {
			nextButton.setEnabled(false);
			if (finishButton != null)
				finishButton.setEnabled(false);
		} else {
			nextButton.setEnabled(true);
			if (finishButton != null)
				finishButton.setEnabled(true);
		}
		
	}
	*/
}
