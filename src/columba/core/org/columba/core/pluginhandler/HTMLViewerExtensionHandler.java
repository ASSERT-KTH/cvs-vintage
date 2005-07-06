package org.columba.core.pluginhandler;

import org.columba.core.plugin.ExtensionHandler;

public class HTMLViewerExtensionHandler extends ExtensionHandler {

	public static final String XML_RESOURCE = "org/columba/core/plugin/htmlviewer.xml";

	public static final String NAME = "org.columba.core.htmlviewer";

	public HTMLViewerExtensionHandler() {
		super(NAME);

		loadExtensionsFromFile(XML_RESOURCE);

	}

}
