//The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.gui.profiles;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.help.HelpManager;
import org.columba.core.io.DiskIO;
import org.columba.core.profiles.Profile;
import org.columba.core.profiles.ProfileManager;
import org.columba.core.util.GlobalResourceLoader;
import org.columba.core.xml.XmlElement;

/**
 * Profile chooser dialog.
 * <p>
 * User can choose a profile from a list. Add a new profile or edit and existing
 * profiles's properties.
 * <p>
 * Additionally, the user can choose to hide this dialog on next startup.
 *
 * @author fdietz
 */
public class ProfileManagerDialog extends JDialog
implements ActionListener, ListSelectionListener {
    private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";
    
    protected JButton okButton;
    protected JButton helpButton;
    protected JButton addButton;
    protected JButton editButton;
    protected JButton removeButton;
    protected JButton importButton;
    protected JButton exportButton;
    //protected JButton defaultButton;
    private DefaultListModel model;
    protected JList list;
    protected String selection;
    protected JLabel nameLabel;
    protected JCheckBox checkBox;
    
    private FrameMediator mediator;
    
    public ProfileManagerDialog(FrameMediator mediator) throws HeadlessException {
        super(mediator.getView().getFrame(), GlobalResourceLoader.getString(
            RESOURCE_PATH, "profiles", "manager.title"), true);
        this.mediator = mediator;
        
        initComponents();
        
        layoutComponents();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    protected void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        //		 top panel
        JPanel topPanel = new JPanel();
        
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        //topPanel.setLayout( );
        JPanel topBorderPanel = new JPanel();
        topBorderPanel.setLayout(new BorderLayout());
        
        //topBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5,
        // 0));
        topBorderPanel.add(topPanel);
        
        //mainPanel.add( topBorderPanel, BorderLayout.NORTH );
        
        topPanel.add(nameLabel);
        
        topPanel.add(Box.createRigidArea(new Dimension(10, 0)));
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
        gridBagLayout.setConstraints(addButton, c);
        eastPanel.add(addButton);
        
        Component strut1 = Box.createRigidArea(new Dimension(30, 6));
        gridBagLayout.setConstraints(strut1, c);
        eastPanel.add(strut1);
        
        gridBagLayout.setConstraints(editButton, c);
        eastPanel.add(editButton);
        
        Component strut2 = Box.createRigidArea(new Dimension(30, 6));
        gridBagLayout.setConstraints(strut2, c);
        eastPanel.add(strut2);
        
        gridBagLayout.setConstraints(removeButton, c);
        eastPanel.add(removeButton);
        
        /*
         * Component strut3 = Box.createRigidArea(new Dimension(30, 12));
         * gridBagLayout.setConstraints(strut3, c); eastPanel.add(strut3);
         *
         * gridBagLayout.setConstraints(importButton, c);
         * eastPanel.add(importButton);
         *
         * Component strut4 = Box.createRigidArea(new Dimension(30, 6));
         * gridBagLayout.setConstraints(strut4, c); eastPanel.add(strut4);
         *
         * gridBagLayout.setConstraints(exportButton, c);
         * eastPanel.add(exportButton);
         */
        
        glue = Box.createVerticalGlue();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        gridBagLayout.setConstraints(glue, c);
        eastPanel.add(glue);
        
        // centerpanel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(250, 150));
        scrollPane.getViewport().setBackground(Color.white);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        centerPanel.add(checkBox, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        mainPanel.add(centerPanel);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        buttonPanel.add(okButton);
        
        buttonPanel.add(helpButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }
    
    protected void initComponents() {
        addButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
            RESOURCE_PATH, "profiles", "add"));
        addButton.setActionCommand("ADD");
        addButton.addActionListener(this);
        addButton.setEnabled(false);
        
        editButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
            RESOURCE_PATH, "profiles", "edit"));
        editButton.setActionCommand("EDIT");
        editButton.addActionListener(this);
        editButton.setEnabled(false);
        
        removeButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
            RESOURCE_PATH, "profiles", "remove"));
        removeButton.setActionCommand("REMOVE");
        removeButton.addActionListener(this);
        removeButton.setEnabled(false);
        
        importButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
            RESOURCE_PATH, "profiles", "import"));
        importButton.setActionCommand("IMPORT");
        importButton.addActionListener(this);
        
        exportButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
            RESOURCE_PATH, "profiles", "export"));
        exportButton.setActionCommand("EXPORT");
        exportButton.addActionListener(this);
        exportButton.setEnabled(false);
        
        nameLabel = new JLabel("Choose Profile:");
        
        checkBox = new JCheckBox("Always ask on startup.");
        checkBox.setSelected(ProfileManager.getInstance().isAlwaysAsk());
        checkBox.setActionCommand("CHECKBOX");
        checkBox.addActionListener(this);
        
        okButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
            "", "", "close"));
        okButton.setActionCommand("CLOSE");
        okButton.addActionListener(this);
        
        helpButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
            "", "", "help"));
        
        // associate with JavaHelp
        HelpManager.getHelpManager().enableHelpOnButton(helpButton,
            "extending_columba_2");
        HelpManager.getHelpManager().enableHelpKey(getRootPane(),
            "extending_columba_2");
        
        XmlElement profiles = ProfileManager.getInstance().getProfiles();
        model = new DefaultListModel();
        model.addElement("Default");
        
        for (int i = 0; i < profiles.count(); i++) {
            XmlElement p = profiles.getElement(i);
            String name = p.getAttribute("name");
            model.addElement(name);
        }
        
        list = new JList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        
        String selected = ProfileManager.getInstance().getSelectedProfile();
        if (selected != null) {
            list.setSelectedValue(selected, true);
        }
        
        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(this, "CLOSE",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("CLOSE")) {
            ProfileManager.getInstance().setAlwaysAsk(isAlwaysAskSelected());
            
            setVisible(false);
        } else if (action.equals("CHECKBOX")) {
            
        } else if (action.equals("ADD")) {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setFileHidingEnabled(false);
            
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File location = fc.getSelectedFile();
                Profile p = new Profile(location.getName(), location);
                // add profile to profiles.xml
                ProfileManager.getInstance().addProfile(p);
                
                // add to listmodel
                model.addElement(p.getName());
                // select new item
                list.setSelectedValue(p.getName(), true);
            }
        } else if (action.equals("EDIT")) {
            String inputValue = JOptionPane.showInputDialog(
                GlobalResourceLoader.getString(RESOURCE_PATH, "profiles",
                "enter_name"), selection);
            
            if (inputValue == null) {
                return;
            }
            
            // rename profile in profiles.xml
            ProfileManager.getInstance().renameProfile(selection, inputValue);
            
            // modify listmodel
            model.setElementAt(inputValue, model.indexOf(selection));
            
            selection = inputValue;
        } else if (action.equals("REMOVE")) {
            if (ProfileManager.getInstance().getCurrentProfile().getName()
                .equals(selection)) {
                // can't delete currently running profile
                JOptionPane.showMessageDialog(this, GlobalResourceLoader.getString(
                    RESOURCE_PATH, "profiles", "errDelete.msg"),
                    GlobalResourceLoader.getString(RESOURCE_PATH, "profiles",
                    "errDelete.title"), JOptionPane.ERROR_MESSAGE);
            } else {
                Profile p = ProfileManager.getInstance().getProfileForName(
                selection);
                if (p != null) {
                    int n = JOptionPane.showConfirmDialog(this,
                        GlobalResourceLoader.getString(RESOURCE_PATH, "profiles",
                        "confirmDelete.msg"), GlobalResourceLoader.getString(
                        RESOURCE_PATH, "profiles", "confirmDelete.title"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (n == JOptionPane.NO_OPTION) {
                        return;
                    }
                    
                    File location = p.getLocation();
                    // delete directory recursivly
                    DiskIO.deleteDirectory(location);
                }
            }
        } else if (action.equals("IMPORT")) {
            // TODO: add import feature
            /*
             * JFileChooser chooser = new JFileChooser();
             * chooser.addChoosableFileFilter(new FileFilter() { public boolean
             * accept(File file) { return file.isDirectory() ||
             * file.getName().toLowerCase().endsWith(".zip"); }
             *
             * public String getDescription() { return "Columba Profile
             * Archive"; } }); chooser.setAcceptAllFileFilterUsed(false);
             *
             * int result = chooser.showOpenDialog(this);
             *
             * if (result == JFileChooser.APPROVE_OPTION) { File file =
             * chooser.getSelectedFile(); }
             */
        } else if (action.equals("EXPORT")) {
            // TODO: add export feature
        }
    }
    
    public void valueChanged(ListSelectionEvent e) {
        boolean enabled = !list.isSelectionEmpty();
        addButton.setEnabled(enabled);
        
        exportButton.setEnabled(enabled);
        
        // get current list selection
        selection = (String) list.getSelectedValue();
        
        // user's can't delete default account
        if ((selection != null) && (!selection.equals("Default"))) {
            removeButton.setEnabled(true);
            editButton.setEnabled(true);
        } else {
            removeButton.setEnabled(false);
            editButton.setEnabled(false);
        }
    }
    
    /**
     * @return The selection.
     */
    public String getSelection() {
        return selection;
    }
    
    public boolean isAlwaysAskSelected() {
        return checkBox.isSelected();
    }
}
