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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class TriangleSquareWindowsCornerIcon implements Icon {

    //RGB values discovered using ZoomIn
    private static final Color THREE_D_EFFECT_COLOR = new Color(255, 255, 255);
    private static final Color SQUARE_COLOR_LEFT = new Color(184, 180, 163);
    private static final Color SQUARE_COLOR_TOP_RIGHT = new Color(184, 180, 161);
    private static final Color SQUARE_COLOR_BOTTOM_RIGHT = new Color(184, 181, 161);

    //Dimensions
    private static final int WIDTH = 12;
    private static final int HEIGHT = 12;



    public int getIconHeight() {
        return WIDTH;
    }

    public int getIconWidth() {
        return HEIGHT;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {

        //Layout a row and column "grid"
        int firstRow = 0;
        int firstColumn = 0;
        int rowDiff = 4;
        int columnDiff = 4;

        int secondRow = firstRow + rowDiff;
        int secondColumn = firstColumn + columnDiff;
        int thirdRow = secondRow + rowDiff;
        int thirdColumn = secondColumn + columnDiff;


        //Draw the white squares first, so the gray squares will overlap
        draw3dSquare(g, firstColumn+1, thirdRow+1);

        draw3dSquare(g, secondColumn+1, secondRow+1);
        draw3dSquare(g, secondColumn+1, thirdRow+1);

        draw3dSquare(g, thirdColumn+1, firstRow+1);
        draw3dSquare(g, thirdColumn+1, secondRow+1);
        draw3dSquare(g, thirdColumn+1, thirdRow+1);

        //draw the gray squares overlapping the white background squares
        drawSquare(g, firstColumn, thirdRow);

        drawSquare(g, secondColumn, secondRow);
        drawSquare(g, secondColumn, thirdRow);

        drawSquare(g, thirdColumn, firstRow);
        drawSquare(g, thirdColumn, secondRow);
        drawSquare(g, thirdColumn, thirdRow);

    }

    private void draw3dSquare(Graphics g, int x, int y){
        Color oldColor = g.getColor(); //cache the old color
        g.setColor(THREE_D_EFFECT_COLOR); //set the white color
        g.fillRect(x,y,2,2); //draw the square
        g.setColor(oldColor); //reset the old color
    }


    private void drawSquare(Graphics g, int x, int y){
        Color oldColor = g.getColor();
        g.setColor(SQUARE_COLOR_LEFT);
        g.drawLine(x,y, x,y+1);
        g.setColor(SQUARE_COLOR_TOP_RIGHT);
        g.drawLine(x+1,y, x+1,y);
        g.setColor(SQUARE_COLOR_BOTTOM_RIGHT);
        g.drawLine(x+1,y+1, x+1,y+1);
        g.setColor(oldColor);
    }

}