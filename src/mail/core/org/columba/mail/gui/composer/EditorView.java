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
//All Rights Reserved.undation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.composer;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JTextPane;

import org.columba.core.config.Config;
import org.columba.mail.gui.composer.util.UndoDocument;


/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class EditorView extends JTextPane  {
	ComposerModel model;

	private UndoDocument message;

	public EditorView(ComposerModel model, UndoDocument m) {
		super();

		this.model = model;

		message = m;

		setStyledDocument(message);
		setEditable(true);

		Font font = Config.getOptionsConfig().getGuiItem().getTextFont();
		
		setFont(font);
		
		setPreferredSize(new Dimension(300, 200));

		
	}
	
	public void installListener( EditorController controller )
	{
		message.addDocumentListener(controller);
	}
	

	

}