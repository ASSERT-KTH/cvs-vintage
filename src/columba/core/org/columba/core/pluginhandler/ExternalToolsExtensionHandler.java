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

import org.columba.core.plugin.ExtensionHandler;

/**
 * Provides an easy way to integrate external apps in Columba.
 * <p>
 * This includes a first-time assistant for the user. And a configuration file
 * "external_tools.xml" to store the options of the external tools.
 * <p>
 * When using external commandline (already used examples are aspell and GnuPG)
 * tools, you should just use this handler to get the location of the
 * executable.
 * <p>
 * If the executable wasn't configured, yet a wizard will assist the user in
 * configuring the external tool. If everything is correctly configured, it will
 * just return the path of the commandline tool as <code>File</code>.
 * <p>
 * <verbatim> File file = getLocationOfExternalTool("gpg"); </verbatim>
 * 
 * <p>
 * 
 * @see org.columba.api.plugin.external_tools.xml
 * 
 * @author fdietz
 */
public class ExternalToolsExtensionHandler extends ExtensionHandler {

	public static final String XML_RESOURCE = "/org/columba/core/plugin/external_tools.xml";

	public static final String NAME = "org.columba.core.externaltools";

	/**
	 * @param id
	 * @param config
	 */
	public ExternalToolsExtensionHandler() {

		super(NAME);

		InputStream is = this.getClass().getResourceAsStream(XML_RESOURCE);
		loadExtensionsFromStream(is);
	}

}
