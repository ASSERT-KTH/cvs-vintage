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

public class cCmUnit extends cUnit {
	
	private static final double POINTS_PER_CM = 28.8;

	public cCmUnit() {
		setUnits( 0.0 );	
	}

	public cCmUnit( cUnit u ) {
		setPoints( u.getPoints() );	
	}

	public cCmUnit( double u ) {
		setUnits( u );	
	}
	
	public void setPoints( double p ) {
		setUnits( p / POINTS_PER_CM	);
	}
	
	public double getPoints() {
		return getUnits() * POINTS_PER_CM;		
	}	
	
}
