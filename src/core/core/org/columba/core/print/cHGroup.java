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

import java.awt.Graphics2D;
import java.util.Vector;

public class cHGroup extends cPrintObject {

	Vector members;
	
	public cHGroup() {
		members = new Vector();	
	}

	public void add( cPrintObject po ) {
		po.setType( cPrintObject.GROUPMEMBER );
		members.add( po );
	}

	public void print(Graphics2D g) {
		cPrintObject act; 
		
		computePositionAndSize();
		
		for( int i=0; i<members.size(); i++ ) {
			act = (cPrintObject) members.get( i );
			act.setLocation( (cPoint) getDrawingOrigin().clone() );
			act.setPage(page);
			act.print( g );	
		}
	}

	public cSize getSize(cUnit width) {
		cUnit maxHeight = new cCmUnit();
		cSize act;
		
		for( int i=0; i<members.size(); i++ ) {
			act = ((cPrintObject) members.get( i )).getSize( width );	
			
			if( act.getHeight().getPoints() > maxHeight.getPoints() ) {
				maxHeight = act.getHeight();
			}
			
			
		}

		maxHeight.addI( topMargin  );
		maxHeight.addI( bottomMargin );
		
		return new cSize( new cCmUnit(), maxHeight );
	}

}
