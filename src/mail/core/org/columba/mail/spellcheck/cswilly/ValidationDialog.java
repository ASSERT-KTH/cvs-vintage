/*
 * $Revision: 1.2 $
 * $Date: 2004/08/22 15:55:07 $
 * $Author: fdietz $
 *
 * Copyright (C) 2001 C. Scott Willy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.columba.mail.spellcheck.cswilly;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Dialog to the user to determine what to do about a misspelled word.
 *<p>
 *   <p>Features of the dialog box:</p>
 *   <ul>
 *     <li>the dialog box is modal</li>
 *     <li>the title of the dialog box is set to "Spell Check"</li>
 *     <li>the user action change be determined using the getUserAction() method</li>
 *     <li>the possible user actions are modelled using the instances of the class UserAction
 *       (which is an enum-like thingy). The possible instances are give by public
 *       static text fields (e.g. ADD, and CANCEL) of type UserAction.</li>
 *     <li>the dialog box can be closed using the escape key (user action is CANCEL)</li>
 *     <li>there is a default button (<cite>Ignore</cite>)</li>
 *     <li>the buttons all have mnemonic keys</li>
 *     <li>??? todo set focus to <cite>Change to</cite> text field when dialog is
 *       opened</li>
 *     <li>??? todo add tool tips to all visual components</li>
 *     <li>??? todo move text to properties file</li>
 *   </ul>
 *<p>
 * Escape closes dialog (http://www.javaworld.com/javaworld/javatips/jw-javatip72.html)
 */
public class ValidationDialog extends JDialog {
    // ??? bad to have release hardocoded here. Fix later...right.
    private static Point _location = new Point(100, 100);
    public static final UserAction ADD = new UserAction("Add");
    public static final UserAction CANCEL = new UserAction("Cancel");
    public static final UserAction CHANGE = new UserAction("Change");
    public static final UserAction CHANGE_ALL = new UserAction("Change All");
    public static final UserAction IGNORE = new UserAction("Ignore");
    public static final UserAction IGNORE_ALL = new UserAction("Ignore All");
    private JTextField _changeToTextField;
    private final String _originalWord;
    private final List _suggestions;
    private JList _suggestionsJList;
    private UserAction _userAction = CANCEL;
    private String _title = "Spell Check, Release R003";

    public ValidationDialog(Frame owner, String originalWord, List suggestions) {
        super(owner);
        _originalWord = originalWord;
        _suggestions = suggestions;
        _init();
    }

    public ValidationDialog(Dialog owner, String originalWord, List suggestions) {
        super(owner);
        _originalWord = originalWord;
        _suggestions = suggestions;
        _init();
    }

    public ValidationDialog(String originalWord, List suggestions) {
        super();
        _originalWord = originalWord;
        _suggestions = suggestions;
        _init();
    }

    public void dispose() {
        // save current location statically for next time
        _location = getLocation();
        super.dispose();
    }

    public UserAction getUserAction() {
        return _userAction;
    }

    /**
 * Returns the replacement word selected by the user
 *<p>
 * The returned value only makes sense if the user action is either CHANGE
 * or CHANGE_ALL. Should be ignored for any other action.
 *<p>
 * @return the replacement word selected by the user as a String
 */
    public String getSelectedWord() {
        return _changeToTextField.getText();
    }

    /**
 * Overriden to register {@link CloseDialogActionListener} to be called when
 * the escape key is pressed.
 */
    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        ActionListener actionListener = new CloseDialogActionListener();
        rootPane.registerKeyboardAction(actionListener, stroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }

    /**
 * Convenience method add lableled component
 */
    private void _addRow(Box mainBox, JComponent labelComponent,
        Component component) {
        Box hBox = Box.createHorizontalBox();
        mainBox.add(hBox);

        Dimension labelComponentDim = new Dimension(100,
                labelComponent.getPreferredSize().height);
        labelComponent.setPreferredSize(labelComponentDim);
        labelComponent.setMinimumSize(labelComponentDim);
        labelComponent.setMaximumSize(labelComponentDim);
        hBox.add(labelComponent);

        hBox.add(Box.createHorizontalGlue());
        hBox.add(component);
        hBox.add(Box.createHorizontalGlue());
    }

