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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.columba.core.main.IComponentPlugin;
import org.columba.core.plugin.ExtensionHandler;
import org.columba.core.plugin.IExtension;
import org.columba.core.plugin.exception.PluginException;
import org.columba.core.plugin.exception.PluginLoadingFailedException;
import org.columba.core.xml.XmlElement;

/**
 * Handler provides access to main entrypoint of components like addressbook and
 * mail.
 * <p>
 * An internal map is used to ensure that only one instance is loaded from each
 * component.
 * 
 * @author fdietz
 */
public class ComponentExtensionHandler extends ExtensionHandler implements
		IComponentPlugin {

	public static final String XML_RESOURCE = "/org/columba/core/plugin/component.xml";

	public static final String NAME = "org.columba.core.component";

	/**
	 * @param id
	 * @param config
	 */
	public ComponentExtensionHandler() {
		super(NAME);

		InputStream is = this.getClass().getResourceAsStream(XML_RESOURCE);
		loadExtensionsFromStream(is);

	}

	private void initDefaultPlugins() {
		getPlugin("MailComponent");
		getPlugin("AddressbookComponent");
	}

	public IComponentPlugin getPlugin(String id) {
		IComponentPlugin component = null;

		IExtension extension = getExtension(id);

		try {
			component = (IComponentPlugin) extension.instanciateExtension(null);
		} catch (Exception e) {
			handlePluginError(id);
		}

		return component;
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#init()
	 */
	public void init() {
		Iterator it = getMap().values().iterator();
		while (it.hasNext()) {
			IExtension ext = (IExtension) it.next();
			IComponentPlugin p;
			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.init();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#postStartup()
	 */
	public void postStartup() {
		Iterator it = getMap().values().iterator();
		while (it.hasNext()) {
			IExtension ext = (IExtension) it.next();
			IComponentPlugin p;
			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.postStartup();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#registerCommandLineArguments()
	 */
	public void registerCommandLineArguments() {
		// init mail/addressbook internal components
		// FIXME
		//initDefaultPlugins();

		Iterator it = getMap().values().iterator();
		while (it.hasNext()) {
			IExtension ext = (IExtension) it.next();
			IComponentPlugin p;
			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.registerCommandLineArguments();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#handleCommandLineParameters(org.apache.commons.cli.CommandLine)
	 */
	public void handleCommandLineParameters(CommandLine commandLine) {
		Iterator it = getMap().values().iterator();
		while (it.hasNext()) {
			IExtension ext = (IExtension) it.next();
			IComponentPlugin p;
			try {
				p = (IComponentPlugin) ext.instanciateExtension(null);
				p.handleCommandLineParameters(commandLine);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see org.columba.core.plugin.ExtensionHandler#addExtension(java.lang.String,
	 *      org.columba.core.plugin.IExtension)
	 */
	public void addExtension(String id, IExtension extension) {
		super.addExtension(id, extension);

		IComponentPlugin plugin = getPlugin(id);

		// TODO implement
		//plugin.registerCommandLineArguments();
		//plugin.init();

	}

	// /**
	// * @see
	// org.columba.core.plugin.AbstractPluginHandler#addExtension(java.lang.String,
	// * org.columba.core.xml.XmlElement)
	// */
	// public void addExtension(String id, XmlElement extension) {
	// super.addExtension(id, extension);
	//
	// // init plugin
	// XmlElement child = extension.getElement(0);
	// String name = child.getAttribute("name");
	//
	// IComponentPlugin plugin = getPlugin(name);
	// plugin.registerCommandLineArguments();
	// plugin.init();
	// }
}