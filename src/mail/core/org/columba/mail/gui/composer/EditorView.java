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

		Font font =
			new Font(
				Config.getOptionsConfig().getThemeItem().getTextFontName(),
				Font.PLAIN,
				Config.getOptionsConfig().getThemeItem().getTextFontSize());
		setFont(font);
		
		setPreferredSize(new Dimension(300, 200));

		
	}
	
	public void installListener( EditorController controller )
	{
		message.addDocumentListener(controller);
	}
	

	

}