    /**
 * Convenience method to create and config buttons from an action
 *<p>
 * After creating the JButton, sets the Mnemonic and ToolTipText by looking
 * for the AbstractFilterAction.MNEMONIC_KEY and AbstractFilterAction.SHORT_DESCRIPTION
 * keys in <code>action</code>.
 *<p>
 * @return a new, freshly configured JButton
 */
    private static JButton _configButton(AbstractAction action) {
        JButton retButton = new JButton(action);

        Object value;

        value = action.getValue(AbstractAction.MNEMONIC_KEY);

        if (value != null) {
            int MnemonicKey = ((Integer) value).intValue();
            retButton.setMnemonic(MnemonicKey);
        }

        value = action.getValue(AbstractAction.SHORT_DESCRIPTION);

        if (value != null) {
            String toolTip = (String) value;
            retButton.setToolTipText(toolTip);
        }

        return retButton;
    }

    /**
 * Initializes the dialog box
 */
    private void _init() {
        setModal(true);
        setTitle(_title);

        //--
        //-- Buttons
        //--
        JButton aboutButton = _configButton(new AboutAction());
        JButton addButton = _configButton(new AddAction());
        addButton.setEnabled(false);

        JButton cancelButton = _configButton(new CancelAction());
        JButton changeButton = _configButton(new ChangeAction());
        JButton changeAllButton = _configButton(new ChangeAllAction());
        JButton ignoreButton = _configButton(new IgnoreAction());
        JButton ignoreAllButton = _configButton(new IgnoreAllAction());

        //--
        //-- Text Fields
        //--
        _changeToTextField = new JTextField();
        _changeToTextField.setMinimumSize(new Dimension(200,
                _changeToTextField.getPreferredSize().height));
        _changeToTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                _changeToTextField.getPreferredSize().height));

        Dimension textFieldDim = new Dimension(Integer.MAX_VALUE,
                _changeToTextField.getPreferredSize().height);
        JTextField originalWordTextField = new JTextField(_originalWord);
        originalWordTextField.setMinimumSize(new Dimension(200,
                originalWordTextField.getPreferredSize().height));
        originalWordTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                originalWordTextField.getPreferredSize().height));

        //--
        //-- Other components
        //--
        _suggestionsJList = new JList(_suggestions.toArray());
        _suggestionsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _suggestionsJList.addListSelectionListener(new MyListSelectionListener());
        _suggestionsJList.setMinimumSize(new Dimension(200, 300));
        _suggestionsJList.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Integer.MAX_VALUE));
        _suggestionsJList.setPreferredSize(new Dimension(200, 300));

        JScrollPane suggestionsJScrollPane = new JScrollPane(_suggestionsJList);

        //    suggestionsJScrollPane.setPreferredSize(
        //      new Dimension( suggestionsJScrollPane.getPreferredSize().width, 75 ) );
        //--
        //-- Overall Dialog box
        //--
        Box mainBox = Box.createVerticalBox();

        getContentPane().add(mainBox);

        Box hBox;
        JLabel jLabel;

        jLabel = new JLabel("Not in Dictionary:");
        _addRow(mainBox, jLabel, originalWordTextField);

        jLabel = new JLabel("Change to:");
        _addRow(mainBox, jLabel, _changeToTextField);

        jLabel = new JLabel("Suggestions:");
        hBox = Box.createHorizontalBox();

        //suggestionsJScrollPane.setMinimumSize( new Dimension( 200, 300 ) );
        //suggestionsJScrollPane.setMaximumSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
        hBox.add(suggestionsJScrollPane);
        hBox.add(Box.createHorizontalGlue());

        getRootPane().setDefaultButton(ignoreButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(200, 100));
        buttonPanel.add(ignoreButton);
        buttonPanel.add(ignoreAllButton);
        buttonPanel.add(changeButton);
        buttonPanel.add(changeAllButton);
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(aboutButton);
        hBox.add(buttonPanel);
        hBox.add(Box.createHorizontalGlue());

        _addRow(mainBox, jLabel, hBox);

        if (_location != null) {
            setLocation(_location);
        }

        pack();
        _suggestionsJList.setSelectedIndex(0);
        _suggestionsJList.grabFocus();

        //setSize( 750, getPreferredSize().height );
    }

    /**
 * Models a enum of UserActions
 */
    public static class UserAction {
        private final String _name;

        private UserAction(String name) {
            _name = name;
        }

        public String toString() {
            return _name;
        }
    }

    //--
    //-- Availables Actions
    //--
    private class AboutAction extends AbstractAction {
        private AboutAction() {
            super("About...");
        }

        public void actionPerformed(ActionEvent event) {
            String msg = "Based on interfacing Java with Aspell.\n" +
                "Hacked by C. Scott Willy to scratch an itch.\n" +
                "Copyright 2001 Scott Willy\n" +
                "http://www.geocities.com/cswilly/spellcheck/";
            String title = "About " + _title;
            JOptionPane.showMessageDialog(ValidationDialog.this, msg, title,
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class AddAction extends AbstractAction {
        private AddAction() {
            super("Add");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue(ACCELERATOR_KEY, new Integer(KeyEvent.VK_A));
        }

        public void actionPerformed(ActionEvent event) {
            _userAction = ADD;
            dispose();
        }
    }

    private class CancelAction extends AbstractAction {
        private CancelAction() {
            super("Cancel");
        }

        public void actionPerformed(ActionEvent event) {
            _userAction = CANCEL;
            dispose();
        }
    }

    private class ChangeAction extends AbstractAction {
        private ChangeAction() {
            super("Change");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(ACCELERATOR_KEY, new Integer(KeyEvent.VK_C));
            putValue(SHORT_DESCRIPTION,
                "Replaces the word not in the Not in Dictionary text field with the word in the Change to text field.");
        }

        public void actionPerformed(ActionEvent event) {
            _userAction = CHANGE;
            dispose();
        }
    }

    private class ChangeAllAction extends AbstractAction {
        private ChangeAllAction() {
            super("Change All");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
            putValue(ACCELERATOR_KEY, new Integer(KeyEvent.VK_L));
        }

        public void actionPerformed(ActionEvent event) {
            _userAction = CHANGE_ALL;
            dispose();
        }
    }

    private class IgnoreAction extends AbstractAction {
        private IgnoreAction() {
            super("Ignore");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
            putValue(ACCELERATOR_KEY, new Integer(KeyEvent.VK_I));
        }

        public void actionPerformed(ActionEvent event) {
            _userAction = IGNORE;
            dispose();
        }
    }

    private class IgnoreAllAction extends AbstractAction {
        private IgnoreAllAction() {
            super("Ignore All");

            //      putValue( MNEMONIC_KEY, new Integer(KeyEvent.VK_G) );
            //      putValue( ACCELERATOR_KEY, new Integer(KeyEvent.VK_G) );
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
            putValue(ACCELERATOR_KEY, new Integer(KeyEvent.VK_G));
        }

        public void actionPerformed(ActionEvent event) {
            _userAction = IGNORE_ALL;
            dispose();
        }
    }

    private class CloseDialogActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            _userAction = CANCEL;
            dispose();
        }
    }

    private class MyListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            int selectedIndex = _suggestionsJList.getSelectedIndex();

            if (selectedIndex >= 0) {
                _changeToTextField.setText((String) _suggestions.get(
                        selectedIndex));
            }
        }
    }
}
