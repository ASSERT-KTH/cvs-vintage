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
//
package org.columba.core.print;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;

import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.View;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;


/**
 * Class for representing a HTML print object. Objects of this
 * type is intended for inclusion in cDocument objects for printing.
 * Division into multiple pages represented by cPage is supported. 
 * 
 * @author Karl Peder Olesen (karlpeder), 20030601
 * 
 */
public class cHTMLPart extends cPrintObject {
	/** Container holding the HTML to be printed (used to control layout etc. */
	private JTextPane mPane = null;
	/** Y-coordinate in mPane to start printing at */
	private cUnit mStartY = new cCmUnit(0.0);

    /**
     * Creates a new empty HTML print object.
     */
    public cHTMLPart() {
        super();
    }

	/**
	 * Sets the HTML document to be printed.
	 * @param	html	HTML document to be printed
	 */
	public void setHTML(HTMLDocument html) {
		mPane = new JTextPane();
		mPane.setDoubleBuffered(false);
		mPane.setContentType("text/html");
		mPane.setDocument(html);	// "store" html in jTextPane container
		mStartY = new cCmUnit(0.0);	// reset starting position in y-direction
	}
	
	/**
	 * Sets the HTML document to be printed.
	 * Precondition: The URL given contains a HTML document
	 * @param 	url		url to file with HTML document
	 * @throws	IOException if errors occur while reading HTML document from file
	 */
	public void setHTML(URL url) throws IOException
	{
		/*
		 * By using an instance of SyncHTMLEditorKit, the html should load
		 * synchroniously - so everything is loaded before printing starts
		 */

		mPane = new JTextPane();
		mPane.setDoubleBuffered(false);
		mPane.setEditorKit(new SyncHTMLEditorKit());
		mPane.setContentType("text/html");
		mPane.setPage(url);
		mStartY = new cCmUnit(0.0);	// reset starting position in y-direction
	}
	
	/** 
	 * Sets the starting position in y-direction. A starting position != 0 is used
	 * when printing anything but the first page. This bookkeeping is usually done
	 * internally when creating pagebreaks using the method breakBlock.
	 * Therefore this method has not been made public. 
	 * @param	pos		Starting position in y-direction
	 */
	protected void setStartY(cUnit y) {
		mStartY = new cCmUnit(y);
	}

    /**
     * Prints the contents of this HTML print object using the supplied 
     * Graphics2D object.
     * @param	Used for rendering (i.e. printing) this HTML print object
     * @see org.columba.core.print.cPrintObject#print(java.awt.Graphics2D)
     */
    public void print(Graphics2D g) {
		computePositionAndSize();
		
		// get origin / size information (height as "total" height minus current pos.)
		cPoint origin = getDrawingOrigin();
		double width  = getDrawingSize().getWidth().getPoints();
		double height = 
				getPage().getPrintableAreaSize().getHeight().sub(
						getLocation().getY()).getPoints();

		/*
		 * TODO: Guess that right thing to do is to get height as getDrawingSize().getHeight(),
		 * since this should take top- and bottom margin of this print
		 * object into account. But the height seems not to be set 
		 * correctly in computePositionAndSize() (*20030604, karlpeder*)
		 */
		
		// set size of mPane according to the available width
		
		mPane.setSize((int) width, Integer.MAX_VALUE);
		mPane.validate();

		// set clipping for the graphics object
		Shape oldClip = g.getClip();
		g.setClip((int) origin.getX().getPoints(), 
				  (int) origin.getY().getPoints(),
				  (int) width, (int) height);

		// translate g to line up with origin of print area (trans 1)
		Point2D.Double trans = new Point2D.Double(origin.getX().getPoints(),
				origin.getY().getPoints() - mStartY.getPoints());
		g.translate(trans.getX(), trans.getY());
				
		// paint the jTextPane container, i.e. print the contents
		mPane.paint(g);
		
		// translate graphics object back to original position and reset clip
		g.translate(-trans.getX(), -trans.getY());
		g.setClip(oldClip);
	}


    /**
     * Returns the size of this HTML print object subject to the
     * given width.<br>
     * NB: The height returned will always be from the starting point 
     * (which could be different from the top) to the end of the current 
     * content, independent on whether everything will or can be printed
     * on onto one page.
     * 
     * @param	maxWidth		Max. allowable width this print object can occupy
     * 
     * @see org.columba.core.print.cPrintObject#getSize(org.columba.core.print.cUnit)
     */
    public cSize getSize(cUnit maxWidth) {
		// resize jTextPane component to calculate height and get it
		double width = maxWidth.sub(leftMargin).sub(rightMargin).getPoints();
		mPane.setSize((int) width, Integer.MAX_VALUE);
		mPane.validate();
		double height = mPane.getPreferredSize().getHeight();
		// correct for starting position if printing should not start at the top
		height = height - mStartY.getPoints();
		
		// calculate size and return it
		cUnit w = new cCmUnit(maxWidth);	// width unchanged
		cUnit h = new cCmUnit();
		h.setPoints(height);				// height of content
		h.addI(topMargin);					// + top margin
		h.addI(bottomMargin);				// + bottom margin
		
		return new cSize(w, h);
    }


