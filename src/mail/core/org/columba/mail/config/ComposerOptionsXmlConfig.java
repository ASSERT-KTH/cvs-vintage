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

package org.columba.mail.config;

import java.io.File;

import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.config.ViewItem;

public class ComposerOptionsXmlConfig extends DefaultXmlConfig
{
	//private File file;
	ViewItem viewItem;
	SpellcheckItem spellcheckItem;

	public ComposerOptionsXmlConfig(File file)
	{
		super(file);
	}

	
	
	public ViewItem getViewItem()
	{
		if ( viewItem == null )
		{
			viewItem = new ViewItem( getRoot().getElement("/options/gui/view") );
		}
		
		return viewItem;
		
		

	}
	
	
	
	public SpellcheckItem getSpellcheckItem()
	{
		if ( spellcheckItem == null )
		{
			spellcheckItem = new SpellcheckItem(getRoot().getElement("/options/spellcheck"));
		}
		
		return spellcheckItem;
	}
	

}