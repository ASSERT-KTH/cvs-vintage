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
