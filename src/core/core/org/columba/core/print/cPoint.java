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

import java.awt.geom.*;
import java.awt.*;

public class cPoint {
	
	private cUnit x;
	private cUnit y;
	
	public 	cPoint( cUnit x, cUnit y ) {
		this.x = x;
		this.y = y;
	}
	
	public void setLocation( cUnit x, cUnit y ) {
		this.x = x;
		this.y = y;
	}

	public void setX( cUnit x ) {
		this.x = x;
	}
	
	public void setY( cUnit y ) {
		this.y = y;
	}
	
	public cUnit getX() {
		return x;
	}	
	
	public cUnit getY() {
		return y;
	}
	
	public Point2D.Double getPoint2D() {
		Point2D.Double temp = new Point2D.Double( x.getPoints(), y.getPoints() );
		
		return temp;
	}
	
	public Point getPoint() {
		Point temp = new Point(  (int) x.getPoints(), (int) y.getPoints() );
		
		return temp;
	}
	
	public cPoint add( cPoint p ) {
		cPoint temp = new cPoint( p.getX().add( getX() ),
								  p.getY().add( getY() ));
		
		return temp;
	}

	public cPoint subHeight( cUnit h ) {
		cPoint temp = new cPoint( getX() ,
								  getY().sub( h ) );
		
		return temp;
	}

	public cPoint addHeight( cUnit h ) {
		cPoint temp = new cPoint( getX() ,
								  getY().add( h ) );
		
		return temp;
	}
	
	
	public Object clone() {
		cPoint clone = new cPoint(getX(), getY());
		
		return clone;	
	}
}
