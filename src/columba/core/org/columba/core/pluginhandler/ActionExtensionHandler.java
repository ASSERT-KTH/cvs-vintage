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
package org.columba.core.pluginhandler;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.Main;
import org.columba.core.plugin.ExtensionHandler;
import org.columba.core.plugin.IExtension;
import org.columba.core.plugin.exception.PluginException;

/**
 * Every action in Columba is handled by this class.
 * <p>
 * The core actions are listed in the org.columba.core.action.action.xml
 * <p>
 * These actions are used to generate the menu and the toolbar dynamically.
 * 
 * @author fdietz
 */
public class ActionExtensionHandler extends ExtensionHandler {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.pluginhandler");

	public static final String XML_RESOURCE = "/org/columba/core/action/action.xml";

	public static final String NAME = "org.columba.core.action";

	public ActionExtensionHandler() {
		super(ActionExtensionHandler.NAME);

		InputStream is = this.getClass().getResourceAsStream(XML_RESOURCE);
		loadExtensionsFromStream(is);

	}

	public AbstractColumbaAction getAction(String id, FrameMediator controller) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (controller == null)
			throw new IllegalArgumentException("controller == null");

		IExtension extension = getExtension(id);

		AbstractColumbaAction a = null;

		try {
			if (extension != null)
				a = (AbstractColumbaAction) extension
						.instanciateExtension(new Object[] { controller });
		} catch (PluginException e) {
			LOG.severe(e.getMessage());
			if (Main.DEBUG)
				e.printStackTrace();

		}

		return a;

	}

	public IMenu getIMenu(String id, FrameMediator controller) throws Exception {
		IExtension extension = getExtension(id);

		IMenu menu = null;
		try {
			menu = (IMenu) extension
					.instanciateExtension(new Object[] { controller });
		} catch (PluginException e) {
			e.printStackTrace();
		}

		return menu;
	}

}