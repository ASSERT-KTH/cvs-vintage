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

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.config.WindowItem;
import org.w3c.dom.Element;

public class ComposerOptionsXmlConfig extends DefaultXmlConfig
{
	private File file;

	public ComposerOptionsXmlConfig(File file)
	{
		super(file);
	}

	public AdapterNode getRootNode()
	{
		AdapterNode node = new AdapterNode(getDocument());

		AdapterNode rootNode = node.getChild(0);
		return rootNode;
	}

	public WindowItem getWindowItem()
	{
		

		AdapterNode rootNode = getRootNode();
		AdapterNode guiNode = rootNode.getChild("gui");
		AdapterNode windowNode = guiNode.getChild("window");


		return new WindowItem( getDocument(), windowNode );
		
		/*
		item.setXPositionNode(windowNode.getChild("xposition"));
		item.setYPositionNode(windowNode.getChild("yposition"));

		item.setWidthNode(windowNode.getChild("width"));
		item.setHeightNode(windowNode.getChild("height"));
		*/
		

		/*
		item.setShowToolbarNode( windowNode.getChild("showtoolbar") );
		item.setShowFilterToolbarNode( windowNode.getChild("showfiltertoolbar") );
		item.setShowFolderInfoNode( windowNode.getChild("showfolderinfo") );
		
		item.setThemeNode( windowNode.getChild("theme") );
		
		item.setMainSplitPaneNode( windowNode.getChild("mainsplitpane") );
		item.setRightSplitPaneNode( windowNode.getChild("headersplitpane") );
		
		item.setSelectedHeaderNode( windowNode.getChild("selectedheader") );
		item.setHeaderAscendingNode( windowNode.getChild("headerascending") );
		*/

		//return item;

	}

	public SpellcheckItem getSpellcheckItem()
	{
		AdapterNode spellcheck = getRootNode().getChild("spellcheck");
		if ( spellcheck == null ) 
		{
			// create spellcheck root node
			Element e = createElementNode("spellcheck");
			
			spellcheck = new AdapterNode( e );
			getRootNode().add( spellcheck );
			
		}
		
		SpellcheckItem item = new SpellcheckItem(spellcheck, getDocument());

		return item;
	}

}