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

package org.columba.core.main;

import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.command.DefaultProcessor;
import org.columba.core.config.Config;
import org.columba.core.gui.ClipboardManager;
import org.columba.core.gui.frame.FrameModelManager;
import org.columba.core.plugin.PluginManager;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.mail.gui.tree.TreeModel;
import org.columba.mail.pgp.PGPController;
import org.columba.mail.pop3.POP3ServerCollection;

public class MainInterface {

	public static final String version = "0.10.1";

	public static boolean DEBUG = false;

	public static Config config;

	public static POP3ServerCollection popServerCollection;

	public static TreeModel treeModel;

	public static AddressbookTreeModel addressbookTreeModel;

	public static DefaultProcessor processor;

	public static PGPController pgpController;

	public static PluginManager pluginManager;

	public static ShutdownManager shutdownManager;
	
	public static BackgroundTaskManager backgroundTaskManager;

	public static FrameModelManager frameModelManager;
	
	public static ClipboardManager clipboardManager;

	public MainInterface() {
	}
}
