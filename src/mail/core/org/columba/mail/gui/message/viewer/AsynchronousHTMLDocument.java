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
package org.columba.mail.gui.message.viewer;

import javax.swing.text.html.HTMLDocument;

/**
 * Setting HTMLDocument to be an asynchronize model.
 * <p>
 * JTextPane therefore uses a background thread to display
 * the message. This dramatically improves the performance
 * of displaying a message.
 * <p>
 * Trick is to overwrite the getAsynchronousLoadPriority() to
 * return a decent value.
 * 
 * @author fdietz
 */
public class AsynchronousHTMLDocument extends HTMLDocument {

	/**
	 * 
	 */
	public AsynchronousHTMLDocument() {
		super();
	
	}
	
	/**
	 * From the JDK1.4 reference:
	 * <p>
	 * This may load either synchronously or asynchronously depending 
	 * upon the document returned by the EditorKit. If the Document is 
	 * of type AbstractDocument and has a value returned by 
	 * AbstractDocument.getAsynchronousLoadPriority  that is greater 
	 * than or equal to zero, the page will be loaded on a separate 
	 * thread using that priority.
	 * 
	 * @see javax.swing.text.AbstractDocument#getAsynchronousLoadPriority()
	 */
	public int getAsynchronousLoadPriority() {
		return 0;
	}
}
