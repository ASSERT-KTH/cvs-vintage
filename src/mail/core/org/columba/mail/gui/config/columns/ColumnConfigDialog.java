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
package org.columba.mail.gui.config.columns;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.checkablelist.CheckableList;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.help.HelpManager;
import org.columba.core.xml.XmlElement;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folderoptions.ColumnOptionsPlugin;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.util.MailResourceLoader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Configurabe visible columns of the table.
 * <p>
 * TODO: following ui guidelines we should add a show and a hide button
 *       right beside the list, for better accessibility
 * 
 * TODO: adding of columns is not working currently
 * @author fdietz
 */
public class ColumnConfigDialog implements ActionListener,
    ListSelectionListener {
    private JDialog dialog;
    private JButton addButton;
    private JButton removeButton;
    private JList list;
    private int index;
    private XmlElement columns;
    private ColumnItem selection;
    private MailFrameMediator mediator;

    public ColumnConfigDialog(MailFrameMediator mediator, XmlElement columns) {
        dialog = DialogStore.getDialog();
        dialog.setTitle("Configure Columns");

        this.mediator = mediator;
        this.columns = columns;

        list = new CheckableList();

        list.getSelectionModel().addListSelectionListener(this);

        initComponents();

        updateComponents(true);

        dialog.getRootPane().registerKeyboardAction(this, "CLOSE",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.getRootPane().registerKeyboardAction(this, "HELP",
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    protected JPanel createButtonPanel() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());

        bottom.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        ButtonWithMnemonic cancelButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "global", "cancel"));

        //$NON-NLS-1$ //$NON-NLS-2$
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

        ButtonWithMnemonic okButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "global", "ok"));

        //$NON-NLS-1$ //$NON-NLS-2$
        okButton.addActionListener(this);
        okButton.setActionCommand("OK"); //$NON-NLS-1$
        okButton.setDefaultCapable(true);
        dialog.getRootPane().setDefaultButton(okButton);

        ButtonWithMnemonic helpButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "global", "help"));

        // associate with JavaHelp
        HelpManager.enableHelpOnButton(helpButton, "configuring_columba");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 6, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(helpButton);

        bottom.add(buttonPanel, BorderLayout.EAST);

        return bottom;
    }

    public void initComponents() {
        dialog.getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        addButton = new ButtonWithMnemonic("&Add...");

        addButton.setActionCommand("ADD");
        addButton.addActionListener(this);
        addButton.setEnabled(false);
        
        removeButton = new ButtonWithMnemonic("&Remove...");

        removeButton.setActionCommand("REMOVE");
        removeButton.setEnabled(false);
        removeButton.addActionListener(this);

        // top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JPanel topBorderPanel = new JPanel();
        topBorderPanel.setLayout(new BorderLayout());
        topBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        topBorderPanel.add(topPanel, BorderLayout.CENTER);

        Component glue = Box.createVerticalGlue();
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = GridBagConstraints.REMAINDER;

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

        gridBagLayout.setConstraints(removeButton, c);
        eastPanel.add(removeButton);

        Component strut = Box.createRigidArea(new Dimension(30, 5));
        gridBagLayout.setConstraints(strut, c);
        eastPanel.add(strut);

        glue = Box.createVerticalGlue();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(glue, c);
        eastPanel.add(glue);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(300, 250));
        scrollPane.getViewport().setBackground(Color.white);
        mainPanel.add(scrollPane);
        dialog.getContentPane().add(mainPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

        JPanel buttonPanel = createButtonPanel();

        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        dialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private XmlElement findColumn(XmlElement parent, String name) {
        for (int i = 0; i < parent.count(); i++) {
            XmlElement child = parent.getElement(i);

            if (child.getAttribute("name").equals(name)) {
                return child;
            }
        }

        return null;
    }

    public void updateComponents(boolean b) {
        if (b) {
            DefaultListModel model = new DefaultListModel();

            for (int i = 0; i < columns.count(); i++) {
                XmlElement column = columns.getElement(i);
                ColumnItem item = new ColumnItem(column);
                model.addElement(item);
            }

            list.setModel(model);
        } else {
            DefaultListModel model = ((DefaultListModel) list.getModel());

            for (int i = 0; i < model.size(); i++) {
                // get column of list
                ColumnItem column = (ColumnItem) model.get(i);

                // find colum
                XmlElement element = findColumn(columns, column.toString());

                // skip item
                if (element == null) {
                    continue;
                }

                // enable/disable column
                if (column.isSelected()) {
                    element.addAttribute("enabled", "true");
                } else {
                    element.addAttribute("enabled", "false");
                }
            }

            XmlElement.printNode(columns, " ");
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        DefaultListSelectionModel theList = (DefaultListSelectionModel) e.getSource();

        if (!theList.isSelectionEmpty()) {
            index = theList.getAnchorSelectionIndex();

            selection = (ColumnItem) ((DefaultListModel) list.getModel()).getElementAt(index);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("OK")) {
            updateComponents(false);

            dialog.setVisible(false);

            ColumnOptionsPlugin plugin = (ColumnOptionsPlugin) mediator.getFolderOptionsController()
                                                                       .getPlugin("ColumnOptions");

            // make sure this configuration is also visually working immediately
            FolderCommandReference[] r = mediator.getTreeSelection();
            plugin.loadOptionsFromXml((Folder) r[0].getFolder());
            
        } else if (action.equals("CANCEL")) {
            dialog.setVisible(false);
        } else if (action.equals("ADD")) {
            // TODO: implement adding of new columns

            /*
            String columnName = JOptionPane.showInputDialog(null,
                    "New column name");

            if (columnName == null) {
                return;
            }

            if (columnName.length() == 0) {
                return;
            }

            ((DefaultListModel) list.getModel()).addElement(new CheckableItemImpl(
                    columnName));
             */
        } else if (action.equals("REMOVE")) {
            // FIXME: implement column removal

            /*
            if (selection != null) {
                ((DefaultListModel) list.getModel()).removeElement(selection);
            }
            */
        }
    }
}
