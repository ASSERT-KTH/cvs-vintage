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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.help.HelpManager;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.command.ExportFolderCommand;
import org.columba.mail.folder.command.RenameFolderCommand;
import org.columba.mail.folder.command.SyncSearchEngineCommand;
import org.columba.mail.folder.search.AbstractSearchEngine;
import org.columba.mail.folder.search.LocalSearchEngine;
import org.columba.mail.folder.search.LuceneSearchEngine;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.MessageFolderInfo;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Folder Options Dialog.
 *
 * @author fdietz
 */
public class FolderOptionsDialog extends JDialog implements ActionListener {

	JPanel generalPanel;
	JPanel advPanel;

	Folder folder;

	JLabel nameLabel;
	JTextField nameTextField;

	JLabel totalLabel;
	JLabel totalLabel2;
	JLabel unreadLabel;
	JLabel unreadLabel2;
	JLabel recentLabel;
	JLabel recentLabel2;

	JLabel locationLabel;
	JLabel locationLabel2;

	JLabel sizeLabel;
	JLabel sizeLabel2;

	JButton exportButton;

	MultiLineLabel enableLabel;
	JCheckBox enableTextIndexingCheckBox;

	boolean renameFolder;
	String oldFolderName = null;

	/**
	 * Constructor
	 * 
	 * @param folder			selected folder
	 * @param renameFolder		this is a "rename folder" operation
	 */
	public FolderOptionsDialog(Folder folder, boolean renameFolder) {
		this(folder);

		this.renameFolder = renameFolder;

		oldFolderName = folder.getName();

		// focus name textfield
		if (renameFolder) {
			nameTextField.selectAll();
			nameTextField.requestFocus();
		}

	}

	/**
	 * Default constructor 
	 * 
	 * @param folder		selected folder
	 */
	public FolderOptionsDialog(Folder folder) {
		super();
		this.folder = folder;

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
		FormLayout layout =
			new FormLayout(
				"6dlu, right:max(25dlu;default), 3dlu, fill:default:grow, fill:0dlu:grow",
			// 3 columns
	"pref, 3dlu, pref, 6dlu, pref, 3dlu, pref, 3dlu, pref, 6dlu, pref, 24dlu, pref, 3dlu, pref, 6dlu, default, 0dlu");

		// create a form builder
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		// create EmptyBorder between components and dialog-frame 
		builder.setDefaultDialogBorder();

		// Add components to the panel:
		builder.addSeparator("General Info", cc.xywh(1, 1, 5, 1));

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

		builder.addSeparator("Archiving Messages", cc.xywh(1, 13, 5, 1));

		builder.add(locationLabel, cc.xy(2, 15));
		builder.add(locationLabel2, cc.xy(4, 15));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(exportButton, BorderLayout.EAST);
		builder.add(panel, cc.xywh(4, 17, 2, 1));

		return builder.getPanel();
	}

	protected JPanel createAdvancedPanel() {
		// Create a FormLayout instance. 
		FormLayout layout =
			new FormLayout(
				"6dlu, right:max(25dlu;default), 3dlu, default, fill:0dlu:grow",
			// 3 columns
	"pref, 3dlu, pref, 6dlu, pref, 3dlu, 0dlu");

		// create a form builder
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		// create EmptyBorder between components and dialog-frame 
		builder.setDefaultDialogBorder();

		builder.addSeparator("Full-text indexing");

		builder.add(enableLabel, cc.xywh(1, 3, 5, 1));
		builder.add(enableTextIndexingCheckBox, cc.xywh(2, 5, 4, 1));

		return builder.getPanel();
	}

