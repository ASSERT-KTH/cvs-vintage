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
package org.columba.mail.gui.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.core.filter.FilterCriteria;
import org.columba.core.folder.IFolder;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.CTextField;
import org.columba.core.gui.util.ComboMenu;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.filter.MailFilterFactory;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.gui.config.search.SearchFrame;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Filter toolbar lets you do quick searches on folder contents.
 * 
 * @author fdietz
 */
public class FilterToolbar extends JPanel implements ActionListener,
		ItemListener {

	public JButton clearButton;

	private JButton searchButton;

	private ComboMenu criteriaComboMenu;

	private JLabel label;

	private JTextField textField;

	private TableController tableController;

	private IFolder sourceFolder;

	private String selectedItem;

	String[] strs = new String[] { "subject_contains", "from_contains",
			"to_contains", "cc_contains", "bcc_contains", "body_contains",
			"separator", "unread_messages", "flagged_messages",
			"high_priority", "spam_message", "separator", "custom_search" };

	public FilterToolbar(TableController headerTableViewer) {
		super();
		this.tableController = headerTableViewer;

		selectedItem = strs[0];

		initComponents();
		layoutComponents();

		//textField.getDocument().addDocumentListener(new
		// MyDocumentListener());
	}

	private ComboMenu createComboMenu() {
		ComboMenu c = new ComboMenu();
		for (int i = 0; i < strs.length; i++) {
			if (strs[i].equals("separator"))
				c.addSeparator();
			else {
				c.addMenuItem(strs[i], MailResourceLoader.getString("filter",
						"filter", strs[i]));
			}
		}

		return c;
	}

	public void initComponents() {

		criteriaComboMenu = createComboMenu();
		criteriaComboMenu.addItemListener(this);

		textField = new CTextField();

		textField.addActionListener(this);
		textField.setActionCommand("TEXTFIELD");
		textField.addKeyListener(new MyKeyListener());

		clearButton = new ButtonWithMnemonic(MailResourceLoader.getString(
				"menu", "mainframe", "filtertoolbar_clear"));
		clearButton.setToolTipText(MailResourceLoader.getString("menu",
				"mainframe", "filtertoolbar_clear_tooltip"));
		clearButton.setActionCommand("CLEAR");
		clearButton.addActionListener(this);

		searchButton = new JButton("Search");
		searchButton.setActionCommand("SEARCH");
		searchButton.addActionListener(this);
		searchButton.setDefaultCapable(true);
	}

	public void layoutComponents() {
		setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));

		FormLayout l = new FormLayout(
				"default, 1dlu, fill:default:grow, 3dlu, default, 3dlu, default",
				"fill:default:grow");
		PanelBuilder b = new PanelBuilder(this, l);

		CellConstraints c = new CellConstraints();

		b.add(criteriaComboMenu, c.xy(1, 1));

		b.add(textField, c.xy(3, 1));

		b.add(searchButton, c.xy(5, 1));

		b.add(clearButton, c.xy(7, 1));

	}

	private void update() throws Exception {

	}

	private int getIndex(String name) {
		for (int i = 0; i < strs.length; i++) {
			if (name.equals(strs[i]))
				return i;
		}

		return -1;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("SEARCH")) {

			clearButton.setEnabled(true);

			executeSearch();

		} else if (action.equals("CLEAR")) {

			clearButton.setEnabled(false);

			// select search folder
			MailFolderCommandReference r = new MailFolderCommandReference(
					sourceFolder);
			((MailFrameMediator) tableController.getFrameController())
					.setTreeSelection(r);

		}

	}

	/**
	 * Execute search.
	 */
	private void executeSearch() {

		// get selected search criteria
		int index = getIndex(selectedItem);

		// create filter criteria based on selected type
		FilterCriteria c = createFilterCriteria(index);

		// get currently selected folder
		IFolder h = ((MailFrameMediator) tableController.getFrameController())
				.getTreeSelection().getSourceFolder();

		if (h.getUid() != 106)
			sourceFolder = h;

		// set criteria for search folder
		VirtualFolder searchFolder = prepareSearchFolder(c, sourceFolder);

		try {
			// add search to history
			searchFolder.addSearchToHistory();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// select search folder
		MailFolderCommandReference r = new MailFolderCommandReference(null);
		((MailFrameMediator) tableController.getFrameController())
				.setTreeSelection(r);
		r = new MailFolderCommandReference(searchFolder);
		((MailFrameMediator) tableController.getFrameController())
				.setTreeSelection(r);
	}

	/**
	 * Create new virtual folder with filter criteria settings and selected it.
	 * 
	 * @param c
	 *            filter criteria settings
	 * @return source folder
	 */
	private VirtualFolder prepareSearchFolder(FilterCriteria c, IFolder folder) {
		// get search folder
		VirtualFolder searchFolder = (VirtualFolder) FolderTreeModel
				.getInstance().getFolder(106);

		// remove old filters
		searchFolder.getFilter().getFilterRule().removeAll();

		// add filter criteria
		searchFolder.getFilter().getFilterRule().add(c);

		// don't search in subfolders recursively
		searchFolder.getConfiguration().set("property", "include_subfolders",
				"false");

		int uid = folder.getUid();

		// set source folder UID
		searchFolder.getConfiguration().set("property", "source_uid", uid);

		return searchFolder;
	}

	/**
	 * Create filter criteria, based on current selection.
	 * 
	 * @param index
	 *            selected criteria
	 * @return newly created filter criteria
	 */
	private FilterCriteria createFilterCriteria(int index) {
		String pattern = textField.getText();
		FilterCriteria c = null;
		switch (index) {

		case 0:
			c = MailFilterFactory.createSubjectContains(pattern);
			break;
		case 1:
			c = MailFilterFactory.createFromContains(pattern);
			break;
		case 2:
			c = MailFilterFactory.createToContains(pattern);
			break;
		case 3:
			c = MailFilterFactory.createCcContains(pattern);
			break;
		case 4:
			c = MailFilterFactory.createBccContains(pattern);
			break;
		case 5:
			c = MailFilterFactory.createBodyContains(pattern);
			break;
		case 7:
			c = MailFilterFactory.createUnreadMessages();
			break;
		case 8:
			c = MailFilterFactory.createFlaggedMessages();
			break;
		case 9:
			c = MailFilterFactory.createHighPriority();
			break;
		case 10:
			c = MailFilterFactory.createSpamMessages();
			break;

		}

		return c;
	}

	public void setPattern(String pattern) {
		textField.setText(pattern);
	}

	/**
	 * Execute search when pressing RETURN in the textfield.
	 * 
	 * @author fdietz
	 */
	class MyKeyListener implements KeyListener {
		public void keyTyped(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			char ch = e.getKeyChar();

			if (ch == KeyEvent.VK_ENTER) {
				executeSearch();
			}
		}
	}

	/**
	 * Execute search while user is typing pattern in textfield.
	 * 
	 * @author fdietz
	 */
	class MyDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			update();
		}

		public void removeUpdate(DocumentEvent e) {
			update();
		}

		public void changedUpdate(DocumentEvent e) {
			//Plain text components don't fire these events
		}

		public void update() {
			if (sourceFolder == null)
				return;

			/*
			 * // get selected search criteria int index =
			 * criteriaComboBox.getSelectedIndex(); // create filter criteria
			 * based on selected type FilterCriteria c =
			 * createFilterCriteria(index); // set criteria for search folder
			 * VirtualFolder searchFolder = prepareSearchFolder(c,
			 * sourceFolder); // select search folder MailFolderCommandReference
			 * r = new MailFolderCommandReference( searchFolder);
			 * ((MailFrameMediator) tableController.getFrameController())
			 * .setTreeSelection(r);
			 */
		}
	}

	/**
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent event) {
		selectedItem = (String) event.getItem();

		// enable/disable textfield in-dependency of selected criteria
		int selectedIndex = getIndex(selectedItem);
		if (selectedIndex >= 0 && selectedIndex <= 5) {
			textField.setEnabled(true);
			textField.requestFocus();
		} else {
			textField.setEnabled(false);
		}

		// execute custom search
		if (selectedItem.equals("custom_search")) {
			executeCustomSearch();
		}
	}

	/**
	 * Open the search dialog, with pre-filled settings.
	 *  
	 */
	private void executeCustomSearch() {
		AbstractMessageFolder searchFolder = (AbstractMessageFolder) FolderTreeModel
				.getInstance().getFolder(106);

		AbstractMessageFolder folder = (AbstractMessageFolder) ((MailFrameMediator) tableController
				.getFrameController()).getTableSelection().getSourceFolder();

		if (folder == null) {
			return;
		}

		SearchFrame frame = new SearchFrame(tableController
				.getFrameController(), searchFolder, folder);
	}
}