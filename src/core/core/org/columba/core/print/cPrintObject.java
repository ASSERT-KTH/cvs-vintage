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

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class cPrintObject {

	public static final int SOUTHWEST = 1;
	public static final int SOUTH = 2;
	public static final int SOUTHEAST = 3;
	public static final int WEST = 4;
	public static final int CENTER = 5;
	public static final int EAST = 6;
	public static final int NORTHWEST = 7;
	public static final int NORTH = 8;
	public static final int NORTHEAST = 9;
	public static final int NONE = 0;

	public static final int HORIZONTAL = 1;
	public static final int VERTICAL = 2;
	public static final int BOTH = 3;

	public static final int NORMAL = 1;
	public static final int HEADER = 2;
	public static final int FOOTER = 3;
	public static final int GROUPMEMBER = 4;


	private int orientation;
	private int sizePolicy;
	private int type;

	private cPoint location;
	private cSize size;

	protected cUnit leftMargin;
	protected cUnit rightMargin;
	protected cUnit topMargin;
	protected cUnit bottomMargin;

	private cPoint drawingOrigin;
	private cSize drawingSize;

	protected Color color;

	private boolean relative;

	protected cPage page;
	public cPrintObject() {
		relative = true;

		leftMargin = new cCmUnit();
		rightMargin = new cCmUnit();
		topMargin = new cCmUnit();
		bottomMargin = new cCmUnit();

		drawingOrigin = new cPoint(new cCmUnit(), new cCmUnit());
		drawingSize = new cSize(new cCmUnit(), new cCmUnit());

		location = new cPoint(new cCmUnit(), new cCmUnit());
		size = new cSize(new cCmUnit(), new cCmUnit());

		orientation = NORTHWEST;
		sizePolicy = HORIZONTAL;
		type = NORMAL;

		color = Color.black;
	}


	public void setPage(cPage p) {
		page = p;
	}


	public abstract void print(Graphics2D g);

	public void setLocation(cPoint l) {
		location = l;
	}

	public cPoint getLocation() {
		return location;
	}

	public void setSize(cSize s) {
		size = s;
	}

	public void setOrientation(int o) {
		orientation = o;
	}

	public void setSizePolicy(int sp) {
		sizePolicy = sp;
	}


	public void setLeftMargin(cUnit m) {
		leftMargin = m;
	}

	public void setRightMargin(cUnit m) {
		rightMargin = m;
	}

	public void setTopMargin(cUnit m) {
		topMargin = m;
	}

	public void setBottomMargin(cUnit m) {
		bottomMargin = m;
	}

	public cPoint getDrawingOrigin() {
		return drawingOrigin;
	}

	public cSize getDrawingSize() {
		return drawingSize;
	}

	public cSize getSize() {
		return size;
	}

	public void setRelativeMode(boolean r) {
		relative = r;
	}

	public void setColor(Color c) {
		color = c;
	}

	public Color getColor() {
		return color;
	}

	public void computePositionAndSize() {
		cPoint parentLocation;
		cSize parentSize;

		parentLocation = page.getPrintableAreaOrigin();
		parentSize = page.getPrintableAreaSize();


		switch( type ) {

			case NORMAL : {
				drawingOrigin = location.add(parentLocation);
				break;
			}

			case HEADER : {
				drawingOrigin = parentLocation.subHeight( getSize( parentSize.getWidth() ).getHeight() );
				break;
			}

			case FOOTER : {
				drawingOrigin = parentLocation.addHeight( parentSize.getHeight() );
				break;
			}

			case GROUPMEMBER : {
				drawingOrigin = location;
				break;
			}

		}


		// SizePolicy

		if ((sizePolicy == HORIZONTAL) || (sizePolicy == BOTH)) {
			drawingSize = new cSize(parentSize.getWidth(), drawingSize.getHeight());
		}

		if ((sizePolicy == VERTICAL) || (sizePolicy == BOTH)) {
			drawingSize = new cSize(drawingSize.getWidth(), parentSize.getHeight());
		}

		// Margins

		drawingOrigin.setX(drawingOrigin.getX().add(leftMargin));
		drawingOrigin.setY(drawingOrigin.getY().add(topMargin));
		drawingSize.setWidth(drawingSize.getWidth().sub(leftMargin).sub(rightMargin));
		drawingSize.setHeight(drawingSize.getHeight().sub(topMargin).sub(bottomMargin));

		// Orientation

		if ((orientation == SOUTHWEST)
			|| (orientation == WEST)
			|| (orientation == NORTHWEST)) {
			drawingOrigin = new cPoint(drawingOrigin.getX(), drawingOrigin.getY());
		}

		if ((orientation == SOUTHEAST)
			|| (orientation == EAST)
			|| (orientation == NORTHEAST)) {
			cUnit objectPosition = parentSize.getWidth().sub(this.getSize().getWidth());
			drawingOrigin =
				new cPoint(drawingOrigin.getX().add(objectPosition), drawingOrigin.getY());
		}

		if ((orientation == NORTH)
			|| (orientation == CENTER)
			|| (orientation == SOUTH)) {
			cUnit parentCenter = parentSize.getWidth().div(2.0);
			cUnit childCenter = getSize().getWidth().div(2.0);
			cUnit objectPosition = parentCenter.sub(childCenter);
			drawingOrigin =
				new cPoint(drawingOrigin.getX().add(objectPosition), drawingOrigin.getY());
		}

		if ((orientation == SOUTHWEST)
			|| (orientation == SOUTH)
			|| (orientation == SOUTHEAST)) {
			cUnit objectPosition = parentSize.getHeight().sub(getSize().getHeight());

			drawingOrigin =
				new cPoint(drawingOrigin.getX(), objectPosition.add(drawingOrigin.getY()));
		}

		if ((orientation == WEST)
			|| (orientation == CENTER)
			|| (orientation == EAST)) {
			cUnit parentCenter = parentSize.getHeight().div(2.0);
			cUnit childCenter = getSize().getHeight().div(2.0);
			cUnit objectPosition = parentCenter.sub(childCenter);
			drawingOrigin =
				new cPoint(drawingOrigin.getX(), drawingOrigin.getY().add(objectPosition));
		}

		if ((orientation == SOUTHWEST)
			|| (orientation == SOUTH)
			|| (orientation == SOUTHEAST)) {
			cUnit objectPosition = parentSize.getHeight().sub(getSize().getHeight());

			drawingOrigin =
				new cPoint(
					drawingOrigin.getX(),
					parentLocation.getY().add(location.getY()).add(size.getHeight()));
		}

	}

	public abstract cSize getSize(cUnit maxWidth);


	public cPrintObject breakBlock(cUnit w, cUnit maxHeight) {
		return null;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
