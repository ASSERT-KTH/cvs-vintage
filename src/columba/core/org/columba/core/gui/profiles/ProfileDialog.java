//The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.profiles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.help.HelpManager;
import org.columba.core.profiles.ProfileManager;
import org.columba.core.xml.XmlElement;

/**
 * Profile chooser dialog.
 * <p>
 * User can choose a profile from a list. Add a new profile or edit and existing
 * profiles's properties.
 * <p>
 * Additionally, the user can choose to hide this dialog on next startup.
 * 
 * @author fdietz
 */
public class ProfileDialog extends JDialog
		implements
			ActionListener,
			ListSelectionListener {

	protected JButton okButton;
	protected JButton helpButton;
	protected JButton addButton;
	protected JButton editButton;
	protected JButton defaultButton;
	protected JList list;
	protected String selection;
	protected JLabel nameLabel;
	protected JCheckBox checkBox;

	/**
	 * @throws java.awt.HeadlessException
	 */
	public ProfileDialog() throws HeadlessException {
		super(new JFrame(), true);

		// TODO: i18n
		setTitle("Profile Management");

		initComponents();

		layoutComponents();

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	protected void layoutComponents() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		//		 top panel
		JPanel topPanel = new JPanel();

		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		//topPanel.setLayout( );
		JPanel topBorderPanel = new JPanel();
		topBorderPanel.setLayout(new BorderLayout());

		//topBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5,
		// 0));
		topBorderPanel.add(topPanel);

		//mainPanel.add( topBorderPanel, BorderLayout.NORTH );

		topPanel.add(nameLabel);

		topPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		topPanel.add(Box.createHorizontalGlue());

		Component glue = Box.createVerticalGlue();
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;

		//c.fill = GridBagConstraints.HORIZONTAL;
		gridBagLayout.setConstraints(glue, c);

		gridBagLayout = new GridBagLayout();
		c = new GridBagConstraints();

		JPanel eastPanel = new JPanel(gridBagLayout);
		mainPanel.add(eastPanel, BorderLayout.EAST);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints(addButton, c);
		eastPanel.add(addButton);

		Component strut1 = Box.createRigidArea(new Dimension(30, 5));
		gridBagLayout.setConstraints(strut1, c);
		eastPanel.add(strut1);

		gridBagLayout.setConstraints(editButton, c);
		eastPanel.add(editButton);

		Component strut2 = Box.createRigidArea(new Dimension(30, 5));
		gridBagLayout.setConstraints(strut2, c);
		eastPanel.add(strut2);

		gridBagLayout.setConstraints(defaultButton, c);
		eastPanel.add(defaultButton);

		glue = Box.createVerticalGlue();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		gridBagLayout.setConstraints(glue, c);
		eastPanel.add(glue);

		// centerpanel
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(250, 150));
		scrollPane.getViewport().setBackground(Color.white);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		centerPanel.add(checkBox, BorderLayout.SOUTH);

		mainPanel.add(topPanel, BorderLayout.NORTH);

		mainPanel.add(centerPanel);
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		buttonPanel.add(okButton);

		buttonPanel.add(helpButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}

	protected void initComponents() {

		// TODO: i18n
		addButton = new ButtonWithMnemonic("&Add...");
		addButton.setActionCommand("ADD");
		addButton.addActionListener(this);
		addButton.setEnabled(false);

		// TODO: i18n
		editButton = new ButtonWithMnemonic("&Edit...");
		editButton.setActionCommand("EDIT");
		editButton.addActionListener(this);
		editButton.setEnabled(false);

		// TODO: i18n
		defaultButton = new ButtonWithMnemonic("Set &Default...");
		defaultButton.setActionCommand("DEFAULT");
		defaultButton.addActionListener(this);
		defaultButton.setEnabled(false);

		nameLabel = new JLabel("Choose Profile:");

		checkBox = new JCheckBox("Don't ask on next startup.");

		okButton = new ButtonWithMnemonic("&Ok");
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.addActionListener(this);

		helpButton = new ButtonWithMnemonic("&Help");

		// associate with JavaHelp
		HelpManager.getHelpManager().enableHelpOnButton(helpButton,
				"extending_columba_2");
		HelpManager.getHelpManager().enableHelpKey(getRootPane(),
				"extending_columba_2");

		XmlElement profiles = ProfileManager.getInstance().getProfiles();
		String[] profilesList = new String[profiles.count() + 1];
		profilesList[0] = "Default";

		for (int i = 0; i < profiles.count(); i++) {
			XmlElement p = profiles.getElement(i);
			String name = p.getAttribute("name");
			profilesList[i+1] = name;
		}

		list = new JList(profilesList);
		list.addListSelectionListener(this);
	
		String selected = ProfileManager.getInstance().getSelectedProfile();
		if ( selected != null)
			list.setSelectedValue(selected, true);
		
		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this, "CLOSE",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("OK")) {
			setVisible(false);
		} else if (action.equals("ADD")) {

		} else if (action.equals("EDIT")) {

		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		boolean enabled = !list.isSelectionEmpty();
		addButton.setEnabled(enabled);
		editButton.setEnabled(enabled);
		okButton.setEnabled(enabled);
		defaultButton.setEnabled(enabled);

		selection = (String) list.getSelectedValue();
	}

	/**
	 * @return Returns the selection.
	 */
	public String getSelection() {
		return selection;
	}
	
	public boolean isDontAskedSelected() {
		return checkBox.isSelected();
	}
}