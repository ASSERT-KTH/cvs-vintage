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
package org.columba.mail.gui.config.folder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.gui.checkablelist.CheckableItem;
import org.columba.core.gui.checkablelist.CheckableItemListTableModel;
import org.columba.core.gui.checkablelist.CheckableList;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.CTabbedPane;
import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.help.HelpManager;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.command.ExportFolderCommand;
import org.columba.mail.folder.command.RenameFolderCommand;
import org.columba.mail.folder.command.SyncSearchEngineCommand;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.MessageFolderInfo;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Folder Options Dialog.
 *
 * @author fdietz
 */
public class FolderOptionsDialog
	extends JDialog
	implements ActionListener, ListSelectionListener {

	private final static String[] tooltips=
		{ "columns", "sorting", "filter", "threadedview", "selection" };

	private JPanel generalPanel;
	private JPanel advPanel;
	private Folder folder;
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JLabel totalLabel;
	private JLabel totalLabel2;
	private JLabel unreadLabel;
	private JLabel unreadLabel2;
	private JLabel recentLabel;
	private JLabel recentLabel2;
	private JLabel locationLabel;
	private JLabel locationLabel2;
	private JLabel sizeLabel;
	private JLabel sizeLabel2;
	private JButton exportButton;
	private MultiLineLabel enableLabel;
	private JCheckBox enableTextIndexingCheckBox;
	private boolean renameFolder;
	private String oldFolderName= null;
	private MultiLineLabel overwriteLabel;
	private JButton resetButton;
	private JButton enableButton;
	private JButton disableButton;

	//JCheckBox overwriteOptionsCheckBox;
	private CheckableTooltipList checkableList;

	/**
	 * Constructor
	 *
	 * @param folder                        selected folder
	 * @param renameFolder                this is a "rename folder" operation
	 */
	public FolderOptionsDialog(Folder folder, boolean renameFolder) {
		this(folder);

		this.renameFolder= renameFolder;

		oldFolderName= folder.getName();

		// focus name textfield
		if (renameFolder) {
			nameTextField.selectAll();
			nameTextField.requestFocus();
		}
	}

	/**
	 * Default constructor
	 *
	 * @param folder                selected folder
	 */
	public FolderOptionsDialog(Folder folder) {
		super();
		this.folder= folder;

		initComponents();

		updateComponents(true);

		pack();

		setLocationRelativeTo(null);

		setVisible(true);

		nameTextField.selectAll();
		nameTextField.requestFocus();
	}

	protected JPanel createGeneralPanel() {
		// Create a FormLayout instance. 
			FormLayout layout= new FormLayout("6dlu, right:max(25dlu;default), 3dlu, fill:default:grow, fill:0dlu:grow", //$NON-NLS-1$

		// 3 columns
	"pref, 3dlu, pref, 6dlu, pref, 3dlu, pref, 3dlu, pref, 6dlu, pref, 24dlu, pref, 3dlu, pref, 6dlu, default, 0dlu"); //$NON-NLS-1$

		// create a form builder
		PanelBuilder builder= new PanelBuilder(layout);
		CellConstraints cc= new CellConstraints();

		// create EmptyBorder between components and dialog-frame 
		builder.setDefaultDialogBorder();

		// Add components to the panel:
		builder.addSeparator(MailResourceLoader.getString("dialog", "folderoptions", "general_info"), cc.xywh(1, 1, 5, 1)); //$NON-NLS-1$

		builder.add(nameLabel, cc.xy(2, 3));
		builder.add(nameTextField, cc.xywh(4, 3, 2, 1));

		builder.add(totalLabel, cc.xy(2, 5));
		builder.add(totalLabel2, cc.xy(4, 5));

		builder.add(unreadLabel, cc.xy(2, 7));
		builder.add(unreadLabel2, cc.xy(4, 7));

		builder.add(recentLabel, cc.xy(2, 9));
		builder.add(unreadLabel2, cc.xy(4, 9));

		builder.add(sizeLabel, cc.xy(2, 11));
		builder.add(sizeLabel2, cc.xy(4, 11));

		builder.appendGlueRow();

		builder.addSeparator(MailResourceLoader.getString("dialog", "folderoptions", "archiving_messages"), cc.xywh(1, 13, 5, 1)); //$NON-NLS-1$

		builder.add(locationLabel, cc.xy(2, 15));
		builder.add(locationLabel2, cc.xy(4, 15));

		JPanel panel= new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(exportButton, BorderLayout.EAST);
		builder.add(panel, cc.xywh(4, 17, 2, 1));

		return builder.getPanel();
	}

	/**
	 * Create advanced panel.
	 * <p>
	 * TODO: following ui guidelines we should add an enable and a disable button
	 *       right beside the checklist, for a better accessibility
	 * 
	 * @return      panel
	 */
	protected JPanel createAdvancedPanel() {
		// Create a FormLayout instance. 
			FormLayout layout= new FormLayout("fill:default:grow, 6px, default", //$NON-NLS-1$

		// 3 columns
	"pref, 6px, fill:pref:grow"); //$NON-NLS-1$

		// create a form builder
		PanelBuilder builder= new PanelBuilder(layout);
		CellConstraints cc= new CellConstraints();

		// create EmptyBorder between components and dialog-frame 
		builder.setDefaultDialogBorder();

		builder.add(overwriteLabel, cc.xywh(1, 1, 3, 1));

		JScrollPane sp= new JScrollPane(checkableList);
		sp.setPreferredSize(new Dimension(200, 200));
		sp.getViewport().setBackground(Color.white);
		builder.add(sp, cc.xy(1, 3));

		ButtonStackBuilder b= new ButtonStackBuilder();
		b.addGridded(enableButton);
		b.addRelatedGap();
		b.addGridded(disableButton);
		b.addUnrelatedGap();
		b.addGlue();
		b.addFixed(resetButton);

		JPanel buttonPanel= b.getPanel();
		builder.add(buttonPanel, cc.xy(3, 3));

		/*
		JPanel panel= new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(resetButton, BorderLayout.EAST);
		builder.add(panel, cc.xywh(5, 7, 1, 1));
		*/

		/*
		builder.addSeparator("Full-text indexing");
		
		builder.add(enableLabel, cc.xywh(1, 7, 5, 1));
		builder.add(enableTextIndexingCheckBox, cc.xywh(2, 9, 4, 1));
		*/
		return builder.getPanel();
	}

	protected void initComponents() {
		Font boldFont= (Font) UIManager.get("Label.font"); //$NON-NLS-1$
		boldFont= boldFont.deriveFont(Font.BOLD);

		nameLabel= new JLabel(MailResourceLoader.getString("dialog", "folderoptions", "name")); //$NON-NLS-1$
		nameLabel.setFont(boldFont);
		nameTextField= new JTextField();

		totalLabel= new JLabel(MailResourceLoader.getString("dialog", "folderoptions", "total")); //$NON-NLS-1$
		totalLabel.setFont(boldFont);
		totalLabel2= new JLabel("0"); //$NON-NLS-1$

		unreadLabel= new JLabel(MailResourceLoader.getString("dialog", "folderoptions", "unread")); //$NON-NLS-1$
		unreadLabel.setFont(boldFont);
		unreadLabel2= new JLabel("0"); //$NON-NLS-1$

		recentLabel= new JLabel(MailResourceLoader.getString("dialog", "folderoptions", "recent")); //$NON-NLS-1$
		recentLabel.setFont(boldFont);
		recentLabel2= new JLabel("0"); //$NON-NLS-1$

		sizeLabel= new JLabel(MailResourceLoader.getString("dialog", "folderoptions", "mailbox_size")); //$NON-NLS-1$
		sizeLabel.setFont(boldFont);
		sizeLabel2= new JLabel("2"); //$NON-NLS-1$

		locationLabel= new JLabel(MailResourceLoader.getString("dialog", "folderoptions", "location")); //$NON-NLS-1$
		locationLabel.setFont(boldFont);
		locationLabel2= new JLabel(""); //$NON-NLS-1$

		exportButton= new JButton(MailResourceLoader.getString("dialog", "folderoptions", "export")); //$NON-NLS-1$
		exportButton.setActionCommand("EXPORT"); //$NON-NLS-1$
		exportButton.addActionListener(this);

		enableTextIndexingCheckBox= new JCheckBox(MailResourceLoader.getString("dialog", "folderoptions", "enable_full-text_indexing")); //$NON-NLS-1$

		enableLabel= new MultiLineLabel(MailResourceLoader.getString("dialog", "folderoptions", "this_is_an_experimental_feature")); //$NON-NLS-1$
		enableLabel.setFont(boldFont);

			overwriteLabel= new MultiLineLabel(MailResourceLoader.getString("dialog", "folderoptions", "select_individual_options"), //$NON-NLS-1$
	250);

		resetButton= new ButtonWithMnemonic(MailResourceLoader.getString("dialog", "folderoptions", "reset")); //$NON-NLS-1$
		resetButton.setActionCommand("RESET"); //$NON-NLS-1$
		resetButton.addActionListener(this);

		enableButton= new ButtonWithMnemonic(MailResourceLoader.getString("dialog", "folderoptions", "overwrite")); //$NON-NLS-1$
		enableButton.setActionCommand("ENABLED"); //$NON-NLS-1$
		enableButton.addActionListener(this);
		disableButton= new ButtonWithMnemonic(MailResourceLoader.getString("dialog", "folderoptions", "use_default")); //$NON-NLS-1$
		disableButton.setActionCommand("DISABLED"); //$NON-NLS-1$
		disableButton.addActionListener(this);

		/*
		overwriteOptionsCheckBox = new JCheckBox("Overwrite global settings");
		overwriteOptionsCheckBox.addActionListener(this);
		overwriteOptionsCheckBox.setActionCommand("OVERWRITE");
		*/
		checkableList= new CheckableTooltipList();
		checkableList.getSelectionModel().addListSelectionListener(this);

		CTabbedPane tp= new CTabbedPane();
		tp.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

		tp.add(MailResourceLoader.getString("dialog", "folderoptions", "general_options"), createGeneralPanel()); //$NON-NLS-1$
		tp.add(MailResourceLoader.getString("dialog", "folderoptions", "advanced"), createAdvancedPanel()); //$NON-NLS-1$

		getContentPane().add(tp, BorderLayout.CENTER);

		getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
		getRootPane().registerKeyboardAction(this, "CANCEL", //$NON-NLS-1$
		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(this, "HELP", //$NON-NLS-1$
		KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	protected JPanel createButtonPanel() {
		JPanel bottom= new JPanel();
		bottom.setLayout(new BorderLayout());
		//bottom.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		//bottom.setLayout( new BoxLayout( bottom, BoxLayout.X_AXIS ) );
		//bottom.add( Box.createHorizontalStrut());
		ButtonWithMnemonic cancelButton= new ButtonWithMnemonic(MailResourceLoader.getString("global", "cancel")); //$NON-NLS-1$ //$NON-NLS-2$

		//$NON-NLS-1$ //$NON-NLS-2$
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

		ButtonWithMnemonic okButton= new ButtonWithMnemonic(MailResourceLoader.getString("global", "ok")); //$NON-NLS-1$ //$NON-NLS-2$

		//$NON-NLS-1$ //$NON-NLS-2$
		okButton.addActionListener(this);
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);

		ButtonWithMnemonic helpButton= new ButtonWithMnemonic(MailResourceLoader.getString("global", "help")); //$NON-NLS-1$ //$NON-NLS-2$

		// associate with JavaHelp
		HelpManager.enableHelpOnButton(helpButton, "folder_options"); //$NON-NLS-1$

		JPanel buttonPanel= new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		buttonPanel.setLayout(new GridLayout(1, 3, 6, 0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(helpButton);

		//bottom.add( Box.createHorizontalGlue() );
		bottom.add(buttonPanel, BorderLayout.EAST);

		return bottom;
	}

	public void updateComponents(boolean b) {
		if (b) {
			MessageFolderInfo info= folder.getMessageFolderInfo();

			nameTextField.setText(folder.getName());

			totalLabel2.setText(Integer.toString(info.getExists()));
			unreadLabel2.setText(Integer.toString(info.getUnseen()));
			recentLabel2.setText(Integer.toString(info.getRecent()));

			locationLabel2.setText(folder.getDirectoryFile().getPath());

			FolderItem item= folder.getFolderItem();
			XmlElement property= item.getElement("property"); //$NON-NLS-1$

			CheckableItemListTableModel model=
				new CheckableItemListTableModel();

			for (int i= 0; i < property.count(); i++) {
				OptionsItem optionsItem=
					new OptionsItem(
						(XmlElement) property.getElement(i).clone());

				model.addElement(optionsItem);
			}

			checkableList.setModel(model);

			/*
			if (property.getAttribute("overwrite_default_settings", "false")
			                .equals("true")) {
			    overwriteOptionsCheckBox.setSelected(true);
			} else {
			    overwriteOptionsCheckBox.setSelected(false);
			}
			*/

			// only local folders have an full-text indexing capability
			if (folder instanceof LocalFolder) {
				item= folder.getFolderItem();

				boolean bool= item.getBoolean("property", "enable_lucene"); //$NON-NLS-1$ //$NON-NLS-2$

				enableTextIndexingCheckBox.setSelected(bool);
			} else {
				enableTextIndexingCheckBox.setEnabled(false);
			}
		} else {
			if (renameFolder) {
				if (!oldFolderName.equals(nameTextField.getText())) {
					// user changed folder name
					FolderCommandReference[] r= new FolderCommandReference[1];
					r[0]= new FolderCommandReference(folder);
					r[0].setFolderName(nameTextField.getText());
					MainInterface.processor.addOp(new RenameFolderCommand(r));
				}
			}

			FolderItem item= folder.getFolderItem();
			XmlElement property= item.getElement("property"); //$NON-NLS-1$

			// remove all old elements
			property.removeAllElements();

			CheckableItemListTableModel model= (CheckableItemListTableModel) checkableList.getModel();

			for (int i= 0; i < model.count(); i++) {
				OptionsItem optionsItem= (OptionsItem) model.getElement(i);

				// add new element
				property.addElement(optionsItem.getElement());
			}

			/*
			item.set("property", "overwrite_default_settings",
			    overwriteOptionsCheckBox.isSelected());
			
			
			XmlElement table = property.getElement("table");
			
			if (table == null) {
			    // create default table
			    // use copy of global options as default
			    table = (XmlElement) MailConfig.get("options")
			                                   .getElement("/options/gui/table")
			                                   .clone();
			    property.addElement(table);
			}
			*/

			//	only local folders have an full-text indexing capability
			if (folder instanceof LocalFolder) {
				item= folder.getFolderItem();

				boolean bool= enableTextIndexingCheckBox.isSelected();
				item.set("property", "enable_lucene", bool); //$NON-NLS-1$ //$NON-NLS-2$

				// cast to Local Folder is safe here
				LocalFolder localFolder= (LocalFolder) folder;

				DefaultSearchEngine engine= null;

				if (bool) {
					//engine = new LuceneQueryEngine(localFolder);
					localFolder.setSearchEngine(null);

					// execute resyncing command
					FolderCommandReference[] r= new FolderCommandReference[1];
					r[0]= new FolderCommandReference(folder);
					MainInterface.processor.addOp(
						new SyncSearchEngineCommand(r));
				} else {
					//engine = new LocalSearchEngine(localFolder);
					localFolder.setSearchEngine(null);
				}
			}
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		String action= arg0.getActionCommand();

		if (action.equals("CANCEL")) { //$NON-NLS-1$
			setVisible(false);
		} else if (action.equals("OK")) { //$NON-NLS-1$
			setVisible(false);
			updateComponents(false);
		} else if (action.equals("EXPORT")) { //$NON-NLS-1$
			File destFile= null;

			// ask the user about the destination file
			JFileChooser chooser= new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);

			int result= chooser.showSaveDialog(this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File file= chooser.getSelectedFile();

				destFile= file;
			} else {
				return;
			}

			setVisible(false);

			FolderCommandReference[] r= new FolderCommandReference[1];
			r[0]= new FolderCommandReference(folder);
			r[0].setDestFile(destFile);
			MainInterface.processor.addOp(new ExportFolderCommand(r));
		} else if (action.equals("RESET")) { //$NON-NLS-1$
			FolderItem item= folder.getFolderItem();
			XmlElement property= item.getElement("property"); //$NON-NLS-1$

			// reset all options 
			for (int i= 0; i < property.count(); i++) {
				XmlElement child= property.getElement(i);
				child.addAttribute("overwrite", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// update list view
			CheckableItemListTableModel model=
				new CheckableItemListTableModel();
			for (int i= 0; i < property.count(); i++) {
				OptionsItem optionsItem=
					new OptionsItem(
						(XmlElement) property.getElement(i).clone());

				model.addElement(optionsItem);
			}

			checkableList.setModel(model);
		} else if (action.equals("ENABLED")) { //$NON-NLS-1$
			CheckableItem item= (CheckableItem) checkableList.getSelected();
			item.setSelected(!item.isSelected());
			((CheckableItemListTableModel) checkableList.getModel()).updateRow(
				item);
			updateButtonState(item.isSelected());

		} else if (action.equals("DISABLED")) { //$NON-NLS-1$
			CheckableItem item= (CheckableItem) checkableList.getSelected();
			item.setSelected(!item.isSelected());
			((CheckableItemListTableModel) checkableList.getModel()).updateRow(
				item);
			updateButtonState(item.isSelected());
		}
	}

	private void updateButtonState(boolean selected) {
		if (selected) {
			enableButton.setEnabled(false);
			disableButton.setEnabled(true);
		} else {
			enableButton.setEnabled(true);
			disableButton.setEnabled(false);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		DefaultListSelectionModel theList=
			(DefaultListSelectionModel) e.getSource();

		if (!theList.isSelectionEmpty()) {
			int index= theList.getAnchorSelectionIndex();

			CheckableItem item= (CheckableItem) checkableList.getSelected();
			updateButtonState(item.isSelected());
		}
	}

	class CheckableTooltipList extends CheckableList {
		public CheckableTooltipList() {
			super();
		}
		public String getToolTipText(MouseEvent event) {
			int row= rowAtPoint(event.getPoint());
			int col= columnAtPoint(event.getPoint());

			String s=
				MailResourceLoader.getString(
					"dialog",
					"folderoptions",
					tooltips[row]);
			return s;
		}

		public Point getToolTipLocation(MouseEvent event) {
			int row= rowAtPoint(event.getPoint());
			int col= columnAtPoint(event.getPoint());
			Object o= getValueAt(row, col);
			if (o == null)
				return null;
			if (o.toString().equals(""))
				return null;
			Point pt= getCellRect(row, col, true).getLocation();
			pt.translate(-1, -2);
			return pt;
		}

	}
}
