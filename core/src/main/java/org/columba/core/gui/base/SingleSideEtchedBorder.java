// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.gui.base;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class SingleSideEtchedBorder extends EtchedBorder {
    protected int side;
    
    public SingleSideEtchedBorder(int side) {
        this(side, LOWERED);
    }
    
    public SingleSideEtchedBorder(int side, int etchType) {
        super(etchType);
        this.side = side;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.translate(x, y);
        int x1, y1, x2, y2;
        switch(side) {
            case SwingConstants.TOP:
                x2 = width-2;
                x1 = y1 = y2 = 0;
                break;
            case SwingConstants.LEFT:
                y2 = height-2;
                x1 = y1 = x2 = 0;
                break;
            case SwingConstants.RIGHT:
                x1 = x2 = width-2;
                y1 = 0;
                y2 = height-2;
                break;
            default:
                x1 = 0;
                x2 = width-2;
                y1 = y2 = height-2;
        }
        g.setColor(etchType == LOWERED? getShadowColor(c) : getHighlightColor(c));
        g.drawLine(x1, y1, x2, y2);
        g.setColor(etchType == LOWERED? getHighlightColor(c) : getShadowColor(c));
        g.drawLine(x1+1, y1+1, x2+1, y2+1);
        g.translate(-x, -y);
    }
    
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }
    
    public Insets getBorderInsets(Component c, Insets i) {
        switch(side) {
            case SwingConstants.TOP:
                i.top = 2;
                i.left = i.right = i.bottom = 0;
                break;
            case SwingConstants.LEFT:
                i.left = 2;
                i.top = i.right = i.bottom = 0;
                break;
            case SwingConstants.RIGHT:
                i.right = 2;
                i.top = i.left = i.bottom = 0;
                break;
            default:
                i.bottom = 2;
                i.top = i.left = i.right = 0;
        }
        return i;
    }
}
