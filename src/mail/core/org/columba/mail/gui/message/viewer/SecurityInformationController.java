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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message.viewer;

import javax.swing.JComponent;

import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.filter.SecurityStatusEvent;
import org.columba.mail.gui.message.filter.SecurityStatusListener;

/**
 * Viewer displays security status information.
 * 
 * @author fdietz
 *  
 */
public class SecurityInformationController implements Viewer,
        SecurityStatusListener {

	
    public static final int DECRYPTION_SUCCESS = 0;

    public static final int DECRYPTION_FAILURE = 1;

    public static final int VERIFICATION_SUCCESS = 2;

    public static final int VERIFICATION_FAILURE = 3;

    public static final int NO_KEY = 4;

    public static final int NOOP = 5;

    private SecurityInformationView panel;

    private boolean visible;

    public SecurityInformationController() {
        super();

        panel = new SecurityInformationView();

        visible = false;
    }

    /**
     * @see org.columba.mail.gui.message.viewer.Viewer#view(org.columba.mail.folder.Folder,
     *      java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
     */
    public void view(MessageFolder folder, Object uid, MailFrameMediator mediator)
            throws Exception {

    }

    /**
     * @see org.columba.mail.gui.message.viewer.Viewer#getView()
     */
    public JComponent getView() {
        return panel;
    }

    /**
     * @see org.columba.mail.gui.message.filter.SecurityStatusListener#statusUpdate(org.columba.mail.gui.message.filter.SecurityStatusEvent)
     */
    public void statusUpdate(SecurityStatusEvent event) {
        String message = event.getMessage();
        int status = event.getStatus();

        panel.setValue(status, message);

        if (status == NOOP)
            visible = false;
        else
            visible = true;
    }

    /**
     * @see org.columba.mail.gui.message.viewer.Viewer#isVisible()
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
	 * @see org.columba.mail.gui.message.viewer.Viewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		

	}
}
