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