	protected void initComponents() {
		Font boldFont = (Font) UIManager.get("Label.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		nameLabel = new JLabel("Name:");
		nameLabel.setFont(boldFont);
		nameTextField = new JTextField();

		totalLabel = new JLabel("Total:");
		totalLabel.setFont(boldFont);
		totalLabel2 = new JLabel("0");

		unreadLabel = new JLabel("Unread:");
		unreadLabel.setFont(boldFont);
		unreadLabel2 = new JLabel("0");

		recentLabel = new JLabel("Recent:");
		recentLabel.setFont(boldFont);
		recentLabel2 = new JLabel("0");

		sizeLabel = new JLabel("Mailbox Size:");
		sizeLabel.setFont(boldFont);
		sizeLabel2 = new JLabel("2");

		locationLabel = new JLabel("Location:");
		locationLabel.setFont(boldFont);
		locationLabel2 = new JLabel("");

		exportButton = new JButton("Export...");
		exportButton.setActionCommand("EXPORT");
		exportButton.addActionListener(this);

		enableTextIndexingCheckBox = new JCheckBox("Enable full-text indexing");

		enableLabel =
			new MultiLineLabel("This is an experimental feature. Enable this only if you know what your are doing!");
		enableLabel.setFont(boldFont);

		/*
		CTabbedPane tp = new CTabbedPane();
		tp.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		tp.add("General Options", createGeneralPanel());
		tp.add("Advanced", createAdvancedPanel());
		*/

		getContentPane().add(createGeneralPanel(), BorderLayout.CENTER);

		getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
		getRootPane().registerKeyboardAction(
			this,
			"CANCEL",
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(
			this,
			"HELP",
			KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	protected JPanel createButtonPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
		//bottom.setLayout( new BoxLayout( bottom, BoxLayout.X_AXIS ) );

		//bottom.add( Box.createHorizontalStrut());

		JButton cancelButton =
			new JButton(MailResourceLoader.getString("global", "cancel"));
		//$NON-NLS-1$ //$NON-NLS-2$
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

		JButton okButton =
			new JButton(MailResourceLoader.getString("global", "ok"));
		//$NON-NLS-1$ //$NON-NLS-2$
		okButton.addActionListener(this);
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);

		JButton helpButton =
			new JButton(MailResourceLoader.getString("global", "help"));
		// associate with JavaHelp
		HelpManager.enableHelpOnButton(helpButton, "folder_options");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 12, 11, 11));
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(helpButton);

		//bottom.add( Box.createHorizontalGlue() );

		bottom.add(buttonPanel, BorderLayout.EAST);

		return bottom;
	}

	public void updateComponents(boolean b) {
		if (b) {
			MessageFolderInfo info = folder.getMessageFolderInfo();

			nameTextField.setText(folder.getName());

			totalLabel2.setText(Integer.toString(info.getExists()));
			unreadLabel2.setText(Integer.toString(info.getUnseen()));
			recentLabel2.setText(Integer.toString(info.getRecent()));

			locationLabel2.setText(folder.getDirectoryFile().getPath());

			// only local folders have an full-text indexing capability
			if (folder instanceof LocalFolder) {
				FolderItem item = folder.getFolderItem();
				boolean bool = item.getBoolean("property", "enable_lucene");

				enableTextIndexingCheckBox.setSelected(bool);
			} else
				enableTextIndexingCheckBox.setEnabled(false);

		} else {
			if (renameFolder) {
				if (!oldFolderName.equals(nameTextField.getText())) {
					// user changed folder name

					FolderCommandReference[] r = new FolderCommandReference[1];
					r[0] = new FolderCommandReference(folder);
					r[0].setFolderName(nameTextField.getText());
					MainInterface.processor.addOp(new RenameFolderCommand(r));
				}
			}

			//	only local folders have an full-text indexing capability
			if (folder instanceof LocalFolder) {

				FolderItem item = folder.getFolderItem();
				boolean bool = enableTextIndexingCheckBox.isSelected();
				item.set("property", "enable_lucene", bool);

				// cast to Local Folder is safe here
				LocalFolder localFolder = (LocalFolder) folder;

				AbstractSearchEngine engine = null;
				if (bool) {
					engine = new LuceneSearchEngine(localFolder);
					localFolder.setSearchEngine(engine);

					// execute resyncing command
					FolderCommandReference[] r = new FolderCommandReference[1];
					r[0] = new FolderCommandReference(folder);
					MainInterface.processor.addOp(
						new SyncSearchEngineCommand(r));
				} else {
					engine = new LocalSearchEngine(localFolder);
					localFolder.setSearchEngine(engine);
				}

			}

		}
	}

	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		if (action.equals("CANCEL")) {

			setVisible(false);
		} else if (action.equals("OK")) {

			setVisible(false);
			updateComponents(false);
		} else if (action.equals("EXPORT")) {

			File destFile = null;

			// ask the user about the destination file
			JFileChooser chooser = new JFileChooser();

			int result = chooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();

				destFile = file;
			}

			setVisible(false);

			FolderCommandReference[] r = new FolderCommandReference[1];
			r[0] = new FolderCommandReference(folder);
			r[0].setDestFile(destFile);
			MainInterface.processor.addOp(new ExportFolderCommand(r));
		}

	}

}
