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
package org.columba.addressbook.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.gui.list.AddressbookDNDListView;
import org.columba.addressbook.gui.list.AddressbookListModel;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.model.HeaderItem;
import org.columba.addressbook.model.HeaderItemList;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.DoubleClickListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SelectAddressDialog extends JDialog implements ActionListener {
  
	// recipient lists
	private AddressbookDNDListView toList;

	private AddressbookDNDListView ccList;

	private AddressbookDNDListView bccList;

	// addressbook list
	private AddressbookDNDListView addressbook;

	// action buttons for moving contacts
	private JButton toButton;

	private JButton ccButton;

	private JButton bccButton;

	private JButton toRemoveButton;

	private JButton ccRemoveButton;

	private JButton bccRemoveButton;

	// labels
	private JLabel toLabel;

	private JLabel ccLabel;

	private JLabel bccLabel;

	// choose addressbook
	private JButton chooseButton;

	private JLabel chooseLabel;

	// models for addressbook/recipients lists
	private AddressbookListModel[] dialogAddressbookListModel;

	private HeaderItemList[] headerItemList;

	private ButtonWithMnemonic cancelButton;

	private ButtonWithMnemonic okButton;

	private boolean success;

	public SelectAddressDialog(JFrame frame, HeaderItemList[] list) {
		super(frame, true);

		setTitle(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "title"));

		this.headerItemList = list;

		dialogAddressbookListModel = new AddressbookListModel[3];

		initComponents();

		layoutComponents();

		AbstractFolder folder = (AbstractFolder) AddressbookInterface.addressbookTreeModel
				.getFolder(101);
		try {
			addressbook.setHeaderItemList(folder.getHeaderItemList());
		} catch (Exception ex) {

		}

		pack();
		setLocationRelativeTo(null);

		setVisible(true);
	}

	public HeaderItemList[] getHeaderItemLists() {
		return headerItemList;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout(
				"fill:default:grow, 6px, fill:default:grow, 6px, fill:default:grow", //$NON-NLS-1$
				"default"); //$NON-NLS-1$

		PanelBuilder builder = new PanelBuilder(panel, layout);
		CellConstraints cc = new CellConstraints();

		builder.add(toButton, cc.xy(1, 1));
		builder.add(ccButton, cc.xy(3, 1));
		builder.add(bccButton, cc.xy(5, 1));
		
		return panel;
	}
	private JPanel createRemoveButtonPanel() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout(
				"fill:default:grow, 6px, fill:default:grow, 6px, fill:default:grow", //$NON-NLS-1$
				"default"); //$NON-NLS-1$

		PanelBuilder builder = new PanelBuilder(panel, layout);
		CellConstraints cc = new CellConstraints();

		builder.add(toRemoveButton, cc.xy(1, 1));
		builder.add(ccRemoveButton, cc.xy(3, 1));
		builder.add(bccRemoveButton, cc.xy(5, 1));
		
		return panel;
	}

	private JPanel createAddressbookPanel() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout("default, 6px, fill:default:grow", //$NON-NLS-1$
				"default, 12px, fill:default:grow, 6px, default, 6px, default"); //$NON-NLS-1$

		PanelBuilder builder = new PanelBuilder(panel, layout);
		CellConstraints cc = new CellConstraints();

		builder.add(chooseLabel, cc.xy(1, 1));
		builder.add(chooseButton, cc.xy(3, 1));
		builder.add(new JScrollPane(addressbook), cc.xywh(1, 3, 3, 1));
		builder.add(createButtonPanel(), cc.xywh(1, 5, 3, 1));
		builder.add(createRemoveButtonPanel(), cc.xywh(1, 7, 3, 1));

		return panel;
	}

	private JPanel createRecipientsPanel() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout(
				"fill:default:grow", //$NON-NLS-1$
				"default, 6px, fill:default:grow, 12px, default, 6px, fill:default:grow, 12px, default, 6px, fill:default:grow"); //$NON-NLS-1$

		PanelBuilder builder = new PanelBuilder(panel, layout);
		CellConstraints cc = new CellConstraints();

		int y = 1;
		builder.add(toLabel, cc.xy(1, y));
		y += 2;
		builder.add(new JScrollPane(toList), cc.xy(1, y));
		y += 2;
		builder.add(ccLabel, cc.xy(1, y));
		y += 2;
		builder.add(new JScrollPane(ccList), cc.xy(1, y));
		y += 2;
		builder.add(bccLabel, cc.xy(1, y));
		y += 2;
		builder.add(new JScrollPane(bccList), cc.xy(1, y));

		return panel;
	}

	private void layoutComponents() {
		getContentPane().setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		FormLayout layout = new FormLayout(
				"fill:default:grow, 12px, fill:default:grow", //$NON-NLS-1$
				"fill:default:grow"); //$NON-NLS-1$

		CellConstraints cc = new CellConstraints();
		mainPanel.setLayout(layout);

		mainPanel.add(createAddressbookPanel(), cc.xy(1, 1));
		mainPanel.add(createRecipientsPanel(), cc.xy(3, 1));

		getContentPane().add(mainPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		buttonPanel.add(okButton);

		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}

	private void initComponents() {
		toLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "to")); //$NON-NLS-1$

		dialogAddressbookListModel[0] = new AddressbookListModel();
		dialogAddressbookListModel[0].setHeaderItemList(headerItemList[0]);
		toList = new AddressbookDNDListView(dialogAddressbookListModel[0]);
		toList.setMinimumSize(new Dimension(150, 150));
		toList.addMouseListener(new DoubleClickListener()
		                        {
		  												
		  												public void doubleClick(MouseEvent ev)
		  												{
	  													  actionPerformed(new ActionEvent(toList,0,"TO_REMOVE"));
		  												}
		  
		                        });

		ccLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "cc")); //$NON-NLS-1$

		dialogAddressbookListModel[1] = new AddressbookListModel();
		dialogAddressbookListModel[1].setHeaderItemList(headerItemList[1]);
		ccList = new AddressbookDNDListView(dialogAddressbookListModel[1]);
		ccList.setMinimumSize(new Dimension(150, 150));
		ccList.addMouseListener(new DoubleClickListener()
		                        {
		  												
		  												public void doubleClick(MouseEvent ev)
		  												{
	  													  actionPerformed(new ActionEvent(ccList,0,"CC_REMOVE"));
		  												}
		  
		                        });

		bccLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "bcc")); //$NON-NLS-1$

		dialogAddressbookListModel[2] = new AddressbookListModel();
		dialogAddressbookListModel[2].setHeaderItemList(headerItemList[2]);
		bccList = new AddressbookDNDListView(dialogAddressbookListModel[2]);
		bccList.setMinimumSize(new Dimension(150, 150));
		bccList.addMouseListener(new DoubleClickListener()
		                        {
		  												
		  												public void doubleClick(MouseEvent ev)
		  												{
	  													  actionPerformed(new ActionEvent(bccList,0,"BCC_REMOVE"));
		  												}
		  
		                        });

		toButton = new JButton(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "right_arrow_to")); //$NON-NLS-1$
		toButton.addActionListener(this);
		toButton.setActionCommand("TO"); //$NON-NLS-1$

		toRemoveButton = new JButton(AddressbookResourceLoader.getString(
				"dialog", "selectaddressdialog", "left_arrow_to")); //$NON-NLS-1$
		toRemoveButton.addActionListener(this);
		toRemoveButton.setActionCommand("TO_REMOVE"); //$NON-NLS-1$

		ccButton = new JButton(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "right_arrow_cc")); //$NON-NLS-1$
		ccButton.addActionListener(this);
		ccButton.setActionCommand("CC"); //$NON-NLS-1$

		ccRemoveButton = new JButton(AddressbookResourceLoader.getString(
				"dialog", "selectaddressdialog", "left_arrow_cc")); //$NON-NLS-1$
		ccRemoveButton.addActionListener(this);
		ccRemoveButton.setActionCommand("CC_REMOVE"); //$NON-NLS-1$

		bccButton = new JButton(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "right_arrow_bcc")); //$NON-NLS-1$
		bccButton.addActionListener(this);
		bccButton.setActionCommand("BCC"); //$NON-NLS-1$

		bccRemoveButton = new JButton(AddressbookResourceLoader.getString(
				"dialog", "selectaddressdialog", "left_arrow_bcc")); //$NON-NLS-1$
		bccRemoveButton.addActionListener(this);
		bccRemoveButton.setActionCommand("BCC_REMOVE"); //$NON-NLS-1$

		chooseLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
				"selectaddressdialog", "addressbook")); //$NON-NLS-1$

		chooseButton = new JButton(AddressbookResourceLoader.getString(
				"dialog", "selectaddressdialog", "personal_addressbook")); //$NON-NLS-1$
		chooseButton.setActionCommand("CHOOSE"); //$NON-NLS-1$
		chooseButton.addActionListener(this);

		addressbook = new AddressbookDNDListView();
		addressbook.setMinimumSize(new Dimension(450, 200));
		addressbook.setAcceptDrop(false);
		addressbook.addMouseListener(new DoubleClickListener()
		                        {
		  												
		  												public void doubleClick(MouseEvent ev)
		  												{
	  													  actionPerformed(new ActionEvent(addressbook,0,"TO"));
		  												}
		  
		                        });
		
		okButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
				"global", "ok")); //$NON-NLS-1$ //$NON-NLS-2$
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.addActionListener(this);

		cancelButton = new ButtonWithMnemonic(AddressbookResourceLoader
				.getString("global", "cancel")); //$NON-NLS-1$ //$NON-NLS-2$
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$
		cancelButton.addActionListener(this);

		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this, "CANCEL", //$NON-NLS-1$
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	/**
	 * @return Returns the success.
	 */
	public boolean isSuccess() {
		return success;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("CANCEL")) { //$NON-NLS-1$
			setVisible(false);
		} else if (command.equals("OK")) { //$NON-NLS-1$
			setVisible(false);

			success = true;
			for (int i = 0; i < 3; i++) {
				Object[] array = dialogAddressbookListModel[i].toArray();
				headerItemList[i].clear();

				System.out.println("array-size=" + array.length); //$NON-NLS-1$

				for (int j = 0; j < array.length; j++) {
					HeaderItem item = (HeaderItem) array[j];

					/*
					 * if (item.isContact()) { String address = (String)
					 * item.get("email;internet"); //$NON-NLS-1$
					 * System.out.println("old address:" + address);
					 * //$NON-NLS-1$
					 * 
					 * if (address == null) { address = ""; //$NON-NLS-1$ } }
					 */

					if (i == 0) {
						item.setHeader("To");

					} else if (i == 1) {
						item.setHeader("Cc");
					} else if (i == 2) {
						item.setHeader("Bcc");
					}

					headerItemList[i].add((HeaderItem) item.clone());

				}
			}
		} else if (command.equals("TO")) { //$NON-NLS-1$

			int[] array = addressbook.getSelectedIndices();
			ListModel model = addressbook.getModel();
			HeaderItem item;

			for (int j = 0; j < array.length; j++) {
				item = (HeaderItem) model.getElementAt(array[j]);
				dialogAddressbookListModel[0].addElement((HeaderItem) item
						.clone());
			}
		} else if (command.equals("CC")) { //$NON-NLS-1$

			int[] array = addressbook.getSelectedIndices();
			ListModel model = addressbook.getModel();
			HeaderItem item;

			for (int j = 0; j < array.length; j++) {
				item = (HeaderItem) model.getElementAt(array[j]);
				dialogAddressbookListModel[1].addElement((HeaderItem) item
						.clone());
			}
		} else if (command.equals("BCC")) { //$NON-NLS-1$

			int[] array = addressbook.getSelectedIndices();
			ListModel model = addressbook.getModel();
			HeaderItem item;

			for (int j = 0; j < array.length; j++) {
				item = (HeaderItem) model.getElementAt(array[j]);
				dialogAddressbookListModel[2].addElement((HeaderItem) item
						.clone());
			}
		} else if (command.equals("TO_REMOVE")) { //$NON-NLS-1$

			Object[] array = toList.getSelectedValues();

			for (int j = 0; j < array.length; j++) {
				dialogAddressbookListModel[0]
						.removeElement((HeaderItem) array[j]);
			}
		} else if (command.equals("CC_REMOVE")) { //$NON-NLS-1$

			Object[] array = ccList.getSelectedValues();

			for (int j = 0; j < array.length; j++) {
				dialogAddressbookListModel[1]
						.removeElement((HeaderItem) array[j]);
			}
		} else if (command.equals("BCC_REMOVE")) { //$NON-NLS-1$

			Object[] array = bccList.getSelectedValues();

			for (int j = 0; j < array.length; j++) {
				dialogAddressbookListModel[2]
						.removeElement((HeaderItem) array[j]);
			}
		} else if (command.equals("CHOOSE")) { //$NON-NLS-1$

			SelectAddressbookFolderDialog dialog = AddressbookInterface.addressbookTreeModel
					.getSelectAddressbookFolderDialog();

			AbstractFolder selectedFolder = dialog.getSelectedFolder();

			if (selectedFolder != null) {
				try {
					//ContactItemMap list = selectedFolder.getContactItemMap();
					HeaderItemList itemList = selectedFolder
							.getHeaderItemList();
					addressbook.setHeaderItemList(itemList);
					chooseButton.setText(selectedFolder.getName());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}