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

import org.columba.core.gui.checkabletree.*;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.main.MainInterface;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.command.ExportFolderCommand;
import org.columba.mail.gui.util.URLController;
import org.columba.mail.main.MailInterface;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * ExportDialog lets you select a number of folders for exporting
 * messages.
 *

 * @author fdietz
 *
 */
public class ExportDialog
    extends JDialog
    implements ActionListener, TreeSelectionListener {
    private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";
    private JButton exportButton;
    private JButton selectAllButton;
    private JButton helpButton;
    private JButton closeButton;
    private JTree tree;
    private DefaultMutableTreeNode selectedNode;

    public ExportDialog() {
        // modal JDialog
        super(new JFrame(), true);

        initComponents();

        pack();

        setLocationRelativeTo(null);

        setVisible(true);
    }

    private void initTree(CheckableTreeNode root, FolderTreeNode parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            FolderTreeNode child = (FolderTreeNode) parent.getChildAt(i);

            CheckableTreeNode c = new CheckableTreeNode(child.getName());
            c.setIcon(child.getCollapsedIcon());
            c.setNode(child);
            root.addChild(c);

            initTree(c, child);
        }
    }

    protected void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(mainPanel);

        exportButton = new JButton("Export...");

        exportButton.setActionCommand("EXPORT");
        exportButton.addActionListener(this);

        selectAllButton = new JButton("Select All");

        selectAllButton.setActionCommand("SELECTALL");
        selectAllButton.addActionListener(this);

        FolderTreeNode parent =
            (FolderTreeNode) MailInterface.treeModel.getRoot();
        CheckableTreeNode root = new CheckableTreeNode(parent.getName());
        root.setNode(parent);
        initTree(root, parent);

        tree = new CheckableTree(root);
        tree.setRootVisible(false);

        tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(new NodeSelectionListener(tree));
        tree.expandRow(0);
        tree.expandRow(1);
        tree.addTreeSelectionListener(this);

        // top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        //topPanel.setLayout( );
        JPanel topBorderPanel = new JPanel();
        topBorderPanel.setLayout(new BorderLayout());

        //topBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        topBorderPanel.add(topPanel);

        //mainPanel.add( topBorderPanel, BorderLayout.NORTH );
        JLabel nameLabel = new JLabel("name");
        nameLabel.setEnabled(false);
        topPanel.add(nameLabel);

        topPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
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
        gridBagLayout.setConstraints(exportButton, c);
        eastPanel.add(exportButton);

        Component strut1 = Box.createRigidArea(new Dimension(30, 5));
        gridBagLayout.setConstraints(strut1, c);
        eastPanel.add(strut1);

        gridBagLayout.setConstraints(selectAllButton, c);
        eastPanel.add(selectAllButton);

        glue = Box.createVerticalGlue();
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

        ButtonWithMnemonic closeButton =
            new ButtonWithMnemonic(
                MailResourceLoader.getString("global", "close"));
        closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        ButtonWithMnemonic helpButton =
            new ButtonWithMnemonic(
                MailResourceLoader.getString("global", "help"));
        helpButton.setActionCommand("HELP");
        helpButton.addActionListener(this);
        buttonPanel.add(helpButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(closeButton);
        getRootPane().registerKeyboardAction(
            this,
            "CLOSE",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(
            this,
            "HELP",
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void getTreeNodeIteration(CheckableTreeNode parent, Vector v)
    {
        v.add(parent);
        
        for ( int i=0; i<parent.getChildCount(); i++)
        {
            v.add(parent.getChildAt(i));
            getTreeNodeIteration((CheckableTreeNode) parent.getChildAt(i), v);
                
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CLOSE")) {
            setVisible(false);
        } else if (action.equals("HELP")) {
            URLController c = new URLController();

            try {
                c.open(new URL("help.html"));
            } catch (MalformedURLException mue) {
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
       
            Vector list = new Vector();
            getTreeNodeIteration( (CheckableTreeNode) tree.getModel().getRoot(), list);

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
                FolderTreeNode node = (FolderTreeNode) ((CheckableTreeNode)v.get(i)).getNode();

                r[i] = new FolderCommandReference(node);
                r[i].setDestFile(destFile);
            }

            // execute the command
            MainInterface.processor.addOp(new ExportFolderCommand(r));
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent arg0) {
        selectedNode =
            (DefaultMutableTreeNode) arg0.getPath().getLastPathComponent();

        if (selectedNode == null) {
            return;
        }
    }

    /**
     * @return
     */
    public DefaultMutableTreeNode getSelectedNode() {
        return selectedNode;
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
                CheckableItem node =
                    (CheckableItem) path.getLastPathComponent();

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
