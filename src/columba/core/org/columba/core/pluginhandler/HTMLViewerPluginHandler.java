package org.columba.core.pluginhandler;

import org.columba.core.plugin.AbstractPluginHandler;

public class HTMLViewerPluginHandler extends AbstractPluginHandler {

	public HTMLViewerPluginHandler() {

		super("org.columba.core.htmlviewer",
				"org/columba/core/plugin/htmlviewer.xml");

		parentNode = getConfig().getRoot().getElement("htmlviewerlist");
	}

}
