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
package org.columba.core.gui.externaltools;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.externaltools.AbstractExternalToolsPlugin;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.DoubleClickListener;
import org.columba.core.gui.util.InfoViewerDialog;
import org.columba.core.help.HelpManager;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.ExternalToolsPluginHandler;

import org.columba.mail.util.MailResourceLoader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Shows a list of external tools used in Columba.
 * <p>
 * Should be the central location to configure these tools
 *
 * @author fdietz
 */
public class ExternalToolsDialog extends JDialog implements ActionListener,
    ListSelectionListener {
    ExternalToolsPluginHandler handler;
    protected JButton helpButton;
    protected JButton closeButton;
    protected JButton configButton;
    protected JButton infoButton;
    protected JList list;
    protected String selection;

    /**
 * @throws java.awt.HeadlessException
 */
    public ExternalToolsDialog() throws HeadlessException {
        super(new JFrame(), true);

        // TODO: i18n
        setTitle("External Tools");

        try {
            handler = (ExternalToolsPluginHandler) MainInterface.pluginManager.getHandler(
                    "org.columba.core.externaltools");
        } catch (PluginHandlerNotFoundException e) {
            e.printStackTrace();
        }

        initComponents();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    protected void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // TODO: i18n
        configButton = new ButtonWithMnemonic("Con&figure...");
        configButton.setActionCommand("CONFIG");
        configButton.addActionListener(this);
        configButton.setEnabled(false);

        // TODO: i18n
        infoButton = new ButtonWithMnemonic("&Details...");
        infoButton.setActionCommand("INFO");
        infoButton.addActionListener(this);
        infoButton.setEnabled(false);

        String[] ids = handler.getPluginIdList();
        list = new JList(ids);
        list.addListSelectionListener(this);
				list.addMouseListener(new DoubleClickListener()
				                      {
				  											public void doubleClick(MouseEvent ev)
				  											{
				  												actionPerformed(new ActionEvent(list,0,"CONFIG"));  
				  											}
				                      });
        // top panel
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
        JLabel nameLabel = new JLabel("name");
        nameLabel.setEnabled(false);
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
        gridBagLayout.setConstraints(configButton, c);
        eastPanel.add(configButton);

        Component strut1 = Box.createRigidArea(new Dimension(30, 5));
        gridBagLayout.setConstraints(strut1, c);
        eastPanel.add(strut1);

        gridBagLayout.setConstraints(infoButton, c);
        eastPanel.add(infoButton);

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
        centerPanel.add(scrollPane);

        mainPanel.add(centerPanel);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

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
            "extending_columba_2");
        HelpManager.getHelpManager().enableHelpKey(getRootPane(),
            "extending_columba_2");
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CLOSE")) {
            setVisible(false);
        } else if (action.equals("CONFIG")) {
            new ExternalToolsWizardLauncher().launchWizard(selection, false);
        } else if (action.equals("INFO")) {
            AbstractExternalToolsPlugin plugin = null;

            try {
                plugin = (AbstractExternalToolsPlugin) handler.getPlugin(selection,
                        null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            String info = plugin.getDescription();
            new InfoViewerDialog(info);
        }
    }

    /*
 * (non-Javadoc)
 *
 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
 */
    public void valueChanged(ListSelectionEvent e) {
        boolean enabled = !list.isSelectionEmpty();
        configButton.setEnabled(enabled);
        infoButton.setEnabled(enabled);
        selection = (String) list.getSelectedValue();
    }
}