    /**
     * Divides (breaks) this HTML print object into a remainder (which fits
     * inside the given max height) and the rest. The remainder is returned and
     * "the rest" is stored by modifying this object.
     * 
     * @param	w			Max. allowable width this print object can occupy
     * @param	maxHeight	Max. allowable height before breaking
     * @return	The part of the print object, which fits inside the given max height
     * 
     * @see org.columba.core.print.cPrintObject#breakBlock(org.columba.core.print.cUnit, org.columba.core.print.cUnit)
     */
    public cPrintObject breakBlock(cUnit w, cUnit maxHeight) {
    	
		// get size of content
		cSize contentSize = this.getSize(w);
		int width  = (int) contentSize.getWidth().getPoints();
		int height = (int) contentSize.getHeight().getPoints();
		int startY = (int) mStartY.getPoints();
		
		// define allocation rectangle (startY is used to compensate for
		// different start point if printing shall not start from the top)
		Rectangle allocation = new Rectangle(0, -startY, width, height + startY);

		// set initial value for height where this print object should be broken
    	double breakHeight = maxHeight.getPoints();	// in points
		
		/*
		 * calculate a new break height according to the contents, possibly
		 * smaller to break before some content (i.e. not to break in the
		 * middle of something
		 */
		View rootView = mPane.getUI().getRootView(mPane);
		breakHeight = calcBreakHeightFromView(rootView, allocation, breakHeight);
		
		// create remainder
		cHTMLPart remainder = new cHTMLPart();
		remainder.setHTML((HTMLDocument) mPane.getDocument());
		remainder.setStartY(mStartY);
		// modify "this" to start where remainder ends
		cUnit newStartY = new cCmUnit();
		if (breakHeight < height) {
			newStartY.setPoints(mStartY.getPoints() + breakHeight);
		}
		else {	// this happends if there's nothing left for the next page
			newStartY = mStartY.add(contentSize.getHeight());
		}
		this.setStartY(newStartY);
		
		return remainder;    	 	
    }

	/**
	 * Private utility to calculate break height based on the contents
	 * of a view. If the break height calculated is not smaller than the
	 * actual break height, actBreakHeight is returned unchanged.
	 * @param	view			The View object to operate on
	 * @param	allocation		Allocation for the view (where to render)
	 * @param	actBreakHeight 	Actual break height
	 */
	private double calcBreakHeightFromView(View view, 
										   Shape allocation,
										   double actBreakHeight) {
		if (view.getViewCount() > 0) {
			// child views exist - operate recursively on these
			double breakHeight = actBreakHeight;
			Shape childAllocation;
			View childView;
			for (int i = 0; i < view.getViewCount(); i++) {
				childAllocation = view.getChildAllocation(i,allocation);
				if (childAllocation != null) {
			  		childView = view.getView(i);
			  		// calculate break height for child, and use updated
			  		// value in the further processing
			  		breakHeight = calcBreakHeightFromView(
			  				childView, childAllocation, breakHeight);
				}
			}
			return breakHeight;	// return (possibly) updated value
		}
		else {
			// no childs - we have a leaf view (i.e. with contents)

			double allocY      = allocation.getBounds().getY();
			double allocMaxY   = allocation.getBounds().getMaxY();
			double allocHeight = allocation.getBounds().getHeight();
			if ((allocY >= 0) && (allocY < actBreakHeight) && 
					(allocMaxY > actBreakHeight)) {
				// view starts on page and exceeds it

				/*
				 * If the height of a view exceeds the paperheight, there should
				 * be no break before (since it will be impossible to fit it in
				 * anywhere => an infinite loop). We don't have access to the 
				 * pageheight here, therefore an "educated guess" is made:
				 * No breaks are inserted before views starting within the first
				 * 1% (chosen to avoid round-off errors) of the available space
				 * given by actBreakHeight. If the view starts after the first 1%,
				 * a break is inserted and the view will start at the top of the
				 * next page (i.e. withing the first 1% this time).
				 */
				
				if (allocY < (actBreakHeight * 0.01)) {
					return actBreakHeight;	// unchanged, i.e. no breaks before this view
				}
				else {
					// view can be broken 
					if (allocY < actBreakHeight)
						return allocY;			// break before start of view
					else
						return actBreakHeight;	// unchanged
				}
			}
			else {
				return actBreakHeight;			// unchanged
			}
		}
	}
	
}

/**
 * Utility class used for loading html synchroniously into a jTextPane
 * @author	Karl Peder Olesen (karlpeder), 20030604 
 */
class SyncHTMLEditorKit extends HTMLEditorKit
{

    /**
     * Create an uninitialized text storage model that is appropriate for
     * this type of editor.<br>
     * The document returned will load synchroniously.
     * 
     * @see javax.swing.text.EditorKit#createDefaultDocument()
     */
    public Document createDefaultDocument()
    {
		Document doc = super.createDefaultDocument();
		((HTMLDocument)doc).setAsynchronousLoadPriority(-1);
		return doc;
    }

}