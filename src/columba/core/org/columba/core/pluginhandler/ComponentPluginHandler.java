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
package org.columba.core.pluginhandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.columba.core.main.IComponentPlugin;
import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.plugin.PluginLoadingFailedException;

/**
 * Handler provides access to main entrypoint of components like addressbook and
 * mail.
 * <p>
 * An internal map is used to ensure that only one instance is loaded from each
 * component.
 * 
 * @author fdietz
 */
public class ComponentPluginHandler extends AbstractPluginHandler implements
		IComponentPlugin {

	private Map map;

	/**
	 * @param id
	 * @param config
	 */
	public ComponentPluginHandler() {
		super("org.columba.core.component",
				"org/columba/core/plugin/component.xml");

		parentNode = getConfig().getRoot().getElement("componentlist");
		
		map = new HashMap();

		
	}

	private void initDefaultPlugins() {
		getPlugin("MailComponent");
		getPlugin("AddressbookComponent");
	}

	public IComponentPlugin getPlugin(String id) {
		IComponentPlugin c = null;

		if (map.containsKey(id)) {
			// return cached instance
			c = (IComponentPlugin) map.get(id);
		} else {
			try {
				c = (IComponentPlugin) getPlugin(id, new Object[] {});
				map.put(id, c);
			} catch (PluginLoadingFailedException e) {

				e.printStackTrace();
			}
		}

		return c;
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#init()
	 */
	public void init() {
		Iterator it = map.values().iterator();
		while (it.hasNext()) {
			IComponentPlugin p = (IComponentPlugin) it.next();
			p.init();
		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#postStartup()
	 */
	public void postStartup() {
		Iterator it = map.values().iterator();
		while (it.hasNext()) {
			IComponentPlugin p = (IComponentPlugin) it.next();
			p.postStartup();
		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#registerCommandLineArguments()
	 */
	public void registerCommandLineArguments() {
//		 init mail/addressbook internal components
		initDefaultPlugins();
		
		Iterator it = map.values().iterator();
		while (it.hasNext()) {
			IComponentPlugin p = (IComponentPlugin) it.next();
			p.registerCommandLineArguments();
		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#handleCommandLineParameters(org.apache.commons.cli.CommandLine)
	 */
	public void handleCommandLineParameters(CommandLine commandLine) {
		Iterator it = map.values().iterator();
		while (it.hasNext()) {
			IComponentPlugin p = (IComponentPlugin) it.next();
			p.handleCommandLineParameters(commandLine);
		}
	}

}