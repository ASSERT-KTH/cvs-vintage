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

import javax.swing.JComponent;

import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameMediator;


/**
 * Viewer displaying spam status information.
 * 
 * @author fdietz
 *
 */
public class SpamStatusController implements Viewer {

    private SpamStatusView label;
    
    public SpamStatusController() {
        super();
        
        label = new SpamStatusView();
    }
    /**
     * @see org.columba.mail.gui.message.status.Status#show(org.columba.mail.folder.Folder, java.lang.Object)
     */
    public void view(Folder folder, Object uid, MailFrameMediator mediator) throws Exception {
        Boolean spam = (Boolean) folder.getAttribute(uid, "columba.spam");
        
        label.setSpam(spam.booleanValue());
        
        
       
    }

    /**
     * @see org.columba.mail.gui.message.viewer.Viewer#getView()
     */
    public JComponent getView() {
        return label;
    }

}
