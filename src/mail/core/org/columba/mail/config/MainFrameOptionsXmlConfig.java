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

package org.columba.mail.config;


import java.io.File;

import org.columba.addressbook.config.AdapterNode;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.config.GuiItem;
import org.columba.core.config.TableItem;
import org.columba.core.config.ViewItem;
import org.columba.core.config.WindowItem;


public class MainFrameOptionsXmlConfig extends DefaultXmlConfig
{
   // private File file;

	WindowItem windowItem;
	GuiItem guiItem;
	TableItem headerTableItem;
	ViewItem viewItem;
	
    public MainFrameOptionsXmlConfig( File file )
    {
        super( file );
    }

	/*
    public AdapterNode getRootNode()
    {
        AdapterNode node = new AdapterNode( getDocument() );

        AdapterNode rootNode = node.getChild( 0 );
        return rootNode;
    }
	*/
	
    public TableItem getTableItem()
    {
    	if ( headerTableItem ==  null )
    	{
    		headerTableItem = new TableItem(getRoot().getElement("/options/gui/table"));
    	}
    	
    	return headerTableItem;
    	
    	/*
	HeaderTableItem headerTableItem = new HeaderTableItem();

        AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");


        AdapterNode headerTableItemNode = guiNode.getChild("headertable");


        for ( int i=0; i< headerTableItemNode.getChildCount(); i++)
        {
            AdapterNode parent = headerTableItemNode.getChild(i);

            headerTableItem.addHeaderItem( parent.getChild(0),
                                           parent.getChild(3),
                                           parent.getChild(1),
                                           parent.getChild(2)
                                           );
        }

	return headerTableItem;
	*/
	
	
    }

    public TableItem getPop3HeaderTableItem()
    {
    	/*
	HeaderTableItem headerTableItem = new HeaderTableItem();

        AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");


        AdapterNode headerTableItemNode = guiNode.getChild("pop3headertable");


        for ( int i=0; i< headerTableItemNode.getChildCount(); i++)
        {
            AdapterNode parent = headerTableItemNode.getChild(i);

            headerTableItem.addHeaderItem( parent.getChild(0),
                                           parent.getChild(3),
                                           parent.getChild(1),
                                           parent.getChild(2)
                                           );
        }

	return headerTableItem;
	*/
	
	return null;
    }
    
    public ViewItem getViewItem()
    {
    	if ( viewItem == null )
    	{
    		viewItem = new ViewItem( getRoot().getElement("/options/gui/viewlist/view") );
    	}
    	
    	return viewItem;
    }

	public GuiItem getGuiItem()
	{
		if ( guiItem == null )
		{
			guiItem = new GuiItem(getRoot().getElement("/options/gui"));
		}
		
		return guiItem;
	}
	
    public WindowItem getWindowItem()
    {
    	if ( windowItem == null )
    	{
    		
    		windowItem = new WindowItem(getRoot().getElement("/options/gui/viewlist/view/window"));
    		
    	}
       /*

        AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");
        AdapterNode windowNode = guiNode.getChild("window");

      



        return new WindowItem( getDocument(), windowNode );
        */
        
        return windowItem;

    }

    public AdapterNode getMimeTypeNode()
    {
    	/*
        AdapterNode rootNode = getRootNode();
        AdapterNode node = rootNode.getChild("mimetypes");

        return node;
        */
        
        return null;
    }



}




