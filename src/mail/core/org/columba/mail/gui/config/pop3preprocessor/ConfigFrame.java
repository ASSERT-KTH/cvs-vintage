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
package org.columba.mail.gui.config.pop3preprocessor;

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
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.xml.XmlElement;
import org.columba.mail.pop3.POP3Server;
import org.columba.mail.util.MailResourceLoader;


public class ConfigFrame extends JDialog implements ListSelectionListener,
    ActionListener {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.config.pop3preprocessor");

    /*
    private JTextField textField;
    private JPanel leftPanel;
    private JTabbedPane rightPanel;
    private JButton addButton;
    private JButton removeButton;
    private JButton editButton;
    private JButton upButton;
    private JButton downButton;
    */
    private JFrame frame;
    public FilterListTable listView;

    //private AdapterNode actNode;
    private boolean newAccount = false;
    private int index = -1;
    private XmlElement filterList;
    private XmlElement filter;

    //private JDialog dialog;
    private JPanel centerPanel = new JPanel();
    private JPanel eastPanel = new JPanel();
    private JPanel jPanel1 = new JPanel();
    private JTextField nameTextField = new JTextField();
    private JLabel nameLabel = new JLabel();
    private JButton addButton;
    private JButton removeButton;
    private JButton editButton;
    private JButton enableButton;
    private JButton disableButton;
    private JButton moveupButton;
    private JButton movedownButton;
    private BorderLayout borderLayout3 = new BorderLayout();
    private GridLayout gridLayout1 = new GridLayout();
    private POP3Server popserver;

    public ConfigFrame(JDialog dialog, XmlElement filterList) {
        super(dialog, true);

        this.filterList = filterList;

        setTitle("POP3 Preprocessing Filter Configuration");

        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public XmlElement getSelected() {
        return filter;
    }

    public void setSelected(XmlElement f) {
        filter = f;
    }

    public void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(mainPanel);

        addButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "filter", "add_filter"));
        addButton.setActionCommand("ADD");
        addButton.addActionListener(this);

        removeButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "filter", "remove_filter"));
        removeButton.setActionCommand("REMOVE");
        removeButton.setEnabled(false);
        removeButton.addActionListener(this);

        editButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "filter", "edit_filter"));
        editButton.setActionCommand("EDIT");
        editButton.setEnabled(false);
        editButton.addActionListener(this);

        /*
        enableButton.setText("Enable");
        enableButton.setActionCommand("ENABLE");
        enableButton.addActionListener( this );

        disableButton.setText("Disable");
        disableButton.setActionCommand("DISABLE");
        disableButton.addActionListener( this );
        */
        moveupButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "filter", "moveup"));
        moveupButton.setActionCommand("MOVEUP");
        moveupButton.setEnabled(false);
        moveupButton.addActionListener(this);

        movedownButton = new ButtonWithMnemonic(MailResourceLoader.getString(
                    "dialog", "filter", "movedown"));
        movedownButton.setActionCommand("MOVEDOWN");
        movedownButton.setEnabled(false);
        movedownButton.addActionListener(this);

        // top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        //topPanel.setLayout( );
        JPanel topBorderPanel = new JPanel();
        topBorderPanel.setLayout(new BorderLayout());

        //topBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        topBorderPanel.add(topPanel, BorderLayout.CENTER);

        //mainPanel.add( topBorderPanel, BorderLayout.NORTH );
        nameLabel.setText("name");
        nameLabel.setEnabled(false);
        topPanel.add(nameLabel);

        topPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
        topPanel.add(Box.createHorizontalGlue());

        nameTextField.setText("name");
        nameTextField.setEnabled(false);
        topPanel.add(nameTextField);

        Component glue = Box.createVerticalGlue();
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = GridBagConstraints.REMAINDER;

        //c.fill = GridBagConstraints.HORIZONTAL;
        gridBagLayout.setConstraints(glue, c);

        gridBagLayout = new GridBagLayout();
        c = new GridBagConstraints();
        eastPanel.setLayout(gridBagLayout);
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

        gridBagLayout.setConstraints(editButton, c);
        eastPanel.add(editButton);

        strut = Box.createRigidArea(new Dimension(30, 20));
        gridBagLayout.setConstraints(strut, c);
        eastPanel.add(strut);

        /*
        gridBagLayout.setConstraints( enableButton, c );
        eastPanel.add( enableButton );

        strut = Box.createRigidArea( new Dimension(30,10) );
        gridBagLayout.setConstraints( strut, c );
        eastPanel.add( strut );

        gridBagLayout.setConstraints( disableButton, c );
        eastPanel.add( disableButton );

        strut = Box.createRigidArea( new Dimension(30,20) );
        gridBagLayout.setConstraints( strut, c );
        eastPanel.add( strut );
        */
        gridBagLayout.setConstraints(moveupButton, c);
        eastPanel.add(moveupButton);

        strut = Box.createRigidArea(new Dimension(30, 5));
        gridBagLayout.setConstraints(strut, c);
        eastPanel.add(strut);

        gridBagLayout.setConstraints(movedownButton, c);
        eastPanel.add(movedownButton);

        glue = Box.createVerticalGlue();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(glue, c);
        eastPanel.add(glue);

        /*
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        gridBagLayout.setConstraints( closeButton, c );
        eastPanel.add( closeButton );
        */
        // centerpanel
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        listView = new FilterListTable(filterList, this);
        listView.getSelectionModel().addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(listView);
        scrollPane.setPreferredSize(new Dimension(300, 250));
        scrollPane.getViewport().setBackground(Color.white);
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

        //TODO: enable help for button and root pane
        buttonPanel.add(helpButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(closeButton);
        getRootPane().registerKeyboardAction(this, "CLOSE",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        DefaultListSelectionModel theList = (DefaultListSelectionModel) e.getSource();

        if (theList.isSelectionEmpty()) {
            removeButton.setEnabled(false);
            editButton.setEnabled(false);
        } else {
            removeButton.setEnabled(true);
            editButton.setEnabled(true);

            //String value = (String) theList.getSelectedValue();
            index = theList.getAnchorSelectionIndex();

            setSelected(filterList.getElement(index));
        }
    }

    public void showFilterDialog() {
        XmlElement parent = getSelected();

        if (parent != null) {
            // TODO: add config-dialog here
            //FilterDialog dialog = new FilterDialog(parent);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CLOSE")) {
            // FIXME
            //Config.save();
            setVisible(false);
        } else if (action.equals("ADD")) {
            LOG.info("add");

            ChooseFilterDialog d = new ChooseFilterDialog(this);

            if (d.isSuccess() == false) {
                return;
            }

            String id = d.getSelection();

            LOG.info("selected id=" + id);

            XmlElement filter = filterList.addSubElement(
                    "pop3preprocessingfilter");
            filter.addAttribute("enabled", "true");
            filter.addAttribute("name", id);

            listView.update();

            setSelected(filter);

            /*
            XmlElement filter = filterList.addSubElement("pop3preprocessingfilter");
            filter.addAttribute("enabled","true");


            listView.update();

            setSelected(filter);

            showFilterDialog();

            listView.update();
            */
        } else if (action.equals("REMOVE")) {
            System.out.println("remove");

            filterList.removeElement(index);

            removeButton.setEnabled(false);
            editButton.setEnabled(false);

            listView.update();
        } else if (action.equals("EDIT")) {
            showFilterDialog();

            listView.update();
        }
    }
}
