package org.columba.core.gui.htmlviewer.api;

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
	
	String getText();
	
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
	
	/**
	 * Sets the position of the text insertion caret for the TextComponent. Note
	 * that the caret tracks change, so this may move if the underlying text of
	 * the component is changed. If the document is null, does nothing. The
	 * position must be between 0 and the length of the component's text or else
	 * an exception is thrown.
	 * 
	 * @param position
	 */
	public void setCaretPosition(int position);

	/**
	 * Moves the caret to a new position, leaving behind a mark defined by the
	 * last time setCaretPosition was called. This forms a selection. If the
	 * document is null, does nothing. The position must be between 0 and the
	 * length of the component's text or else an exception is thrown.
	 * 
	 * @param position
	 */
	public void moveCaretPosition(int position);
	
}
