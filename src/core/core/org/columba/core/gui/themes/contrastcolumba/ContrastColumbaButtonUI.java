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

package org.columba.core.gui.themes.contrastcolumba;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

public class ContrastColumbaButtonUI extends BasicButtonUI {

    private final static ContrastColumbaButtonUI gtkButtonUI = new ContrastColumbaButtonUI();

    protected Color selectColor;     
    protected Color lightShadow;
    protected Color mediumShadow;
    protected Color darkShadow;
    protected Color highlight;

    private boolean defaults_initialized = false;
    
    public static ComponentUI createUI(JComponent c){
	return gtkButtonUI;
    }
    
    protected BasicButtonListener createButtonListener(AbstractButton b) {
	return new ContrastColumbaButtonListener(b);
    }

    public void installDefaults(AbstractButton b) {
	super.installDefaults(b);
	if(!defaults_initialized) {
	    selectColor = UIManager.getColor(getPropertyPrefix() + "select");
	    defaults_initialized = true;
	    
	    lightShadow = MetalLookAndFeel.getControlHighlight();
	    mediumShadow = MetalLookAndFeel.getControlDarkShadow();
	    darkShadow = Color.black;
	    highlight = Color.white;
	}
	b.setOpaque(false);
    }

    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }
    
    protected Color getSelectColor() {
	return selectColor;
    }
    
    public void paint(Graphics g, JComponent c) {
	AbstractButton b = (AbstractButton) c;
	Color col = c.getBackground();
	/*
	if (b.getModel().isRollover()) {
	    col = highlight;
	}
        fillContentArea(g, (AbstractButton) c, col);   
      */
	super.paint(g, c);
    }

    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
	// focus painting is handled by the border
    }
    
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        fillContentArea(g, b, getSelectColor());
    }

    protected void fillContentArea(Graphics g, AbstractButton b, Color fillColor) {
        if (b.isContentAreaFilled()) {
	    Insets margin = b.getMargin();
	    Insets insets = b.getInsets();
	    Dimension size = b.getSize();
	    g.setColor(fillColor);
	    g.fillRect(insets.left-margin.left,
		       insets.top-margin.top, 
		       size.width-(insets.left-margin.left)-(insets.right-margin.right),
		       size.height-(insets.top-margin.top)-(insets.bottom-margin.bottom));
	}
    }
}



