package org.columba.core.gui.htmlviewer;

import javax.swing.JComponent;

import org.columba.api.plugin.IExtensionInterface;

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
	void view(String body);
	
	
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
	 * Return embedded view.
	 * 
	 * @return	view
	 */
	JComponent getComponent();
	
	/**
	 * Return container.
	 * 
	 * @return
	 */
	JComponent getContainer();
}
