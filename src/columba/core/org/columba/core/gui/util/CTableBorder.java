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
package org.columba.core.gui.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.border.BevelBorder;

public class CTableBorder extends BevelBorder
{
    boolean b;
	public CTableBorder(boolean b)
	{
	    super(0);
	    this.b = b;
	}
	
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
	    if ( b )
		{
		    Graphics2D g2 = (Graphics2D) g;
		    
		    g2.setColor( getHighlightOuterColor(c) );
		    g2.drawLine(0,0,c.getWidth()-1, 0 );
		    g2.drawLine(0,0,0, c.getHeight()-1 );
		    g2.setColor( getShadowOuterColor(c) );
		    g2.drawLine(0,c.getHeight()-1,c.getWidth()-1, c.getHeight()-1 );
		    g2.drawLine(c.getWidth()-1,0,c.getWidth()-1, c.getHeight()-1 );
		}
	    else
		{
		    Graphics2D g2 = (Graphics2D) g;

		    g2.setColor( getShadowOuterColor(c) );
		    g2.drawLine(0,0,c.getWidth()-1, 0 );
		    g2.drawLine(0,0,0, c.getHeight()-1 );
		    g2.setColor( getHighlightOuterColor(c));
		    g2.drawLine(0,c.getHeight()-1,c.getWidth()-1, c.getHeight()-1 );
		    g2.drawLine(c.getWidth()-1,0,c.getWidth()-1, c.getHeight()-1 );
		}

	}
}
