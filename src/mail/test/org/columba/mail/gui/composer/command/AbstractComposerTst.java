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
package org.columba.mail.gui.composer.command;

import java.io.File;

import org.columba.core.config.Config;
import org.columba.core.io.DiskIO;
import org.columba.mail.config.AccountList;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.AbstractFolderTst;
import org.columba.mail.folder.MailboxTstFactory;

/**
 * @author fdietz
 *  
 */
public class AbstractComposerTst extends AbstractFolderTst {

    private File file;

    public AbstractComposerTst(String arg0) {
        super(arg0);
        
    }
    /**
     * @param arg0
     */
    public AbstractComposerTst(MailboxTstFactory factory, String arg0) {
        super(factory, arg0);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

    	super.setUp();
    	    
        AccountList list = MailConfig.getInstance().getAccountList();
        list.addEmptyAccount("pop3");
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {

        super.tearDown();

        // remove configuration directory
        if ( file != null)
        	DiskIO.deleteDirectory(file);
    }
}