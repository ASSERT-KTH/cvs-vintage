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
package org.columba.core.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.mail.util.MailResourceLoader;


/**
 * Dialg showing information to the user. This can be either a URL to document
 * or a string.
 *
 * @author fdietz
 */
public class InfoViewerDialog extends JDialog implements ActionListener {
    JButton helpButton;
    JButton closeButton;
    JTextPane textPane;

    public InfoViewerDialog(String message) {
        this();

        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet styles = new StyleSheet();

        Font font = UIManager.getFont("Label.font");
        String name = font.getName();
        int size = font.getSize();
        String css = "<style type=\"text/css\"><!--p {font-family:\"" + name +
            "\"; font-size:\"" + size + "pt\"}--></style>";
        styles.addRule(css);
        editorKit.setStyleSheet(styles);

        textPane.setEditorKit(editorKit);
        textPane.setText(message);
        setVisible(true);
    }

    /**
 * @throws java.awt.HeadlessException
 */
    public InfoViewerDialog(URL url) {
        this();

        try {
            textPane.setPage(url);
        } catch (IOException ex) {
            NotifyDialog d = new NotifyDialog();
            d.showDialog(ex);
        }

        setVisible(true);
    }

    protected InfoViewerDialog() {
        super(new JFrame(), "Info", true);
        initComponents();
        pack();
        setLocationRelativeTo(null);
        textPane.setEditable(false);
    }

    protected void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(mainPanel);

        // centerpanel
        textPane = new JTextPane();

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(450, 300));
        scrollPane.getViewport().setBackground(Color.white);

        mainPanel.add(scrollPane);

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

        //TODO: associate help with button and root pane
        buttonPanel.add(helpButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(closeButton);
        getRootPane().registerKeyboardAction(this, "CLOSE",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CLOSE")) {
            setVisible(false);
        }
    }
}
