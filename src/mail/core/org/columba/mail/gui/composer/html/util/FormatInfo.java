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
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;

/**
 * Objects of this class is used to inform observers of the
 * HtmlEditorController about the current format (bold, italic etc.)
 * and other information about the current selection
 * 
 * @author Karl Peder Olesen (karlpeder)
 */
public class FormatInfo {
	
	/** Reference to html document used by editor */
	private ExtendedHTMLDocument htmlDoc;
	
	/** Current caret position */
	private int caretPos;
	
	/** Flag telling whether text is currently selected in the editor */
	private boolean textSelected;
	
	/**
	 * Default constructor
	 *
	 * @param	doc		Html document used by editor
	 * @param	pos		Current caret position
	 * @param	select	True if some text is currently selected
	 */
	public FormatInfo(ExtendedHTMLDocument doc,
					  int pos, 
					  boolean select) {
		htmlDoc        	= doc;
		caretPos		= pos;
		textSelected	= select;
	}
	
	/**
	 * Private utility to return the character attributes 
	 * at the current caret position.
	 * NB: To get the right attributes, caret position - 1 is used!!! 
	 */
	private AttributeSet getCharAttr() {
		return htmlDoc.
				getCharacterElement(caretPos - 1).
						getAttributes();
	}
	/**
	 * Returns true if some text is currently selected in the editor
	 */
	public boolean isTextSelected() {
		return textSelected;
	}
	
	/**
	 * Convenience method to determine whether current text is bold
	 * @return	true if text is bold
	 */
	public boolean isBold() {
		if (StyleConstants.isBold(getCharAttr())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Convenience method to determine whether current text is italic
	 * @return	true if text is italic
	 */
	public boolean isItalic() {
		if (StyleConstants.isItalic(getCharAttr())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Convenience method to determine whether current text is underlined
	 * @return	true if text is underlined
	 */
	public boolean isUnderline() {
		if (StyleConstants.isUnderline(getCharAttr())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Convenience method to determine whether current text is formattet
	 * as heading 1 (H1)
	 * @return	true if text is formattet as heading 1
	 */
	public boolean isHeading1() {
		return checkIfTagIsParent(HTML.Tag.H1);
	}

	/**
	 * Convenience method to determine whether current text is formattet
	 * as heading 2 (H2)
	 * @return	true if text is formattet as heading 2
	 */
	public boolean isHeading2() {
		return checkIfTagIsParent(HTML.Tag.H2);
	}

	/**
	 * Convenience method to determine whether current text is formattet
	 * as heading 3 (H3)
	 * @return	true if text is formattet as heading 3
	 */
	public boolean isHeading3() {
		return checkIfTagIsParent(HTML.Tag.H3);
	}
	
	/**
	 * Convenience method to determine whether current text is formattet
	 * as "preformattet" (pre tag)
	 * @return	true if text is preformattet
	 */
	public boolean isPreformattet() {
		return checkIfTagIsParent(HTML.Tag.PRE);
	}
	
	/**
	 * Convenience method to determine whether current text is formattet
	 * as "address" (address tag)
	 * @return	true if text is formattet as adress
	 */
	public boolean isAddress() {
		return checkIfTagIsParent(HTML.Tag.ADDRESS);
	}
	
	/**
	 * Private utility to check whether a given tag is found 
	 * as parent at the current caret position.
	 * <br>
	 * This is used to check for a given paragraph format
	 * 
	 * @param	tag		Tag to search for
	 * @return	true if the tag is found as parent
	 */
	private boolean checkIfTagIsParent(HTML.Tag tag) {
		Element e = htmlDoc.getParagraphElement(caretPos);
		String current = e.getName(); 
		do {
			if (current.equalsIgnoreCase(tag.toString())) {
				return true;	// found
			}
			e = e.getParentElement();
			current = e.getName();
		} while (!current.equalsIgnoreCase(HTML.Tag.HTML.toString()));
		return false;			// not found		
	}
}
