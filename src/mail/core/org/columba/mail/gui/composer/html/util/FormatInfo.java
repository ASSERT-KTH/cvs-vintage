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

package org.columba.mail.gui.composer.html.util;

import javax.swing.text.AttributeSet;

/**
 * Objects of this class is used to inform observers of the
 * HtmlEditorController about the current format (bold, italic etc.)
 * and other information about the current selection
 * 
 * @author Karl Peder Olesen (karlpeder)
 */
public class FormatInfo {
	
	/** Holds attributes from the text in the editor at the curret caret pos. */
	private AttributeSet attributes;
	
	/** Flag telling whether text is currently selected in the editor */
	private boolean textSelected;
	
	/**
	 * Default constructor
	 * 
	 * @param	attr	Attributes at the curret caret position
	 * @param	select	True if some text is currently selected
	 */
	public FormatInfo(AttributeSet attr, boolean select) {
		attributes   = attr;
		textSelected = select;
	}
	
	/**
	 * Returns true if some text is currently selected in the editor
	 */
	public boolean isTextSelected() {
		return textSelected;
	}
	
	/**
	 * Returns the attributes of the text at the current caret position
	 */
	public AttributeSet getTextAttributes() {
		return attributes;
	}
}
