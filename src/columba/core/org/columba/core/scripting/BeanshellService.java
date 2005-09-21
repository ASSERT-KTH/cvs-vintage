/*

 The contents of this file are subject to the Mozilla Public License Version 1.1 
 (the "License") you may not use this file except in compliance with the License. 

 You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Original Code is "BshInterpreter plugin for The Columba Project"

 The Initial Developer of the Original Code is Celso Pinto
 Portions created by Celso Pinto are Copyright (C) 2005.
 Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

 All Rights Reserved.

 */
package org.columba.core.scripting;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.columba.core.gui.globalactions.BeanshellManagerAction;
import org.columba.core.io.DiskIO;
import org.columba.core.scripting.config.BeanshellConfig;
import org.columba.core.scripting.extensions.ExtensionPointManager;
import org.columba.core.scripting.extensions.MenuExtensionPoint;
import org.columba.core.scripting.service.IColumbaService;

/**
 * This class represents the Beanshell Service.<br>
 * The Beanshell Service enables the use of scriptable plugins, meaning a
 * 3rd-party developer can created plugins based on beanshell scripts. <br>
 * To create a Beanshell plugin, the 3rd party must create one script with a
 * .bsh extension and copy the file to the ~/.columba/scripts directory. <br>
 * <br>
 * The service will then automaticaly pick up the script and execute it. <br>
 * If a plugin depends on more than one script, then only the entry point should
 * have the .bsh extension, for example:<br> - my_plugin.bsh<br> -
 * my_plugin.file_2<br> - my_plugin.file_3<br> - ...<br>
 * <br>
 * <br>
 * <strong>This is still alpha software so expect things to change.</strong>
 * <br>
 * 
 * @author Celso Pinto (cpinto@yimports.com)
 */
public class BeanshellService implements IColumbaService {

	private static final Logger LOG = Logger.getLogger(BeanshellService.class
			.getName());

	private BeanshellConfig config = BeanshellConfig.getInstance();

	private Map beanshellScripts = new HashMap();

	public BeanshellService() {
		super();
	}

	/**
	 * @see org.columba.core.plugin.IColumbaService#initService()
	 */
	public boolean initService() {

		/* check if script directory exists */
		if (!DiskIO.ensureDirectory(config.getPath()))
			return false;

		/*
		 * seems like we're ready to go add a new configuration hook, so that
		 * scripts can be maintained in a configuration dialog of some sort
		 */
		if (!addMenuItem())
			return false;

		/*
		 * initialize file observer thread with a reference to our
		 * beanshellScripts map
		 */
		FileObserverThread.getInstance().setScriptList(beanshellScripts);

		LOG.info("BeanshellService initialized...");
		return true;

	}

	private boolean addMenuItem() {

		MenuExtensionPoint menu = (MenuExtensionPoint) ExtensionPointManager
				.getInstance().getExtensionPoint(
						MenuExtensionPoint.EXTENSION_POINT_ID);

		menu.addAction(new BeanshellManagerAction(),
				"utilities",
				"bottom");

		return true;
	}

	/**
	 * @see org.columba.core.plugin.IColumbaService#disposeService()
	 */
	public void disposeService() {
		/* nothing to dispose, yet... */
	}

	/**
	 * @see org.columba.core.plugin.IColumbaService#startService()
	 */
	public void startService() {
		/* start pooling thread */
		LOG.fine("Starting " + getClass().getName());
		LOG.fine("Starting FileObserverThread...");
		FileObserverThread.getInstance().start();
	}

	/**
	 * @see org.columba.core.plugin.IColumbaService#stopService()
	 */
	public void stopService() {
		LOG.fine("Stoping " + getClass().getName());
		LOG.fine("Stopping FileObserverThread...");
		FileObserverThread.getInstance().finish();
	}

	public Map getBeanshellScripts() {
		return beanshellScripts;
	}

}
