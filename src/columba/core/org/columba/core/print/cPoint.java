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

import java.awt.Point;
import java.awt.geom.Point2D;

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
