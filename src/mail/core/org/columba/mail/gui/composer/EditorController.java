// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
	ComposerModel model;
	EditorView view;
	
	private UndoDocument document;
	
	public EditorController(ComposerModel model)
	{
		this.model = model;
	
		document = new UndoDocument();
		
		view = new EditorView(model, document);
		
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
			if ( model.getBodyText() != null )
				view.setText( model.getBodyText() );
		}
		else
		{
			if ( view.getText() != null )
				model.setBodyText( view.getText() );
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
