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
package org.columba.mail.gui.message.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author fdietz
 *  
 */
public class SpamStatusView extends JPanel {

    private JLabel label;

    private JButton button;

    private JPanel panel;

    public SpamStatusView() {
        super();

        panel = new JPanel();
        label = new JLabel("");
        button = new JButton("No Spam");
        
        layoutComponents(false);

    }

    protected void layoutComponents(boolean isSpam) {
       
        if (isSpam) {
            panel.removeAll();
            
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
            panel.setBackground(new Color(1.0f,0.8f,0.5f));
            panel.setLayout(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            add(panel, BorderLayout.CENTER);

            panel.add(label, BorderLayout.WEST);

            panel.add(button, BorderLayout.EAST);
        } else {
            removeAll();
            setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        }

        revalidate();
        updateUI();
    }

    /**
     * @see javax.swing.JComponent#updateUI()
     */
    public void updateUI() {
        super.updateUI();

        setBackground(Color.white);
        if (panel != null) panel.setBackground(Color.orange);
        
        if ( label != null ) label.setFont(label.getFont().deriveFont(Font.BOLD));
        
    }

    public void setSpam(boolean isSpam) {

        if (label != null) {
            if (isSpam == true)
                label.setText("Message is marked as spam");
            else
                label.setText("");

            layoutComponents(isSpam);
        }
    }
}
