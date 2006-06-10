/*
 The contents of this file are subject to the Mozilla Public License Version 1.1
 (the "License"); you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Original Code is "The Columba Project"

 The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
 Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

 All Rights Reserved.
 */

package org.columba.core.gui.scripting;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.scripting.ScriptLogger;

/**
 @author Celso Pinto <cpinto@yimports.com> */
public class MessageDetailsDialog
    extends JDialog
    implements ActionListener
{
    private static final String
        RES_TITLE = "Log message details",
        RES_CLOSE = "&Close";

    public MessageDetailsDialog( Dialog owner, ScriptLogger.LogEntry logEntry)
    {
        super(owner,RES_TITLE,true);
        init(logEntry);

        setLocationRelativeTo(getParent());

    }

    private void init(ScriptLogger.LogEntry logEntry)
    {

        JPanel
            main = new JPanel(new BorderLayout(10,10)),
            centerPanel = new JPanel(new BorderLayout(10,5)),
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel messageLabel = new JLabel(logEntry.getMessage());

        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JTextArea detailsField = new JTextArea(logEntry.getDetails(),5,40);
        ButtonWithMnemonic closeButton = new ButtonWithMnemonic(RES_CLOSE);

        detailsField.setLineWrap(true);
        detailsField.setWrapStyleWord(true);

        scroll.getViewport().add(detailsField);

        centerPanel.add(messageLabel,BorderLayout.NORTH);
        centerPanel.add(scroll,BorderLayout.CENTER);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        main.add(buttonPanel,BorderLayout.SOUTH);
        main.add(centerPanel,BorderLayout.CENTER);
        main.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        setLayout(new BorderLayout());
        add(main,BorderLayout.CENTER);
        pack();
    }

    public void actionPerformed(ActionEvent e)
    {
        setVisible(false);
        dispose();
    }

}
