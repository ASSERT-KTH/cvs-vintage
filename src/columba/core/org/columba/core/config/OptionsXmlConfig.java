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
package org.columba.core.config;

import java.io.File;

import org.columba.core.xml.XmlElement;
import org.columba.mail.config.PGPItem;

public class OptionsXmlConfig extends DefaultXmlConfig
{
    //private File file;

	protected ThemeItem themeItem;
	GuiItem guiItem;

    public OptionsXmlConfig( File file )
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

	public GuiItem getGuiItem()
		{
			if ( guiItem == null )
			{
				guiItem = new GuiItem(getRoot().getElement("/options/gui"));
			}

			return guiItem;
		}

    public ThemeItem getThemeItem()
    {
        if ( themeItem == null )
        {
        	themeItem = new ThemeItem( getRoot().getElement("/options/gui/theme") );
        }
        
        
		/*
        AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");
		
	ThemeItem item = new ThemeItem( getDocument(), guiNode );

	

        return item;
		*/
		
		return themeItem;
    }

    public PGPItem getPGPItem()
    {
    	/*
        AdapterNode rootNode = getRootNode();
        AdapterNode pgpNode = rootNode.getChild("pgp");

        PGPItem item = new PGPItem( getDocument(), pgpNode );

        return item;
        */
        
        return null;
    }


    public XmlElement getMimeTypeNode()
    {   
    	XmlElement mimeTypes = getRoot().getElement("/options/mimetypes");
    	if( mimeTypes == null ) {
    		getRoot().getElement("options").addElement(new XmlElement("mimetypes"));
			mimeTypes = getRoot().getElement("/options/mimetypes");
    	}
    	
		return mimeTypes;
    }


	/*
    public int getIntegerGuiOptions( String name, int defaultValue )
    {
        AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");

        AdapterNode node = guiNode.getChild( name );
        if ( node == null )
        {
            // node does not exist => we have to create a new one

            AdapterNode parent = guiNode;

            org.w3c.dom.Element treePathNode = createTextElementNode( name , new Integer(defaultValue).toString() );

            parent.domNode.appendChild( treePathNode );

            node = guiNode.getChild( name );
        }

        Integer i = new Integer( node.getValue() );

        return i.intValue();
    }

    public String getStringGuiOptions( String name, String defaultValue )
    {
        AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");

        AdapterNode node = guiNode.getChild( name );
        if ( node == null )
        {
            // node does not exist => we have to create a new one

            AdapterNode parent = guiNode;

            org.w3c.dom.Element treePathNode = createTextElementNode( name , defaultValue );

            parent.domNode.appendChild( treePathNode );

            node = guiNode.getChild( name );
        }

        String str = node.getValue();

        return str;
    }

    public int getIntegerOptions( String name, int defaultValue )
    {
        AdapterNode rootNode = getRootNode();

        AdapterNode node = rootNode.getChild( name );
        if ( node == null )
        {
            // node does not exist => we have to create a new one

            AdapterNode parent = rootNode;

            org.w3c.dom.Element treePathNode = createTextElementNode( name , new Integer(defaultValue).toString() );

            parent.domNode.appendChild( treePathNode );

            node = rootNode.getChild( name );
        }

        Integer i = new Integer( node.getValue() );

        return i.intValue();
    }

    public String getStringOptions( String name, String defaultValue )
    {
        AdapterNode rootNode = getRootNode();

        AdapterNode node = rootNode.getChild( name );
        if ( node == null )
        {
            // node does not exist => we have to create a new one

            AdapterNode parent = rootNode;

            org.w3c.dom.Element treePathNode = createTextElementNode( name , defaultValue );

            parent.domNode.appendChild( treePathNode );

            node = rootNode.getChild( name );
        }

        String str = node.getValue();

        return str;
    }

	public void setStringGuiOption( String name, String value )
	{
		 AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");

        AdapterNode node = guiNode.getChild( name );
        if ( node != null )
        {
        	node.setValue(value);
        }
	}
	*/
}




