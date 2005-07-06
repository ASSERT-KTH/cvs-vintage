package org.columba.core.gui.htmlviewer;

import java.net.URL;

import javax.swing.JComponent;

import org.columba.core.plugin.IExtensionInterface;

/**
 * HTML Viewer interface. 
 * 
 * @author Frederik Dietz
 */
public interface IHTMLViewerPlugin extends IExtensionInterface{

	/**
	 * View HTML page using the source string.
	 * 
	 * @param htmlSource	HTML source string
	 */
	void view(String htmlSource);
	
	/**
	 * View HTML page using the URL.
	 * 
	 * @param url	URL to HTML page
	 */
	void view(URL url);
	
	/**
	 * Get selected text.
	 * 
	 * @return	selected text
	 */
	String getSelectedText();
	
	/**
	 * Check if HTML viewer was initialized correctly.
	 * 
	 * @return	true, if initialized correctly. False, otherwise.
	 * 
	 */
	boolean initialized();
	
	/**
	 * Get view.
	 * 
	 * @return	view
	 */
	JComponent getView();
}
