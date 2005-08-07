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
package org.columba.mail.gui.config.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.filter.FilterRule;
import org.columba.core.folder.IFolder;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.CheckBoxWithMnemonic;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.gui.util.DialogHeaderPanel;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.gui.config.filter.CriteriaList;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.frame.TreeViewOwner;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.gui.tree.util.SelectSearchFolderDialog;
import org.columba.mail.gui.tree.util.TreeNodeList;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Search message Dialog. Lets you specify a source-folder and the search
 * criteria.
 * 
 * @author fdietz
 */
public class SearchFrame extends JDialog implements ActionListener {
	private JLabel folderLabel;

	private JLabel nameLabel;

	private JTextField folderTextField;

	private JButton addButton;

	private JButton selectButton;

	private JButton searchButton;

	private JCheckBox includeSubfolderButton;

	private CriteriaList criteriaList;

	private VirtualFolder destFolder;

	private JComboBox condList;

	private IFrameMediator frameController;

	public SearchFrame(IFrameMediator frameController,
			AbstractMessageFolder searchFolder) {
		super(frameController.getView().getFrame(), true);

		this.frameController = frameController;
		this.destFolder = (VirtualFolder) searchFolder;

		setTitle(MailResourceLoader.getString("dialog", "filter",
				"searchdialog_title"));

		initComponents();
		updateComponents(true);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public SearchFrame(IFrameMediator frameController,
			AbstractMessageFolder searchFolder,
			AbstractMessageFolder sourceFolder) {
		super(frameController.getView().getFrame(), true);

		this.frameController = frameController;
		this.destFolder = (VirtualFolder) searchFolder;

		setTitle(MailResourceLoader.getString("dialog", "filter",
				"searchdialog_title"));

		initComponents();

		updateComponents(true);

		setSourceFolder(sourceFolder);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public JPanel createPanel() {

		FormLayout formlayout1 = new FormLayout("6DLU,FILL:DEFAULT:GROW(1.0)",
				"CENTER:DEFAULT:NONE,6DLU,CENTER:DEFAULT:NONE,6DLU,FILL:DEFAULT:GROW(1.0)");
		formlayout1.setRowGroups(new int[][] { { 5 } });
		CellConstraints cc = new CellConstraints();
		PanelBuilder builder = new PanelBuilder(formlayout1);
		builder.setDefaultDialogBorder();

		builder.add(createPanel1(), cc.xywh(1, 1, 2, 1));

		builder.addSeparator(MailResourceLoader.getString("dialog", "filter",
				"if"), cc.xywh(1, 3, 2, 1));

		builder.add(createPanel2(), cc.xy(2, 5));

		return builder.getPanel();
	}

	public JPanel createPanel1() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"LEFT:DEFAULT:NONE,3DLU,FILL:DEFAULT:GROW(1.0),3DLU,FILL:DEFAULT:NONE",
				"FILL:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		jpanel1.add(folderLabel, cc.xy(1, 1));

		jpanel1.add(selectButton, cc.xy(3, 1));

		jpanel1.add(includeSubfolderButton, cc.xy(5, 1));

		return jpanel1;
	}

	public JPanel createPanel2() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,3DLU,FILL:DEFAULT:NONE",
				"FILL:DEFAULT:NONE,3DLU,FILL:DEFAULT:GROW(1.0)");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		jpanel1.add(nameLabel, cc.xy(3, 1));

		jpanel1.add(condList, cc.xy(5, 1));

		jpanel1.add(criteriaList, cc.xywh(1, 3, 5, 1));

