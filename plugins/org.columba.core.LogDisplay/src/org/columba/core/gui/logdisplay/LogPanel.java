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
package org.columba.core.gui.logdisplay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * @author redsolo
 */
public class LogPanel extends JPanel {

    //private JTable logTable;
    private JList logList;

    private JToggleButton pauseButton;
    private JButton clearButton;
    private JButton closeButton;
    private JButton detailButton;

    private JTextField searchTextfield;

    private JComboBox levelCombobox;

    /**
     *
     */
    public LogPanel() {
        super();
        initComponents();
        layoutComponents();
    }

    /**
     * Creates all components in the panel.
     */
    private void initComponents() {
        logList = new JList(LogRecordList.getInstance());
        logList.setCellRenderer(new LogRecordListRenderer());
        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    showDetailedLogRecord();
                }
            }
        });
        /*logTable = new JTable(new LogPanelModel());
        logTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    int row = logTable.getSelectedRow();
                    LogPanelModel model = (LogPanelModel) logTable.getModel();
                    LogRecordPanel.showRecord(null, model.getLogRecordForRow(row));
                }
            }
        });*/

        ButtonListener listener = new ButtonListener();
        pauseButton = new JToggleButton("Pause");
        pauseButton.addActionListener(listener);
        clearButton = new JButton("Clear");
        clearButton.addActionListener(listener);
        closeButton = new JButton("Close");
        closeButton.addActionListener(listener);
        detailButton = new JButton("Details");
        detailButton.addActionListener(listener);
        detailButton.setEnabled(false);
        logList.addListSelectionListener(new LogListListener());

        searchTextfield = new JTextField();
        levelCombobox = new JComboBox(new Object[] {Level.ALL, Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE});
    }

    /**
     * Layouts all components in the panel.
     */
    private void layoutComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(searchTextfield);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(levelCombobox);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(pauseButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(detailButton);
        bottomPanel.add(Box.createHorizontalGlue());
        //bottomPanel.add(closeButton);

        JScrollPane scrollPane = new JScrollPane(logList);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Shows a detailed panel about the selected log record.
     */
    private void showDetailedLogRecord() {
        int row = logList.getSelectedIndex();
        if (row != -1) {
            LogRecordList model = (LogRecordList) logList.getModel();
            LogRecordPanel.showRecord(null, model.getLogRecord(row));
        }
    }

    /**
     * Listens on the buttons.
     * @author redsolo
     */
    private class ButtonListener implements ActionListener {

        /** {@inheritDoc} */
        public void actionPerformed(ActionEvent e) {
            LogRecordList instance = LogRecordList.getInstance();
            if (e.getSource() == clearButton) {
                instance.clear();

            } else if (e.getSource() == pauseButton) {

                if (pauseButton.isSelected()) {
                    instance.stopLogging();
                } else {
                    instance.startLogging();
                }
            } else if (e.getSource() == detailButton) {
                showDetailedLogRecord();
            }
        }
    }

    /**
     * A list selection listener, to find out if we are going to enable the detail button.
     * @author redsolo
     */
    private class LogListListener implements ListSelectionListener {

        /** {@inheritDoc} */
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                detailButton.setEnabled(logList.getSelectedIndex() != -1);
            }
        }
    }
}
