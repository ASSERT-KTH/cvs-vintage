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

import java.awt.print.*;
import java.awt.*;
import java.util.*;

public class cDocument{

	private Vector objects;
	private Vector pages;

	private cPrintObject header;
	private cPrintObject footer;

	private String docName;

	private boolean uptodate;

	private PrinterJob printJob;

	public cDocument() {
		objects = new Vector();
		pages = new Vector();
		uptodate = false;
		
		printJob = PrinterJob.getPrinterJob();
	}

	public int getPageCount() {
		if (!uptodate)
			createPages();
		return pages.size();
	}

	public void print() {
		if (!uptodate)
			createPages();
		print(1, getPageCount());
	}

	public void print(int startPage, int endPage) {
		if (!uptodate)
			createPages();

		if (docName != null)
			printJob.setJobName(docName);

		Book book = new Book();

		for (int i = 0; i < endPage; i++) {
			book.append((cPage) pages.get(i), printJob.defaultPage());
		}

		printJob.setPageable(book);

		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (PrinterException e) {
				e.printStackTrace();
			}
		}
	}

	public void setHeader(cPrintObject h) {
		header = h;
		h.setType( cPrintObject.HEADER );
	}

	public cPrintObject getHeader() {
		return header;
	}

	public cPrintObject getFooter() {
		return footer;
	}

	public void setFooter(cPrintObject f) {
		footer = f;
		f.setType( cPrintObject.FOOTER );
	}

	public void setDocumentName(String n) {
		docName = n;
	}

	public int getPageNr(cPage p) {
		if (!uptodate)
			createPages();

		return pages.indexOf(p) + 1;
	}

	public void appendPrintObject(cPrintObject obj) {
		objects.add(obj);
		uptodate = false;
	}

	private void createPages() {
		pages.clear();

		Enumeration objEnum = objects.elements();

		Paper paper = printJob.defaultPage().getPaper();

		cCmUnit pWidth = new cCmUnit();
		pWidth.setPoints(paper.getImageableWidth());

		cCmUnit pHeight = new cCmUnit();
		pHeight.setPoints(paper.getImageableHeight());

		if( getHeader() != null ) {
			pHeight.subI(getHeader().getSize(pWidth).getHeight());
		}
		
		if( getFooter() != null ) {
			pHeight.subI(getFooter().getSize(pWidth).getHeight());
		}

		cUnit remainHeight;
		cPrintObject remainObj;

		cPrintObject nextObj;
		cUnit objHeight;

		cPage nPage = new cPage(this);
		remainHeight = new cPointUnit(pHeight.getPoints() );
		
		cUnit hLocation = new cCmUnit();
		
		nextObj = (cPrintObject) objEnum.nextElement();

		while(true) {
			objHeight = nextObj.getSize(pWidth).getHeight();

			if (objHeight.getPoints() <= remainHeight.getPoints()) {
				nextObj.setLocation(new cPoint( new cCmUnit(), new cCmUnit( hLocation)));
				remainHeight.setPoints( remainHeight.sub(objHeight).getPoints() );
				hLocation.setPoints( hLocation.add( objHeight ).getPoints() );
				nPage.add(nextObj);
				if( objEnum.hasMoreElements() )
					nextObj = (cPrintObject) objEnum.nextElement();
				else
					break;
			} else {
				remainObj = nextObj.breakBlock(pWidth, remainHeight);
				if (remainObj != null) {
					remainObj.setLocation(new cPoint( new cCmUnit(), new cCmUnit( hLocation)));
					nPage.add(remainObj);
				}
				pages.add(nPage);
				nPage = new cPage(this);
				remainHeight.setPoints( pHeight.getPoints() );
				hLocation.setUnits( 0 );
			}

		}

		if( nPage.countObjects() != 0 ) pages.add(nPage);

		uptodate = true;
	}

}