		return jpanel1;
	}

	/**
	 * init components
	 */
	protected void initComponents() {

		folderLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "filter", "choose_folder"));

		selectButton = new JButton();
		folderLabel.setLabelFor(selectButton);
		selectButton.setActionCommand("SELECT");
		selectButton.addActionListener(this);

		includeSubfolderButton = new CheckBoxWithMnemonic(MailResourceLoader
				.getString("dialog", "filter", "include_subfolders"));

		nameLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "filter", "execute_actions"));

		String[] cond = {
				MailResourceLoader
						.getString("dialog", "filter", "all_criteria"),
				MailResourceLoader
						.getString("dialog", "filter", "any_criteria") };
		condList = new JComboBox(cond);

		criteriaList = new CriteriaList(destFolder.getFilter());
		criteriaList.setPreferredSize(new Dimension(500, 100));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createPanel(), BorderLayout.CENTER);

		getContentPane().add(createBottomPanel(), BorderLayout.SOUTH);

		getContentPane().add(new DialogHeaderPanel(MailResourceLoader.getString("dialog",
				"filter", "header_title"), MailResourceLoader.getString(
				"dialog", "filter", "header_description"), ImageLoader
				.getSmallImageIcon("system-search-32.png")), BorderLayout.NORTH);
	}

	/**
	 * @param contentPane
	 */
	private JPanel createBottomPanel() {
		JPanel bottom = new JPanel(new BorderLayout());
		bottom.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		searchButton = new JButton(MailResourceLoader.getString("dialog",
				"filter", "search"));
		searchButton.setIcon(ImageLoader.getImageIcon("stock_search-16.png"));
		searchButton.addActionListener(this);
		searchButton.setActionCommand("SEARCH");
		buttonPanel.add(searchButton);

		ButtonWithMnemonic closeButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "close"));
		closeButton.addActionListener(this);
		closeButton.setActionCommand("CLOSE");
		buttonPanel.add(closeButton);

		ButtonWithMnemonic helpButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "help"));
		helpButton.addActionListener(this);
		helpButton.setActionCommand("HELP");
		helpButton.setEnabled(false);
		buttonPanel.add(helpButton);
		bottom.add(buttonPanel, BorderLayout.EAST);

		getRootPane().setDefaultButton(searchButton);
		getRootPane().registerKeyboardAction(this, "CLOSE",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		return bottom;
	}

	public void updateComponents(boolean b) {
		if (b) {
			FilterRule filterRule = destFolder.getFilter().getFilterRule();
			String value = filterRule.getCondition();

			if (value.equals("matchall")) {
				condList.setSelectedIndex(0);
			} else {
				condList.setSelectedIndex(1);
			}

			boolean isInclude = Boolean.valueOf(
					destFolder.getConfiguration().getString("property",
							"include_subfolders")).booleanValue();

			includeSubfolderButton.setSelected(isInclude);

			int uid = destFolder.getConfiguration().getInteger("property",
					"source_uid");

			AbstractMessageFolder f = (AbstractMessageFolder) FolderTreeModel
					.getInstance().getFolder(uid);

			// If f==null because of deleted AbstractMessageFolder fallback to
			// Inbox
			if (f == null) {
				uid = 101;
				destFolder.getConfiguration().setInteger("property",
						"source_uid", uid);
				f = (AbstractMessageFolder) FolderTreeModel.getInstance()
						.getFolder(uid);
			}

			selectButton.setText(f.getTreePath());

			criteriaList.updateComponents(b);
		} else {
			// get values from components
			FilterRule filterRule = destFolder.getFilter().getFilterRule();
			int index = condList.getSelectedIndex();

			if (index == 0) {
				filterRule.setCondition("matchall");
			} else {
				filterRule.setCondition("matchany");
			}

			if (includeSubfolderButton.isSelected()) {
				destFolder.getConfiguration().setString("property",
						"include_subfolders", "true");
			} else {
				destFolder.getConfiguration().setString("property",
						"include_subfolders", "false");
			}

			String path = selectButton.getText();
			TreeNodeList list = new TreeNodeList(path);
			AbstractMessageFolder folder = (AbstractMessageFolder) FolderTreeModel
					.getInstance().getFolder(list);
			int uid = folder.getUid();
			destFolder.getConfiguration().setInteger("property", "source_uid",
					uid);

			criteriaList.updateComponents(b);
		}
	}

	public void setSourceFolder(AbstractMessageFolder f) {
		selectButton.setText(f.getTreePath());
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CLOSE")) {
			updateComponents(false);
			setVisible(false);
		} else if (action.equals("ADD_CRITERION")) {
			criteriaList.add();
		} else if (action.equals("SELECT")) {
			SelectSearchFolderDialog dialog = new SelectSearchFolderDialog(
					frameController);

			if (dialog.success()) {
				IFolder folder = dialog.getSelectedFolder();
				String path = folder.getTreePath();

				selectButton.setText(path);
			}
		} else if (action.equals("SEARCH")) {
			updateComponents(false);
			setVisible(false);

			try {
				// Deactivate this vFolder because changes are about to happen
				// and the search needs to be redone.
				((VirtualFolder) destFolder).deactivate();
				((VirtualFolder) destFolder).addSearchToHistory();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			MailFolderCommandReference r = new MailFolderCommandReference(
					destFolder);
			((MailFrameMediator) frameController).setTreeSelection(r);
		}
	}
}