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
package org.columba.api.plugin;

public interface IExtensionHandlerKeys {

	/**
	 * Every action in Columba is handled by this class.
	 * <p>
	 * These actions are used to generate the menu and the toolbar dynamically.
	 * <p>
	 * @see core/org.columba.core.ui.globalaction
	 */
	public static final String ORG_COLUMBA_CORE_ACTION = "org.columba.core.action";

	/**
	 * Handler provides access to main entrypoint of components like addressbook
	 * and mail.
	 * <p>
	 * @see contact/org.columba.addressbook.main
	 * @see mail/org.columba.mail.main
	 */
	public static final String ORG_COLUMBA_CORE_COMPONENT = "org.columba.core.component";

	/**
	 * ConfigExtensionHandler provides an easy way for plugins to have their own
	 * configuration dialog.
	 * <p>
	 * Note that every plugin can have its own configuration file "config.xml"
	 * in its folder anyway.
	 */
	public static final String ORG_COLUMBA_CORE_CONFIG = "org.columba.core.config";

	/**
	 * Provides an easy way to integrate external apps in Columba.
	 * <p>
	 * This includes a first-time assistant for the user. And a configuration
	 * file "external_tools.xml" to store the options of the external tools.
	 * <p>
	 * When using external commandline (already used examples are aspell and
	 * GnuPG) tools, you should just use this handler to get the location of the
	 * executable.
	 * <p>
	 * If the executable wasn't configured, yet a wizard will assist the user in
	 * configuring the external tool. If everything is correctly configured, it
	 * will just return the path of the commandline tool as <code>File</code>.
	 * <p>
	 * <verbatim> File file = getLocationOfExternalTool("gpg"); </verbatim>
	 * 
	 */
	public static final String ORG_COLUMBA_CORE_EXTERNALTOOLS = "org.columba.core.externaltools";

	/**
	 * Frame component which is shown in the JFrame container's content pane. 
	 */
	public static final String ORG_COLUMBA_CORE_FRAME = "org.columba.core.frame";

	/**
	 * Html Viewer component.
	 * <p>
	 * @see core/org.columba.core.gui.htmlviewer
	 */
	public static final String ORG_COLUMBA_CORE_HTMLVIEWER = "org.columba.core.htmlviewer";

	/**
	 * Tray Icon plugged into the statusbar
	 */
	public static final String ORG_COLUMBA_CORE_STATUSBAR = "org.columba.core.statusbar";

	/**
	 * Look and Feel plugin handler.
	 */
	public static final String ORG_COLUMBA_CORE_THEME = "org.columba.core.theme";

	/**
	 * Views found in package org.columba.core.gui.view are loaded dynamically.
	 * <p>
	 * This makes it possible to write a plugin, for the mail component where
	 * the view has a completely different layout.
	 */
	public static final String ORG_COLUMBA_CORE_VIEW = "org.columba.core.view";

	/**
	 * Background services
	 */
	public static final String ORG_COLUMBA_CORE_SERVICE = "org.columba.core.service";

	/**
	 * Actions can be registered. These actions should be representing "new item".
	 * For example "New Mail", "New Contact", "New Appointment". 
	 * <p>
	 * They will be promptly presented in the "File->New" menu and the toolbar.
	 */
	public static final String ORG_COLUMBA_CORE_NEWITEM = "org.columba.core.newitem";
	
	/**
	 * Search Providers should implement <code>org.columba.core.search.api.ISearchProvider</code>
	 */
	public static final String ORG_COLUMBA_CORE_SEARCH = "org.columba.core.search";
	
	/**
	 * Search Providers should implement <code>org.columba.core.search.ui.api.IResultPanel</code>
	 * to display the search results appropriately.
	 */
	public static final String ORG_COLUMBA_CORE_SEARCH_UI = "org.columba.core.search.ui";
}
