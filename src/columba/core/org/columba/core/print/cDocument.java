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

import java.awt.print.Book;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Enumeration;
import java.util.Vector;

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