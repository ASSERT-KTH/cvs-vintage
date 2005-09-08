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
package org.columba.mail.gui.message.viewer;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.logging.Logger;

import org.columba.core.xml.XmlElement;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.message.IMessageController;


/**
 * Title:
 * Description: The MarkAsReadTimer marks a Message as read after a user defined
 * time. This class self implements a actionListener. The class as a ActionListener is
 * added to his own timer as ActionListener. So if the timer is started an then finished
 * the timer calls the actionPerfomred Method of this class to do the marking thinks.
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author fdietz, waffel
 * @version 1.0
 */
public class MarkAsReadTimer implements Observer {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.table.util");

    // definition of a second
    private static final int ONE_SECOND = 1000;

    // timer to use
    private int delay;
    private boolean enabled;
    
    private Timer timer;
    
    // Singleton
    private static MarkAsReadTimer myInstance = new MarkAsReadTimer();
    
    /**
    * Creates a new MarkAsReadTimer. This should be only onced in a Session. The contructor
    * fetched the time delay that the user configured.
    */
    protected MarkAsReadTimer() {
        getConfigurationValues();

        timer = new Timer();
    }

	/**
	 * 
	 */
	private void getConfigurationValues() {
		XmlElement markasread = MailConfig.getInstance().get("options").getElement("/options/markasread");

        // listen for configuration changes
        markasread.addObserver(this);

        // get interval value
        String delayString = markasread.getAttribute("delay", "2");
        delay = Integer.parseInt(delayString);

        // enable timer
        String enabledString = markasread.getAttribute("enabled", "true");
        enabled = enabledString.equals("true") ? true : false;
	}

    public static MarkAsReadTimer getInstance() {
    	return myInstance;
    }   
 
    /**
    * Restarts the timer. The given message is used later in the actionPerfomed mathod.
    * This method is for example used by the ViewMessageCommand to restart the timer if a
    * message is shown
    */
    public void start(IMessageController controller, IMailFolderCommandReference reference) {
    	if(enabled) {    	
    		timer.schedule( new MarkAsReadTimerTask(controller, reference), ONE_SECOND * delay);
    	}
    }
 
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1) {
        LOG.info("/options/markasread#delay has changed");
        
        getConfigurationValues();
    }
}
