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
package org.columba.core.print;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

public class cSize implements Cloneable {
	
	private cUnit width;
	private cUnit height;

	public 	cSize() {
	}
	
	public 	cSize( cUnit x, cUnit y ) {
		this.width = x;
		this.height = y;
	}
	
	public void setSize( cUnit x, cUnit y ) {
		this.width = x;
		this.height = y;
	}

	public void setWidth( cUnit x ) {
		this.width = x;
	}
	
	public void setHeight( cUnit y ) {
		this.height = y;
	}
	
	public cUnit getWidth() {
		return width;
	}	
	
	public cUnit getHeight() {
		return height;
	}
	
	public Dimension2D getDimension2D() {
		Dimension temp = new Dimension( (int) width.getPoints(), (int) height.getPoints() );
		
		return temp;
	}

	public Dimension getDimension() {
		Dimension temp = new Dimension( (int) width.getPoints(), (int) height.getPoints() );
		
		return temp;
	}

	public cSize subHeight( cUnit h ) {
		return new cSize( getWidth(), getHeight().sub( h )  );			
	}

	public Object clone() {
		cSize clone = new cSize(width,height);
		
		return clone;	
	}
	
}
