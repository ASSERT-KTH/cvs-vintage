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
package org.columba.mail.gui.composer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.mail.gui.composer.util.UndoDocument;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class EditorController implements DocumentListener {
	EditorView view;
	ComposerController controller;
	
	private UndoDocument document;
	
	public EditorController(ComposerController controller)
	{
		this.controller = controller;
	
		document = new UndoDocument();
		
		view = new EditorView(this, document);
		
	}
	
	public EditorView getView()
	{
		return view;
	}
	
	public void installListener()
	{
		view.installListener(this);
	}
	
	public void updateComponents( boolean b )
	{
		if ( b == true )
		{
			if ( controller.getModel().getBodyText() != null )
				view.setText( controller.getModel().getBodyText() );
		}
		else
		{
			if ( view.getText() != null )
			controller.getModel().setBodyText( view.getText() );
		}
	}
	
	public void undo() {
		document.Undo();
	}
	
	public void redo() {
		document.Redo();
	}
	
	
	
	/************* DocumentListener implementation *******************/
	
	
	public void insertUpdate(DocumentEvent e) {
		
	}
	public void removeUpdate(DocumentEvent e) {
		
	}
	public void changedUpdate(DocumentEvent e) {
		
	}

}
