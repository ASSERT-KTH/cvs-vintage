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