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

import java.awt.Insets;

import javax.swing.JToolBar;

import org.columba.core.gui.util.CButton;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ComposerToolbar extends JToolBar{

	public ComposerToolbar( ComposerInterface composerInterface )
	{
		super();
		
		
		/*
		WindowItem item =
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem();
		*/
	
		
		CButton toolButton;

//		
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		setFloatable(false);

		

		toolButton =
			new CButton(
				composerInterface.composerActionListener.sendAction);
		add(toolButton);

		toolButton =
			new CButton(
				composerInterface.composerActionListener.attachFileAction);
		add(toolButton);

		//addSeparator();
		addSeparator();

		toolButton =
			new CButton(
				composerInterface.composerActionListener.undoAction);
		add(toolButton);
		
		toolButton =
			new CButton(
				composerInterface.composerActionListener.redoAction);
		add(toolButton);
		
		
		toolButton =
			new CButton(
				composerInterface.composerActionListener.cutAction);
		add(toolButton);

		toolButton =
			new CButton(
				composerInterface.composerActionListener.copyAction);
		add(toolButton);

		toolButton =
			new CButton(
				composerInterface.composerActionListener.pasteAction);
		add(toolButton);

		//addSeparator();
		//addSeparator();
		addSeparator();

		
		toolButton =
			new CButton(
				composerInterface.composerActionListener.spellCheckAction);
		add(toolButton);
		
		
		toolButton =
			new CButton(
				composerInterface.composerActionListener.addressbookAction);
		add(toolButton);
		

		//addSeparator();

		addSeparator();
		/*
		toolButton = new CButton(composerInterface.composerActionListener.exitAction);
		add( toolButton );
		*/
		//add(Box.createHorizontalGlue());

		

		//add(composerInterface.identityInfoPanel);

		


	}
}
