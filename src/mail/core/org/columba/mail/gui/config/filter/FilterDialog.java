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
package org.columba.mail.gui.config.filter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterRule;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.LabelWithMnemonic;
import org.columba.core.help.HelpManager;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterDialog extends JDialog implements ActionListener {

	private JTextField nameTextField;

	private JButton addActionButton;

	private Filter filter;

	private JFrame frame;

	private CriteriaList criteriaList;

	private ActionList actionList;

	private JComboBox condList;

	private FrameMediator mediator;

	private JLabel nameLabel;

	private JLabel executeActionLabel;

	private JButton addCriteriaButton;

	/*
	 * private TitledBorderLabel m_titledborderlabel1 = new TitledBorderLabel();
	 * 
	 * private TitledBorderLabel m_titledborderlabel2 = new TitledBorderLabel();
	 */

	/**
	 * Boolean stating whetever the dialog was cancelled or not. Default value
	 * is <code>true</code>.
	 */
	private boolean dialogWasCancelled = true;

	public FilterDialog(FrameMediator mediator, Filter filter) {
		super(mediator.getView().getFrame(), true);

		this.mediator = mediator;

		setTitle(MailResourceLoader.getString("dialog", "filter",
				"dialog_title"));
		this.filter = filter;

		//System.out.println("filternode name: " + filter.getName());
		initComponents();
		updateComponents(true);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel createPanel() {

		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE,6DLU,FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,6DLU,CENTER:DEFAULT:NONE,3DLU,FILL:DEFAULT:GROW(1.0),6DLU,CENTER:DEFAULT:NONE,3DLU,FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE");

		CellConstraints cc = new CellConstraints();
		PanelBuilder builder = new PanelBuilder(formlayout1);

		builder.add(createPanel1(), cc.xywh(2, 2, 2, 1));

		builder.addSeparator(MailResourceLoader.getString("dialog", "filter",
				"if"), cc.xywh(2, 4, 2, 1));

		builder.add(createPanel2(), cc.xy(3, 6));

		builder.addSeparator(MailResourceLoader.getString("dialog", "filter",
				"then"), cc.xywh(2, 8, 2, 1));

		builder.add(createPanel3(), cc.xy(3, 10));

		builder.setDefaultDialogBorder();

		return builder.getPanel();
	}

	private JPanel createPanel1() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"LEFT:DEFAULT:NONE,3DLU,FILL:DEFAULT:GROW(1.0)",
				"FILL:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		jpanel1.add(nameLabel, cc.xy(1, 1));

		jpanel1.add(nameTextField, cc.xy(3, 1));

		return jpanel1;
	}

	private JPanel createPanel2() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,3DLU,FILL:DEFAULT:NONE",
				"FILL:DEFAULT:NONE,3DLU,FILL:DEFAULT:GROW(1.0)");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		//jpanel1.add(addCriteriaButton, cc.xy(1, 1));

		jpanel1.add(executeActionLabel, cc.xy(3, 1));

		jpanel1.add(condList, cc.xy(5, 1));

		jpanel1.add(criteriaList, cc.xywh(1, 3, 5, 1));

		return jpanel1;
	}

	private JPanel createPanel3() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)",
				"FILL:DEFAULT:NONE,3DLU,FILL:DEFAULT:GROW(1.0)");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		//jpanel1.add(addActionButton, cc.xy(1, 1));

		jpanel1.add(actionList, cc.xywh(1, 3, 2, 1));

		return jpanel1;
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		nameLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "filter", "filter_description"));

		nameTextField = new JTextField(22);
		nameLabel.setLabelFor(nameTextField);

		executeActionLabel = new LabelWithMnemonic(MailResourceLoader
				.getString("dialog", "filter", "execute_actions"));

		String[] cond = {
				MailResourceLoader
						.getString("dialog", "filter", "all_criteria"),
				MailResourceLoader
						.getString("dialog", "filter", "any_criteria") };
		condList = new JComboBox(cond);
		executeActionLabel.setLabelFor(condList);

		criteriaList = new CriteriaList(filter);

		addCriteriaButton = new ButtonWithMnemonic(MailResourceLoader
				.getString("dialog", "filter", "add_criteria"));
		addCriteriaButton.setIcon(ImageLoader.getImageIcon("stock_add_16.png"));
		addCriteriaButton.addActionListener(this);
		addCriteriaButton.setActionCommand("ADD_CRITERIA");

		addActionButton = new ButtonWithMnemonic(MailResourceLoader.getString(
				"dialog", "filter", "add_action"));
		addActionButton.setIcon(ImageLoader.getImageIcon("stock_add_16.png"));
		addActionButton.addActionListener(this);
		addActionButton.setActionCommand("ADD_ACTION");

		JLabel actionLabel = new LabelWithMnemonic(MailResourceLoader
				.getString("dialog", "filter", "action_list"));

		actionList = new ActionList(mediator, filter, frame);

		add(createPanel(), BorderLayout.CENTER);

		createBottomPanel();
	}

	/**
	 *  
	 */
	private void createBottomPanel() {
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		ButtonWithMnemonic okButton = new ButtonWithMnemonic(MailResourceLoader
				.getString("global", "ok"));
		okButton.setActionCommand("CLOSE"); //$NON-NLS-1$
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		ButtonWithMnemonic cancelButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "cancel"));
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);

		ButtonWithMnemonic helpButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "help"));
		buttonPanel.add(helpButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this, "CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		// associate with JavaHelp
		HelpManager.getHelpManager().enableHelpOnButton(helpButton,
				"organizing_and_managing_your_email_3");
		HelpManager.getHelpManager().enableHelpKey(getRootPane(),
				"organizing_and_managing_your_email_3");
	}

	public void updateComponents(boolean b) {
		if (b) {
			// set component values
			criteriaList.updateComponents(b);
			actionList.updateComponents(b);

			// filter description JTextField
			nameTextField.setText(filter.getName());
			nameTextField.selectAll();

			// all / match any JComboBox
			FilterRule filterRule = filter.getFilterRule();
			String value = filterRule.getCondition();

			if (value.equals("matchall")) {
				condList.setSelectedIndex(0);
			} else {
				condList.setSelectedIndex(1);
			}
		} else {
			// get values from components
			criteriaList.updateComponents(b);
			actionList.updateComponents(b);

			filter.setName(nameTextField.getText());

			int index = condList.getSelectedIndex();
			FilterRule filterRule = filter.getFilterRule();

			if (index == 0) {
				filterRule.setCondition("matchall");
			} else {
				filterRule.setCondition("matchany");
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CLOSE")) {
			updateComponents(false);
			setVisible(false);

			//frame.listView.update();
			dialogWasCancelled = false;
		} else if (action.equals("CANCEL")) {
			setVisible(false);
			dialogWasCancelled = true;
		} else if (action.equals("ADD_CRITERION")) {
			criteriaList.add();
		} else if (action.equals("ADD_ACTION")) {
			//System.out.println( "add" );
			actionList.add();
		}
	}

	/**
	 * Returns if the dialog was cancelled or not. The dialog is cancelled if
	 * the user presses the <code>Cancel</code> button or presses the
	 * <code>Escape</code> key.
	 * 
	 * @return true if the user pressed the cancel button or escape; false
	 *         otherwise.
	 */
	public boolean wasCancelled() {
		return dialogWasCancelled;
	}
}