/*

The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License") you may not use this file except in compliance with the License. 

You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

The Original Code is "BshInterpreter plugin for The Columba Project"

The Initial Developer of the Original Code is Celso Pinto
Portions created by Celso Pinto are Copyright (C) 2005.
Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

All Rights Reserved.

*/
package org.columba.core.gui.scripting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.SingleSideEtchedBorder;
import org.columba.core.scripting.ScriptLogger;
import org.columba.core.scripting.model.ColumbaScript;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class ScriptManager
    extends JDialog
    implements ActionListener
{

    private static final Logger LOG = Logger.getLogger(ScriptManager.class.getName());


    /*TODO move resources to a resource file */
    private static final String
        RES_WINDOW_TITLE = "Macros",
        RES_EDIT_BUTTON = "&Edit",
        RES_REFRESH_BUTTON = "&Refresh",
        RES_REMOVE_BUTTON = "Re&move",
        RES_CLOSE_BUTTON = "&Close",
        RES_LOG_DETAILS_BUTTON = "&Details...",
        RES_CLEAR_LOG_BUTTON = "C&lear",
        RES_TAB_SCRIPTS = "Scripts",
        RES_TAB_LOG = "Log",
        RES_REMOVE_MESSAGE = "The selected scripts will be removed from disk.\nContinue?",
        RES_WARNING_DIALOG_TITLE = "Remove scripts";

    private static final String
        EDIT_SCRIPT_ACTION = "edit",
        REMOVE_SCRIPT_ACTION = "remove",
        REFRESH_LIST_ACTION = "refresh",
        CLOSE_ACTION = "close",
        LOG_DETAILS_ACTION = "log_details",
        CLEAR_LOG_ACTION = "clear_log";

    private JTable
        scriptsList,
        logList;

    private JButton
        editScriptButton,
        removeScriptButton,
        refreshListButton,
        logDetailsButton,
        clearLogButton;


    private ScriptManagerDocument document;

    public ScriptManager(Frame parent, ScriptManagerDocument doc)
    {
        super(parent, RES_WINDOW_TITLE, true);
        document = doc;

        initGui();
        setLocationRelativeTo(getParent());

    }

    private void initGui()
    {

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
        bottomPanel.add(createDialogActionsPanel());

        getContentPane().setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.add(RES_TAB_SCRIPTS, createScriptsPanel());
        tabs.add(RES_TAB_LOG, createLogPanel());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setButtonStatus();

        pack();

    }

    private JPanel createLogPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createLogList(), BorderLayout.CENTER);
        panel.add(createLogListActionsPanel(), BorderLayout.EAST);
        return panel;
    }

    private JPanel createLogListActionsPanel()
    {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 5));

        logDetailsButton = new ButtonWithMnemonic(RES_LOG_DETAILS_BUTTON);
        clearLogButton = new ButtonWithMnemonic(RES_CLEAR_LOG_BUTTON);

        logDetailsButton.setActionCommand(LOG_DETAILS_ACTION);
        clearLogButton.setActionCommand(CLEAR_LOG_ACTION);

        logDetailsButton.addActionListener(this);
        clearLogButton.addActionListener(this);

        panel.add(logDetailsButton);
        panel.add(clearLogButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.NORTH);
        return container;

    }

    
	private JPanel createLogList()
    {
        JPanel panel = new JPanel(new BorderLayout());

        logList = new JTable(new ScriptLogTableModel());

        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.setShowGrid(true);
        logList.sizeColumnsToFit(JTable.AUTO_RESIZE_ALL_COLUMNS);

        logList.getSelectionModel()
            .addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    if (e.getValueIsAdjusting()) return;

                    setButtonStatus();
                }

            });

        TableColumnModel tcm = logList.getColumnModel();
        tcm.getColumn(ScriptLogTableModel.MESSAGE_COLUMN).setPreferredWidth(400);
        tcm.getColumn(ScriptLogTableModel.MESSAGE_COLUMN)
            .setCellRenderer(new DefaultTableCellRenderer()
            {
                protected void setValue(Object value)
                {
                    setText(((ScriptLogger.LogEntry) value).getMessage());
                }
            });

        JScrollPane scrollPane = new JScrollPane(logList);
        scrollPane.setPreferredSize(new Dimension(450, 200));
        scrollPane.getViewport().setBackground(Color.white);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createScriptsPanel()
    {

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createScriptList(), BorderLayout.CENTER);
        panel.add(createScriptActionsPanel(), BorderLayout.EAST);

        return panel;

    }

    
	private JPanel createScriptList()
    {
        JPanel panel = new JPanel(new BorderLayout());

        scriptsList = new JTable(new ScriptsTableModel(document));

        scriptsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scriptsList.setShowGrid(false);
        scriptsList.sizeColumnsToFit(JTable.AUTO_RESIZE_NEXT_COLUMN);

        scriptsList.getSelectionModel()
            .addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    if (e.getValueIsAdjusting()) return;

                    setButtonStatus();
                }
            });

        TableColumnModel tcm = scriptsList.getColumnModel();
        tcm.getColumn(ScriptsTableModel.NAME_COLUMN).setPreferredWidth(100);
        tcm.getColumn(ScriptsTableModel.NAME_COLUMN)
            .setCellRenderer(new DefaultTableCellRenderer()
            {
                protected void setValue(Object value)
                {
                    setText(((ColumbaScript) value).getName());
                }
            });

        tcm.getColumn(ScriptsTableModel.AUTHOR_COLUMN).setPreferredWidth(100);
        tcm.getColumn(ScriptsTableModel.DESCRIPTION_COLUMN).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(scriptsList);
        scrollPane.setPreferredSize(new Dimension(450, 200));
        scrollPane.getViewport().setBackground(Color.white);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createScriptActionsPanel()
    {

        JPanel scriptActionsPanel = new JPanel(new GridLayout(3, 1, 0, 5));

        editScriptButton = new ButtonWithMnemonic(RES_EDIT_BUTTON);
        removeScriptButton = new ButtonWithMnemonic(RES_REMOVE_BUTTON);
        refreshListButton = new ButtonWithMnemonic(RES_REFRESH_BUTTON);

        editScriptButton.setActionCommand(EDIT_SCRIPT_ACTION);
        removeScriptButton.setActionCommand(REMOVE_SCRIPT_ACTION);
        refreshListButton.setActionCommand(REFRESH_LIST_ACTION);

        editScriptButton.addActionListener(this);
        removeScriptButton.addActionListener(this);
        refreshListButton.addActionListener(this);

        scriptActionsPanel.add(editScriptButton);
        scriptActionsPanel.add(removeScriptButton);
        scriptActionsPanel.add(refreshListButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(scriptActionsPanel, BorderLayout.NORTH);

        return container;
    }

    private void setButtonStatus()
    {
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {

                    boolean
                        scriptSelected = scriptsList.getSelectedRowCount() > 0,
                        hasLogMessages = logList.getRowCount() > 0,
                        logMessageSelected = logList.getSelectedRow() > -1;

                    editScriptButton.setEnabled(scriptSelected);
                    removeScriptButton.setEnabled(scriptSelected);
                    refreshListButton.setEnabled(scriptSelected);

                    if (logMessageSelected)
                    {
                        ScriptLogger.LogEntry entry =
                            (ScriptLogger.LogEntry)logList.getModel().getValueAt(logList.getSelectedRow(),
                                                                                ScriptLogTableModel.MESSAGE_COLUMN);

                        logDetailsButton.setEnabled(entry.getDetails() != null &&
                                                    entry.getDetails().length() > 0);
                    }
                    else
                    {
                        logDetailsButton.setEnabled(false);
                    }

                    clearLogButton.setEnabled(hasLogMessages);

                }
            });


    }

    private JPanel createDialogActionsPanel()
    {

        JButton closeButton = new ButtonWithMnemonic(RES_CLOSE_BUTTON);
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        closeButton.setActionCommand(CLOSE_ACTION);
        closeButton.addActionListener(this);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionsPanel.add(closeButton);

        return actionsPanel;

    }

    public void actionPerformed(ActionEvent event)
    {
        String command = event.getActionCommand();

        if (command.equals(CLOSE_ACTION))
        {
            close();
        }
        else if (command.equals(REMOVE_SCRIPT_ACTION))
        {

            if (JOptionPane.showConfirmDialog(this,
                    RES_REMOVE_MESSAGE,
                    RES_WARNING_DIALOG_TITLE,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
            {
                return;
            }

            int[] selectedIndexes = scriptsList.getSelectedRows();
            TableModel model = scriptsList.getModel();

            ColumbaScript[] scripts = new ColumbaScript[selectedIndexes.length];
            for (int i = 0; i < selectedIndexes.length; i++)
            {
                scripts[i] = (ColumbaScript) model.getValueAt(  selectedIndexes[i],
                                                                ScriptsTableModel.NAME_COLUMN);
            }
            document.removeScript(scripts);
        }
        else if (command.equals(REFRESH_LIST_ACTION))
        {
            document.refreshScriptList();
        }
        else if (command.equals(EDIT_SCRIPT_ACTION))
        {
            JOptionPane.showMessageDialog(this,
                "Ha ha, wouldn't it be really sweet if this were implemented?",
                "TODO",
                JOptionPane.INFORMATION_MESSAGE);
        }
        else if (command.equals(LOG_DETAILS_ACTION))
        {
            ScriptLogger.LogEntry entry =
                (ScriptLogger.LogEntry)logList.getModel().getValueAt(logList.getSelectedRow(),
                                                                    ScriptLogTableModel.MESSAGE_COLUMN);

            new MessageDetailsDialog(this,entry).setVisible(true);

        }
        else if (command.equals(CLEAR_LOG_ACTION))
        {
            ((ScriptLogTableModel) logList.getModel()).clearLog();
        }
        else
        {
            LOG.warning("Not handling: " + event.getActionCommand());
        }

        setButtonStatus();
    }

    private void close()
    {

        ((ScriptLogTableModel) logList.getModel()).dispose();

        setVisible(false);
        dispose();
    }

}
