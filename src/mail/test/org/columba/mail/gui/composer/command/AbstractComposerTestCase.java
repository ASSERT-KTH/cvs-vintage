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

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.config.Config;
import org.columba.core.io.DiskIO;
import org.columba.core.main.MainInterface;
import org.columba.mail.config.AccountList;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.AbstractFolderTest;
import org.columba.mail.folder.MailboxTestFactory;
import org.columba.mail.main.MailInterface;

/**
 * @author fdietz
 *  
 */
public class AbstractComposerTestCase extends AbstractFolderTest {

    private File file;

    /**
     * @param arg0
     */
    public AbstractComposerTestCase(MailboxTestFactory factory, String arg0) {
        super(factory, arg0);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        super.setUp();

        // create config-folder
        file = new File("test_config");
        file.mkdir();

        // initialize configuration - core
        MainInterface.config = new Config(file);

        // initialize configuration - mail component
        MailInterface.config = new MailConfig(MainInterface.config);

        // initialize configuration - addressbook component
        AddressbookInterface.config = new AddressbookConfig(
                MainInterface.config);
        
        // init background manager (needed by ShutdownManager)
        MainInterface.backgroundTaskManager = new BackgroundTaskManager();
        
        // create default config-files
        MainInterface.config.init();
        
        AccountList list = MailInterface.config.getAccountList();
        list.addEmptyAccount("pop3");
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {

        super.tearDown();

        // remove configuration directory
        DiskIO.deleteDirectory(file);
    }
}