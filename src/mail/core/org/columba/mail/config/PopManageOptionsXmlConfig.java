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
import org.columba.core.config.HeaderTableItem;
import org.columba.core.config.WindowItem;


public class PopManageOptionsXmlConfig extends DefaultXmlConfig
{
    private File file;


    public PopManageOptionsXmlConfig( File file )
    {
        super( file );
    }

    public AdapterNode getRootNode()
    {
        AdapterNode node = new AdapterNode( getDocument() );

        AdapterNode rootNode = node.getChild( 0 );
        return rootNode;
    }

    public HeaderTableItem getHeaderTableItem()
    {
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
    }



    public WindowItem getWindowItem()
    {
       

        AdapterNode rootNode = getRootNode();
        AdapterNode guiNode = rootNode.getChild("gui");
        AdapterNode windowNode = guiNode.getChild("window");

     

        return  new WindowItem( getDocument(), windowNode );

    }




}




