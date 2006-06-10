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
import java.util.logging.Logger;

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

    private static final Logger LOG = Logger.getLogger("org.columba.core.print");

    /** IContainer holding the HTML to be printed (used to control layout etc. */
    private JTextPane mPane = null;

    /** Y-coordinate in mPane to start printing at */
    private cUnit mStartY = new cCmUnit(0.0);

    /** Flag indicating whether scaling of the print is allowed (default is no) */
    private boolean mScaleAllowed;

    /**
     * Creates a new empty HTML print object.
     * As default, scaling the print to fit is not allowed
     */
    public cHTMLPart() {
        this(false);
    }

    /**
     * Creates a new empty HTML print object and sets whether scaling is allowed
     * @param scaleAllowed        If true, the print is allowed to "scale to fit"
     */
    public cHTMLPart(boolean scaleAllowed) {
        super();
        mScaleAllowed = scaleAllowed;
    }

    /**
     * Sets the HTML document to be printed.
     * @param        html        HTML document to be printed
     */
    public void setHTML(HTMLDocument html) {
        mPane = new JTextPane();
        mPane.setDoubleBuffered(false);
        mPane.setContentType("text/html");
        mPane.setDocument(html); // "store" html in jTextPane container
        mStartY = new cCmUnit(0.0); // reset starting position in y-direction
    }

    /**
     * Sets the HTML document to be printed.
     * Precondition: The URL given contains a HTML document
     * @param         url                url to file with HTML document
     * @throws        IOException if errors occur while reading HTML document from file
     */
    public void setHTML(URL url) throws IOException {
        /*
         * By using an instance of SyncHTMLEditorKit, the html should load
         * synchroniously - so everything is loaded before printing starts
         */
        mPane = new JTextPane();
        mPane.setDoubleBuffered(false);
        mPane.setEditorKit(new SyncHTMLEditorKit());
        mPane.setContentType("text/html");
        mPane.setPage(url);
        mStartY = new cCmUnit(0.0); // reset starting position in y-direction
    }

    /**
     * Sets the starting position in y-direction. A starting position != 0 is used
     * when printing anything but the first page. This bookkeeping is usually done
     * internally when creating pagebreaks using the method breakBlock.
     * Therefore this method has not been made public.
     * @param        y                Starting position in y-direction
     */
    protected void setStartY(cUnit y) {
        mStartY = new cCmUnit(y);
    }

    /**
     * Prints the contents of this HTML print object using the supplied
     * Graphics2D object.
     * @param        g        Used for rendering (i.e. printing) this HTML print object
     * @see org.columba.core.print.cPrintObject#print(java.awt.Graphics2D)
     */
    public void print(Graphics2D g) {
        /*
             * *20030609, karlpeder* Introduced scaling
             */
        computePositionAndSize();

        // get origin & size information (height as "total" height minus current pos.)
        cPoint origin = getDrawingOrigin();
        double width = getDrawingSize().getWidth().getPoints();
        double height = getPage().getPrintableAreaSize().getHeight()
                            .sub(getLocation().getY()).getPoints();

        /*
         * TODO (@author karlpeder): Guess that right thing to do is to get height as getDrawingSize().getHeight(),
         * since this should take top- and bottom margin of this print
         * object into account. But the height seems not to be set
         * correctly in computePositionAndSize() (*20030604, karlpeder*)
         */
        // set size of mPane according to the available width
        // and fetch root view
        mPane.setSize((int) width, Integer.MAX_VALUE);
        mPane.validate();

        View rootView = mPane.getUI().getRootView(mPane);

        // scale the graphics
        double scale = scaleFactor(new cPointUnit(width));
        g.scale(scale, scale);

        // set clipping for the graphics object
        Shape oldClip = g.getClip();
        g.setClip((int) (origin.getX().getPoints() / scale),
            (int) (origin.getY().getPoints() / scale), (int) (width / scale),
            (int) (height / scale));

        // translate g to line up with origin of print area (trans 1)
        Point2D.Double trans = new Point2D.Double(g.getClipBounds().getX(),
                g.getClipBounds().getY());
        g.translate(trans.getX(), trans.getY());

        // set allocation (defines print area together with the clipping
        // and translation made above), and print...
        Rectangle allocation = new Rectangle(0, (int) -mStartY.getPoints(),
                (int) mPane.getMinimumSize().getWidth(),
                (int) mPane.getPreferredSize().getHeight());
        printView(g, rootView, allocation, height / scale);

        // translate graphics object back to original position and reset clip and scaling
        g.translate(-trans.getX(), -trans.getY());
        g.scale(1 / scale, 1 / scale);
        g.setClip(oldClip);
    }

    /**
     * Private utility to print a view (called from the print method).<br>
     * The traversal through views and their children is the same as in
     * calcBreakHeightForView.
     *
     * @param        g                                Graphics object to print on
     * @param        view                        The View object to operate on
     * @param        allocation                Allocation for the view (where to render)
     * @param        maxHeight                Views starting after maxHeight is not printed
     */
    private void printView(Graphics2D g, View view, Shape allocation,
        double maxHeight) {
        if (view.getViewCount() > 0) {
            // child views exist - operate recursively on these
            Shape childAllocation;
            View childView;

            for (int i = 0; i < view.getViewCount(); i++) {
                childAllocation = view.getChildAllocation(i, allocation);

                if (childAllocation != null) {
                    childView = view.getView(i);

                    // handle child view by recursive call
                    printView(g, childView, childAllocation, maxHeight);
                }
            }
        } else {
            // no childs - we have a leaf view (i.e. with contents)
            double viewStartY = allocation.getBounds().getY();

            if ((viewStartY >= 0) && (viewStartY < maxHeight)) {
                // view starts on page - print it
                view.paint(g, allocation);
            }
        }
    }

    /**
     * Returns the size of this HTML print object subject to the
     * given width.<br>
     * If scaling is allowed, and the contents can not be fitted inside
     * the given width, the content is scaled to fit before the size is
     * returned, i.e. the scaled size is returned.<br>
     * NB: The height returned will always be from the starting point
     * (which could be different from the top) to the end of the current
     * content, independent on whether everything will or can be printed
     * on onto one page.
     *
     * @param        maxWidth                Max. allowable width this print object can occupy
     *
     * @see org.columba.core.print.cPrintObject#getSize(org.columba.core.print.cUnit)
     */
    public cSize getSize(cUnit maxWidth) {
        /*
             * *20030609, karlpeder* Introduced scaling
             */

        // resize jTextPane component to calculate height and get it
        double width = maxWidth.sub(leftMargin).sub(rightMargin).getPoints();
        mPane.setSize((int) width, Integer.MAX_VALUE);
        mPane.validate();

        double height = mPane.getPreferredSize().getHeight();

        // correct for starting position if printing should not start at the top
        height = height - mStartY.getPoints();

        // calculate size and return it
        double scale = scaleFactor(new cPointUnit(width));
        cUnit w = new cCmUnit(maxWidth); // width unchanged
        cUnit h = new cCmUnit();
        h.setPoints(height); // height of content
        h.addI(topMargin); // + top margin
        h.addI(bottomMargin); // + bottom margin
        h.setPoints(h.getPoints() * scale); // height corrected for scaling

        return new cSize(w, h);
    }

    /**
     * Returns the scale, which should be applied to the content to make it
     * fit inside the given width.<br>
     * If scaling is not allowed, 1.0 will be returned.<br>
     * If the content fits inside the given width, or is smaller, 1.0 will
     * be returned.
     * @author  Karl Peder Olesen (karlpeder), 20030609
    * @param        maxWidth                Max. allowable width this print object can occupy
     * @return  scale to be applied to make the contents fit inside the given width
     */
    private double scaleFactor(cUnit maxWidth) {
        mPane.validate(); // ensure contents is layed out properly

        if (!mScaleAllowed) {
            LOG.info("Scaling not active - returning scale=1.0");

            return 1.0;
        } else {
            // calculate scaling and return it
            double width = maxWidth.sub(leftMargin).sub(rightMargin).getPoints();
            double scale;

            if (mPane.getMinimumSize().getWidth() > width) {
                scale = width / mPane.getMinimumSize().getWidth();
            } else {
                scale = 1.0; // do not scale up, i.e. no scale factor above 1.0
            }

            LOG.info("Returning scale=" + scale);

            return scale;
        }
    }

    /**
     * Divides (breaks) this HTML print object into a remainder (which fits
     * inside the given max height) and the rest. The remainder is returned and
     * "the rest" is stored by modifying this object.
     *
     * @param        w                        Max. allowable width this print object can occupy
     * @param        maxHeight        Max. allowable height before breaking
     * @return        The part of the print object, which fits inside the given max height
     *
     * @see org.columba.core.print.cPrintObject#breakBlock(org.columba.core.print.cUnit, org.columba.core.print.cUnit)
     */
    public cPrintObject breakBlock(cUnit w, cUnit maxHeight) {
        /*
             * *20030609, karlpeder* Introduced scaling
             */

        // get size of content (width, height is size without scaling)
        cSize contentSize = this.getSize(w); // scaled size
        double scale = scaleFactor(w);
        int width = (int) (contentSize.getWidth().getPoints() / scale);
        int height = (int) (contentSize.getHeight().getPoints() / scale);
        int startY = (int) mStartY.getPoints();

        // define allocation rectangle (startY is used to compensate for
        // different start point if printing shall not start from the top)
        Rectangle allocation = new Rectangle(0, -startY, width, height
                + startY);

        // set initial value for height where this print object should be broken
        double breakHeight = maxHeight.getPoints() / scale; // in points, without scale

        /*
         * calculate a new break height according to the contents, possibly
         * smaller to break before some content (i.e. not to break in the
         * middle of something
         */
        View rootView = mPane.getUI().getRootView(mPane);
        breakHeight = calcBreakHeightFromView(rootView, allocation, breakHeight);

        // create remainder
        cHTMLPart remainder = new cHTMLPart(mScaleAllowed);
        remainder.setHTML((HTMLDocument) mPane.getDocument());
        remainder.setStartY(mStartY);

        // modify "this" to start where remainder ends
        cUnit newStartY = new cCmUnit();

        if (breakHeight < height) {
            newStartY.setPoints(mStartY.getPoints() + breakHeight);
        } else { // this happends if there's nothing left for the next page
            newStartY = mStartY.add(contentSize.getHeight());
        }

        this.setStartY(newStartY);

        return remainder;
    }

    /**
     * Private utility to calculate break height based on the contents
     * of a view. If the break height calculated is not smaller than the
     * actual break height, actBreakHeight is returned unchanged.
     * @param        view                        The View object to operate on
     * @param        allocation                Allocation for the view (where to render)
     * @param        actBreakHeight         Actual break height
     */
    private double calcBreakHeightFromView(View view, Shape allocation,
        double actBreakHeight) {
        if (view.getViewCount() > 0) {
            // child views exist - operate recursively on these
            double breakHeight = actBreakHeight;
            Shape childAllocation;
            View childView;

            for (int i = 0; i < view.getViewCount(); i++) {
                childAllocation = view.getChildAllocation(i, allocation);

                if (childAllocation != null) {
                    childView = view.getView(i);

                    // calculate break height for child, and use updated
                    // value in the further processing
                    breakHeight = calcBreakHeightFromView(childView,
                            childAllocation, breakHeight);
                }
            }

            return breakHeight; // return (possibly) updated value
        } else {
            // no childs - we have a leaf view (i.e. with contents)
            double allocY = allocation.getBounds().getY();
            double allocMaxY = allocation.getBounds().getMaxY();
            if ((allocY >= 0) && (allocY < actBreakHeight)
                    && (allocMaxY > actBreakHeight)) {
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
                    return actBreakHeight; // unchanged, i.e. no breaks before this view
                } else {
                    // view can be broken
                    if (allocY < actBreakHeight) {
                        return allocY; // break before start of view
                    } else {
                        return actBreakHeight; // unchanged
                    }
                }
            } else {
                return actBreakHeight; // unchanged
            }
        }
    }
}


/**
 * Utility class used for loading html synchroniously into a jTextPane
 * @author        Karl Peder Olesen (karlpeder), 20030604
 */
class SyncHTMLEditorKit extends HTMLEditorKit {
    /**
     * Create an uninitialized text storage model that is appropriate for
     * this type of editor.<br>
     * The document returned will load synchroniously.
     *
     * @see javax.swing.text.EditorKit#createDefaultDocument()
     */
    public Document createDefaultDocument() {
        Document doc = super.createDefaultDocument();
        ((HTMLDocument) doc).setAsynchronousLoadPriority(-1);

        return doc;
    }
}
