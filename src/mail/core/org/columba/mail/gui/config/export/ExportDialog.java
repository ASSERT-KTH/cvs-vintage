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

package org.columba.mail.gui.config.export;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.help.HelpManager;
import org.columba.core.main.MainInterface;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.command.ExportFolderCommand;
import org.columba.mail.gui.tree.util.FolderTreeCellRenderer;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;

import org.frappucino.checkabletree.CheckableItem;
import org.frappucino.checkabletree.CheckableTree;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * ExportDialog lets you select a number of folders for exporting
 * messages into the MBOX format.
 *
 * @author fdietz
 */
public class ExportDialog extends JDialog implements ActionListener {
    private ButtonWithMnemonic exportButton;
    private JTree tree;

    public ExportDialog(JFrame parent) {
        super(parent,
            MailResourceLoader.getString("dialog", "export", "dialog_title"),
            false);

        initComponents();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createChildNodes(CheckableTreeNode root, AbstractFolder parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            AbstractFolder child = (AbstractFolder) parent.getChildAt(i);

            CheckableTreeNode c = new CheckableTreeNode(child.getName());
            c.setIcon(FolderTreeCellRenderer.getFolderIcon(child, false));
            c.setNode(child);
            root.add(c);

            createChildNodes(c, child);
        }
    }

    protected void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(mainPanel);

        exportButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "export", "export"));
        exportButton.setActionCommand("EXPORT");
        exportButton.addActionListener(this);

        ButtonWithMnemonic selectAllButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "export", "select_all"));
        selectAllButton.setActionCommand("SELECTALL");
        selectAllButton.addActionListener(this);

        AbstractFolder parent = (AbstractFolder) MailInterface.treeModel.getRoot();
        CheckableTreeNode root = new CheckableTreeNode(parent.getName());
        root.setNode(parent);
        createChildNodes(root, parent);

        tree = new CheckableTree(root);
        tree.setRootVisible(false);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(new NodeSelectionListener(tree));
        tree.expandRow(0);
        tree.expandRow(1);

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        mainPanel.add(new JLabel(MailResourceLoader.getString("dialog",
                    "export", "info")), BorderLayout.NORTH);

        gridBagLayout = new GridBagLayout();
        c = new GridBagConstraints();

        JPanel eastPanel = new JPanel(gridBagLayout);
        mainPanel.add(eastPanel, BorderLayout.EAST);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBagLayout.setConstraints(exportButton, c);
        eastPanel.add(exportButton);

        Component strut1 = Box.createRigidArea(new Dimension(30, 5));
        gridBagLayout.setConstraints(strut1, c);
        eastPanel.add(strut1);

        gridBagLayout.setConstraints(selectAllButton, c);
        eastPanel.add(selectAllButton);

        Component glue = Box.createVerticalGlue();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(glue, c);
        eastPanel.add(glue);

        // centerpanel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        scrollPane.getViewport().setBackground(Color.white);
        centerPanel.add(scrollPane);

        mainPanel.add(centerPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        ButtonWithMnemonic closeButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "global", "close"));
        closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        ButtonWithMnemonic helpButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "global", "help"));
        buttonPanel.add(helpButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(closeButton);
        getRootPane().registerKeyboardAction(this, "CLOSE",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        // associate with JavaHelp
        HelpManager.getHelpManager().enableHelpOnButton(helpButton,
            "organising_and_managing_your_email_5");
        HelpManager.getHelpManager().enableHelpKey(getRootPane(),
            "organising_and_managing_your_email_5");
    }

    private void getTreeNodeIteration(TreeNode parent, List l) {
        l.add(parent);

        for (int i = 0; i < parent.getChildCount(); i++) {
            l.add(parent.getChildAt(i));
            getTreeNodeIteration((TreeNode) parent.getChildAt(i), l);
        }
    }

    /* (non-Javadoc)
 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CLOSE")) {
            setVisible(false);
        } else if (action.equals("SELECTALL")) {
            List list = new LinkedList();
            getTreeNodeIteration((TreeNode) tree.getModel().getRoot(), list);

            Iterator iterator = list.iterator();
            CheckableTreeNode node;

            while (iterator.hasNext()) {
                node = (CheckableTreeNode) iterator.next();
                node.setSelected(true);
                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
            }
        } else if (action.equals("EXPORT")) {
            File destFile = null;

            // ask the user about the destination file
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            int result = chooser.showSaveDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                destFile = file;
            } else {
                return;
            }

            setVisible(false);

            // get list of all folders
            List list = new LinkedList();
            getTreeNodeIteration((TreeNode) tree.getModel().getRoot(), list);

            Iterator it = list.iterator();

            Vector v = new Vector();

            // get list of all selected folders
            while (it.hasNext()) {
                CheckableItem node = (CheckableItem) it.next();

                boolean export = (boolean) node.isSelected();

                if (export) {
                    v.add(node);
                }
            }

            // create command reference array for the command
            FolderCommandReference[] r = new FolderCommandReference[v.size()];

            for (int i = 0; i < v.size(); i++) {
                AbstractFolder node = (AbstractFolder) ((CheckableTreeNode) v.get(i)).getNode();

                r[i] = new FolderCommandReference(node);
                r[i].setDestFile(destFile);
            }

            // execute the command
            MainInterface.processor.addOp(new ExportFolderCommand(r));
        }
    }

    class NodeSelectionListener extends MouseAdapter {
        JTree tree;

        NodeSelectionListener(JTree tree) {
            this.tree = tree;
        }

        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);

            //TreePath  path = tree.getSelectionPath();
            if (path != null) {
                CheckableItem node = (CheckableItem) path.getLastPathComponent();

                node.setSelected(!node.isSelected());

                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);

                // I need revalidate if node is root.  but why?
                if (row == 0) {
                    tree.revalidate();
                    tree.repaint();
                }
            }
        }
    }
}
