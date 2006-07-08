package org.columba.core.gui.htmlviewer;

import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.IExtensionHandlerKeys;
import org.columba.api.plugin.PluginException;
import org.columba.api.plugin.PluginHandlerNotFoundException;
import org.columba.core.gui.htmlviewer.api.IHTMLViewerPlugin;
import org.columba.core.logging.Logging;
import org.columba.core.plugin.PluginManager;


public class HTMLViewerFactory {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.htmlviewer.HTMLViewerFactory");

	public static IHTMLViewerPlugin createHTMLViewer() {
//		OptionsItem optionsItem = MailConfig.getInstance().getOptionsItem();
//		String selectedBrowser = optionsItem.getStringWithDefault(
//				OptionsItem.MESSAGEVIEWER, OptionsItem.SELECTED_BROWSER,
//				"Default");
		String selectedBrowser = "Default";
		IHTMLViewerPlugin viewerPlugin;

		try {
			viewerPlugin = createHTMLViewerPluginInstance(selectedBrowser);
			// in case of an error -> fall-back to Swing's built-in JTextPane
			if (viewerPlugin == null || !viewerPlugin.initialized()) {
				JOptionPane.showMessageDialog(null,
						"Error while trying to load html viewer");

				LOG
						.severe("Error while trying to load html viewer -> falling back to default");

				viewerPlugin = createHTMLViewerPluginInstance("Default");
			}
		} catch (Exception e) {
			viewerPlugin = createHTMLViewerPluginInstance("Default");

			if (Logging.DEBUG)
				e.printStackTrace();
		} catch (Error e) {
			viewerPlugin = createHTMLViewerPluginInstance("Default");

			if (Logging.DEBUG)
				e.printStackTrace();
		}

		return viewerPlugin;
	}

	private static IHTMLViewerPlugin createHTMLViewerPluginInstance(
			String pluginId) {
		IHTMLViewerPlugin plugin = null;
		try {

			IExtensionHandler handler = PluginManager.getInstance()
					.getExtensionHandler(
							IExtensionHandlerKeys.ORG_COLUMBA_CORE_HTMLVIEWER);

			IExtension extension = handler.getExtension(pluginId);
			if (extension == null)
				return null;

			plugin = (IHTMLViewerPlugin) extension.instanciateExtension(null);

			return plugin;
		} catch (PluginHandlerNotFoundException e) {
			LOG.severe("Error while loading viewer plugin: " + e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		} catch (PluginException e) {
			LOG.severe("Error while loading viewer plugin: " + e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		}

		return null;
	}

}
