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
package org.columba.chat.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.chat.AlturaComponent;
import org.columba.chat.frame.AlturaFrameMediator;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.jivesoftware.smack.XMPPException;

/**
 * @author fdietz
 *  
 */
public class AddContactAction extends AbstractColumbaAction {

    /**
     * @param mediator
     * @param name
     */
    public AddContactAction(IFrameMediator mediator) {
        super(mediator, "Add Contact...");
        
        putValue(AbstractColumbaAction.TOOLBAR_NAME, "Add Contact");

    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
//      prompt for jabber id
        String jabberId = JOptionPane.showInputDialog(null, "Enter jabber ID");
        
        // if user cancelled action
        if ( jabberId == null) return;
        
//      example: fdietz@jabber.org/Jabber-client
        // -> remove "/Jabber-client"
        String normalizedFrom = jabberId.replaceAll("\\/.*", "");
        
        try {
            // add contact to roaster, nickname="", group=null
        	AlturaComponent.connection.getRoster().createEntry(jabberId, "",
                    null);
            System.out.println("update tree");
            ((AlturaFrameMediator)frameMediator).getBuddyTree().populate();
        } catch (XMPPException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }

    }
}
