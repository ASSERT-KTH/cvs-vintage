// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.config.filter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.columba.core.gui.button.CloseButton;
import org.columba.core.gui.button.HelpButton;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.wizard.WizardTopBorder;
import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.util.MailResourceLoader;

public class FilterDialog implements ActionListener {

	private JDialog dialog;

	private JLabel nameLabel;
	private JTextField nameTextField;

	private JButton addActionButton;
	private JButton removeActionButton;

	private CloseButton closeButton;
	private HelpButton helpButton;

	private Filter filter;
	private JFrame frame;
	private CriteriaList criteriaList;
	private ActionList actionList;

	private JComboBox condList;
	private AbstractPluginHandler pluginHandler;
	
	public FilterDialog(AbstractPluginHandler pluginHandler, Filter filter) {
		dialog = DialogStore.getDialog();
		dialog.setTitle(
			MailResourceLoader.getString("dialog", "filter", "dialog_title"));
		this.filter = filter;
		this.pluginHandler = pluginHandler;

		//System.out.println("filternode name: " + filter.getName());

		initComponents();
		updateComponents(true);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void initComponents() {
		JPanel namePanel = new JPanel();
		namePanel.setBorder(BorderFactory.createEmptyBorder(12,12,10,11));
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		nameLabel = new JLabel(
				MailResourceLoader.getString(
					"dialog",
					"filter",
					"filter_description"));
		nameLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"filter",
				"filter_description"));
		namePanel.add(nameLabel);
		namePanel.add(Box.createHorizontalStrut(5));
		nameTextField = new JTextField(22);
		nameLabel.setLabelFor(nameTextField);
		namePanel.add(nameTextField);
		dialog.getContentPane().add(namePanel,BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new BorderLayout(0,0));
		centerPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0,12,10,11),
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(MailResourceLoader.getString("dialog", "filter", "if")),
						BorderFactory.createEmptyBorder(10,10,10,10))));
		
		JPanel middleIfPanel = new JPanel(new BorderLayout());
		centerPanel.add( middleIfPanel, BorderLayout.CENTER );
		
		JPanel ifPanel = new JPanel();
		ifPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		ifPanel.setLayout(new BoxLayout(ifPanel, BoxLayout.X_AXIS));

		ifPanel.add(Box.createHorizontalGlue());

		nameLabel = new JLabel(
				MailResourceLoader.getString(
					"dialog",
					"filter",
					"execute_actions"));
		nameLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"filter",
				"execute_actions"));
				
		ifPanel.add(nameLabel);

		ifPanel.add(Box.createHorizontalStrut(5));

		String[] cond = {
				MailResourceLoader.getString(
					"dialog",
					"filter",
					"all_criteria"),
				MailResourceLoader.getString(
					"dialog",
					"filter",
					"any_criteria")};
		condList = new JComboBox(cond);
		nameLabel.setLabelFor(condList);
		ifPanel.add(condList);

		middleIfPanel.add(ifPanel, BorderLayout.NORTH);

		//middleIfPanel.add(Box.createRigidArea(new java.awt.Dimension(0, 10)));

		criteriaList = new CriteriaList(pluginHandler, filter);
		//JScrollPane scrollPane = new JScrollPane( criteriaList );
		middleIfPanel.add(criteriaList, BorderLayout.CENTER);

		//rootPanel.add(middleIfPanel);

		//rootPanel.add( Box.createRigidArea( new java.awt.Dimension(0,10) ) );

		JPanel middleThenPanel = new JPanel(new BorderLayout());
		centerPanel.add( middleThenPanel, BorderLayout.SOUTH );
		//middleThenPanel.setBorder(border);
		//middleThenPanel.add( Box.createRigidArea( new java.awt.Dimension(0,5) ) );

		JPanel thenPanel = new JPanel();
		thenPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		thenPanel.setLayout(new BoxLayout(thenPanel, BoxLayout.X_AXIS));

		addActionButton = new JButton(
				MailResourceLoader.getString("dialog", "filter", "add_action"));
		addActionButton.setMnemonic(
			MailResourceLoader.getMnemonic("dialog", "filter", "add_action"));
		addActionButton.setIcon(ImageLoader.getImageIcon("stock_add_16.png"));
		addActionButton.addActionListener(this);
		addActionButton.setActionCommand("ADD_ACTION");
		//thenPanel.add(addActionButton);

		//thenPanel.add( Box.createRigidArea( new java.awt.Dimension(5,0) ) );

		JLabel actionLabel = new JLabel(MailResourceLoader.getString("dialog","filter","action_list") );
		actionLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog", "filter", "action_list"));
		thenPanel.add( Box.createRigidArea( new java.awt.Dimension(5,0) ) );
		thenPanel.add( actionLabel );
		
		thenPanel.add(Box.createHorizontalGlue());

		middleThenPanel.add(thenPanel, BorderLayout.NORTH);

		//middleThenPanel.add(Box.createRigidArea(new java.awt.Dimension(0, 10)));

		actionList = new ActionList(filter, frame);
		middleThenPanel.add(actionList, BorderLayout.CENTER);

		dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout(0,0));
		bottomPanel.setBorder(BorderFactory.createCompoundBorder(
				new WizardTopBorder(),
				BorderFactory.createEmptyBorder(17, 12, 11, 11)));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		closeButton = new CloseButton();
		closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		helpButton = new HelpButton();
		buttonPanel.add(helpButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		dialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		dialog.getRootPane().setDefaultButton(closeButton);
		dialog.getRootPane().registerKeyboardAction(this,"CLOSE",KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public void updateComponents(boolean b) {
		if (b) {
			// set component values

			criteriaList.updateComponents(b);
			actionList.updateComponents(b);

			// filter description JTextField
			nameTextField.setText(filter.getName());

			// all / match any JComboBox
			FilterRule filterRule = filter.getFilterRule();
			String value = filterRule.getCondition();
			if (value.equals("matchall"))
				condList.setSelectedIndex(0);
			else
				condList.setSelectedIndex(1);
		} else {
			// get values from components

			criteriaList.updateComponents(b);
			actionList.updateComponents(b);

			filter.setName(nameTextField.getText());

			int index = condList.getSelectedIndex();
			FilterRule filterRule = filter.getFilterRule();
			if (index == 0)
				filterRule.set("conditon", "matchall");
			else
				filterRule.set("condition","matchany");
		}

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CLOSE")) {
			updateComponents(false);
			dialog.setVisible(false);
			//frame.listView.update();
		} else if (action.equals("ADD_CRITERION")) {

			criteriaList.add();

		}
		/*
		else if ( action.equals("REMOVE_CRITERION") )
		{
		      //System.out.println( "remove" );
		
		    criteriaList.remove();
		
		}
		*/
		else if (action.equals("ADD_ACTION")) {
			//System.out.println( "add" );
			actionList.add();

		}
		/*
		else if ( action.equals("REMOVE_ACTION") )
		{
		      //System.out.println( "remove" );
		    actionList.remove();
		
		}
		*/
	}
}
