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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

public class cLine extends cPrintObject {

	private double thickness;

	public cLine() {
		super();
		
		thickness = 1.0;
		color = Color.black; 
	}

	public void setThickness(double t) {
		thickness = t;
	}

	public double getThickness() {
		return thickness;
	}

	public void print(Graphics2D g) {
		
		computePositionAndSize();
		
		double x1 = getDrawingOrigin().getX().getPoints();
		double x2 = x1 + getDrawingSize().getWidth().getPoints();
		
		Line2D.Double line =
			new Line2D.Double(
				x1,
				getDrawingOrigin().getY().getPoints(),
				x2,
				getDrawingOrigin().getY().getPoints());

		Stroke lineStroke = new BasicStroke((float) thickness);
		g.setStroke(lineStroke);

		Color saveForeground = g.getColor();

		g.setColor(color);
		g.draw(line);
		g.setColor(saveForeground);
	}

	public cSize getSize( cUnit width ) {
		
		cUnit height = new cPointUnit(thickness );
		height.addI( topMargin );
		height.addI( bottomMargin );
		
		return new cSize( width, height);		
	}
	


}