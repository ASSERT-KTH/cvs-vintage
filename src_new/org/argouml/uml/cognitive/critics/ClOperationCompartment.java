// $Id: ClOperationCompartment.java,v 1.7 2004/08/15 22:39:24 bobtarling Exp $
// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.cognitive.critics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import org.argouml.cognitive.ToDoItem;
import org.argouml.ui.Clarifier;
import org.argouml.uml.diagram.static_structure.ui.FigClass;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigGroup;



public class ClOperationCompartment implements Clarifier {
    public static ClOperationCompartment TheInstance =
	new ClOperationCompartment();
    public static int WAVE_LENGTH = 4;
    public static int WAVE_HEIGHT = 2;

    ////////////////////////////////////////////////////////////////
    // instance variables
    Fig _fig;

    public void setFig(Fig f) { _fig = f; }
    public void setToDoItem(ToDoItem i) { }

    public void paintIcon(Component c, Graphics g, int x, int y) {
	if (_fig instanceof FigClass) {
	    FigClass fc = (FigClass) _fig;

	    // added by Eric Lefevre 13 Mar 1999: we must check if the
	    // FigText for operations is drawn before drawing things
	    // over it
	    if ( !fc.isOperationsVisible() ) {
		_fig = null;
		return;
	    }

	    FigGroup fg = fc.getOperationsFig();
	    int left  = fg.getX() + 10;
	    int height = fg.getY() + fg.getHeight() - 7;
	    int right = fg.getX() + fg.getWidth() - 10;
	    g.setColor(Color.red);
	    int i = left;
	    while (true) {
		g.drawLine(i, height, i + WAVE_LENGTH, height + WAVE_HEIGHT);
		i += WAVE_LENGTH;
		if (i >= right) break;
		g.drawLine(i, height + WAVE_HEIGHT, i + WAVE_LENGTH, height);
		i += WAVE_LENGTH;
		if (i >= right) break;
		g.drawLine(i, height, i + WAVE_LENGTH,
			   height + WAVE_HEIGHT / 2);
		i += WAVE_LENGTH;
		if (i >= right) break;
		g.drawLine(i, height + WAVE_HEIGHT / 2, i + WAVE_LENGTH,
			   height);
		i += WAVE_LENGTH;
		if (i >= right) break;
	    }
	    _fig = null;
	}
    }

    public int getIconWidth() { return 0; }
    public int getIconHeight() { return 0; }

    public boolean hit(int x, int y) {
	if (!(_fig instanceof FigClass)) return false;
	FigClass fc = (FigClass) _fig;
	FigGroup fg = fc.getOperationsFig();
	boolean res = fg.contains(x, y);
	_fig = null;
	return res;
    }

} /* end class ClOperationCompartment */


