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
