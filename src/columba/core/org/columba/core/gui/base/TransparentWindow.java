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

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JWindow;


public class TransparentWindow extends JWindow {
    Robot robot;
    BufferedImage screen;
    Shape shape;
    BufferedImage buffer;
    ImageIcon splashimg;

    public TransparentWindow(ImageIcon splashimg) throws AWTException {
        this.splashimg = splashimg;

        robot = new Robot(getGraphicsConfiguration().getDevice());
        requestFocus();
        setSize(splashimg.getIconWidth(), splashimg.getIconHeight());
        buffer = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        updateScreen();

        enableEvents(AWTEvent.FOCUS_EVENT_MASK);

    }

    protected void updateScreen() {
        screen = robot.createScreenCapture(new Rectangle(new Point(0, 0),
                    Toolkit.getDefaultToolkit().getScreenSize()));
    }

    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);

        if (e.getID() == FocusEvent.FOCUS_GAINED) {
            updateScreen();
            repaint();
        }
    }

    public void paint(Graphics _g) {
        Graphics2D g = buffer.createGraphics();

        if (screen != null) {
            Point location = getLocationOnScreen();
            g.drawImage(screen, -location.x, -location.y, this);
        }

        g.drawImage(splashimg.getImage(), 0, 0, this);

        _g.drawImage(buffer, 0, 0, this);
    }
}
