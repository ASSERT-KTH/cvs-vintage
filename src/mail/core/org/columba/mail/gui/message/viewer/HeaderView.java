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

import javax.swing.BorderFactory;
import javax.swing.JPanel;


/**
 * @author fdietz
 *
 */
public class HeaderView extends JPanel {

    private HeaderTextPane headerTextPane;
    private StatusPanel statusPanel;
    
    public HeaderView() {
        super();
        
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
        
        JPanel panel = new JPanel();
        
        add(panel, BorderLayout.CENTER);
        
        panel.setLayout( new BorderLayout());
        
        panel.setBorder(BorderFactory.createLineBorder(Color.gray));
    
        headerTextPane = new HeaderTextPane();
    
        statusPanel = new StatusPanel();
        
        panel.add(headerTextPane, BorderLayout.CENTER);
        
        panel.add(statusPanel, BorderLayout.EAST);
        
    }
    
   
    /**
     * @return Returns the headerTextPane.
     */
    public HeaderTextPane getHeaderTextPane() {
        return headerTextPane;
    }
    /**
     * @see javax.swing.JComponent#updateUI()
     */
    public void updateUI() {
        super.updateUI();
        
        setBackground(Color.white);
    }
    /**
     * @return Returns the statusPanel.
     */
    public StatusPanel getStatusPanel() {
        return statusPanel;
    }
}
