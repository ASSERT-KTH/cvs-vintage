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
package org.columba.mail.gui.config.subscribe;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.help.HelpManager;

import org.columba.mail.gui.config.filter.FilterTransferHandler;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;


/**
 * 
 * Subscribe dialog used by IMAP accounts.
 * 
 * @author fdietz
 */
public class SubscribeDialog extends JDialog implements ActionListener,
    TreeSelectionListener, TreeWillExpandListener {
    private JButton subscribeButton;
    private JButton syncButton;
    private JButton unsubscribeButton;
    private JTree tree;
    private Object selection;

    public SubscribeDialog() {
        setTitle(MailResourceLoader.getString("dialog", "subscribe",
                "dialog_title"));
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
        * Inits the GUI components.
        */
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(mainPanel);

        subscribeButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "subscribe", "subscribe"));
        subscribeButton.setActionCommand("SUBSCRIBE");
        subscribeButton.addActionListener(this);
        subscribeButton.setEnabled(false);

        syncButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "subscribe", "sync"));
        syncButton.setActionCommand("SYNC");
        syncButton.setEnabled(false);
        syncButton.addActionListener(this);

        unsubscribeButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "subscribe", "unsubscribe"));
        unsubscribeButton.setActionCommand("UNSUBSCRIBE");
        unsubscribeButton.setEnabled(false);
        unsubscribeButton.addActionListener(this);

        // top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        //topPanel.setLayout( );
        JPanel topBorderPanel = new JPanel();
        topBorderPanel.setLayout(new BorderLayout());

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
        gridBagLayout.setConstraints(subscribeButton, c);
        eastPanel.add(subscribeButton);

        Component strut1 = Box.createRigidArea(new Dimension(30, 6));
        gridBagLayout.setConstraints(strut1, c);
        eastPanel.add(strut1);

        gridBagLayout.setConstraints(unsubscribeButton, c);
        eastPanel.add(unsubscribeButton);

        Component strut = Box.createRigidArea(new Dimension(30, 12));
        gridBagLayout.setConstraints(strut, c);
        eastPanel.add(strut);

        gridBagLayout.setConstraints(syncButton, c);
        eastPanel.add(syncButton);

        glue = Box.createVerticalGlue();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(glue, c);
        eastPanel.add(glue);

        // centerpanel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        tree = new JTree();
        tree.addTreeSelectionListener(this);
        tree.addTreeWillExpandListener(this);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(300, 250));
        scrollPane.getViewport().setBackground(Color.white);
        scrollPane.setTransferHandler(new FilterTransferHandler(scrollPane));
        centerPanel.add(scrollPane);

        mainPanel.add(centerPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        ButtonWithMnemonic closeButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "global", "close"));
        closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        ButtonWithMnemonic helpButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "global", "help"));

        // associate with JavaHelp
        HelpManager.enableHelpOnButton(helpButton,
            "organising_and_managing_your_email_3");
        buttonPanel.add(helpButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(closeButton);
        getRootPane().registerKeyboardAction(this, "CLOSE",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        /*
        getRootPane().registerKeyboardAction(
        this,
        "HELP",
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW);
        */
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CLOSE")) {
            setVisible(false);
        } else if (action.equals("SUBSCRIBE")) {
            // TODO: implement this
        } else if (action.equals("UNSUBSCRIBE")) {
            // TODO: implement this
        } else if (action.equals("SYNC")) {
            // TODO: implement this
        }
    }

    /************************ TreeSelectionListener *********************/
    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent arg0) {
        selection = arg0.getPath().getLastPathComponent();
    }

    /************************** TreeWillExpandListener ********************/
    /**
     * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
     */
    public void treeWillCollapse(TreeExpansionEvent arg0)
        throws ExpandVetoException {
    }

    /**
     * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
     */
    public void treeWillExpand(TreeExpansionEvent arg0)
        throws ExpandVetoException {
    }
}
