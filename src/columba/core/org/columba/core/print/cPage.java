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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.Vector;

public class cPage implements Printable {

	public static final int PORTRAIT = 1;
	public static final int LANDSCAPE = 2;

	private cUnit leftMargin;
	private cUnit rightMargin;
	private cUnit topMargin;
	private cUnit bottomMargin;
	private cUnit gutter;

	private int orientation;

	private Vector pageObjects;

	private cSize pageSize;

	private cDocument document;

	public cPage(cDocument d) {
		pageObjects = new Vector();

		leftMargin = new cCmUnit();
		rightMargin = new cCmUnit();
		topMargin = new cCmUnit();
		bottomMargin = new cCmUnit();
		gutter = new cCmUnit();

		document = d;
	}
	
	public int countObjects() {
		return pageObjects.size();
	}

	public int print(Graphics g, PageFormat pf, int pi) {

		Graphics2D g2d = (Graphics2D) g;
		Paper paper = pf.getPaper();

		leftMargin.setPoints(paper.getImageableX());
		rightMargin.setPoints(
			paper.getWidth() - (paper.getImageableX() + paper.getImageableWidth()));
		bottomMargin.setPoints(
			paper.getHeight() - (paper.getImageableY() + paper.getImageableHeight()));
		topMargin.setPoints(paper.getImageableY());

		cPointUnit width = new cPointUnit(paper.getImageableWidth());
		cPointUnit height = new cPointUnit(paper.getImageableHeight());

		pageSize = new cSize(width, height);

		cPrintObject header = document.getHeader();

		if (header != null) {
			header.setPage( this );
			header.print(g2d);
		}

		for (int i = 0; i < pageObjects.size(); i++) {
			((cPrintObject) pageObjects.get(i)).print(g2d);
		}

		cPrintObject footer = document.getFooter();

		if (footer != null) {
			footer.setPage( this );
			footer.print(g2d);
		}

		return PAGE_EXISTS;
	}

	public cDocument getDocument() {
		return document;
	}

	public void setDocument(cDocument d) {
		document = d;
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

	public void setGutter(cUnit m) {
		gutter = m;
	}

	public void setOrientation(int o) {
		orientation = o;
	}

	public void add(cPrintObject po) {
		po.setPage( this );
		pageObjects.add(po);
	}

	public cPoint getPrintableAreaOrigin() {
		cPoint origin;
		cUnit headerMargin  = new cCmUnit();
		
		cPrintObject header = document.getHeader();
		if( header!= null ) {
			headerMargin = header.getSize(pageSize.getWidth()).getHeight();
		}
		
		origin = new cPoint(leftMargin.add(gutter), topMargin.add( headerMargin ));

		return origin;
	}

	public cSize getPrintableAreaSize() {
		cUnit headerMargin  = new cCmUnit();
		
		cPrintObject header = document.getHeader();
		if( header!= null ) {
			headerMargin.addI( header.getSize(pageSize.getWidth()).getHeight() );
		}

		cPrintObject footer = document.getFooter();
		if( footer!= null ) {
			headerMargin.addI( footer.getSize(pageSize.getWidth()).getHeight() );
		}
		
		return pageSize.subHeight( headerMargin );		
	}

}
