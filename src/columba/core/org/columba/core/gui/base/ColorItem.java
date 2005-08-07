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
package org.columba.core.gui.base;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * A <code>JComboBox</code> item that represent different types of color.
 *
 * @author redsolo
 */
public class ColorItem {
    private Color itemColor;
    private String itemName;
    private Icon itemIcon;

    /**
 * Creates a color item with the specified color and name.
 * @param color the color to represent.
 * @param name the name of the color.
 */
    public ColorItem(Color color, String name) {
        setColor(color);
        itemName = name;
    }

    /**
 * @return the color.
 */
    public Color getColor() {
        return itemColor;
    }

    /**
 * Set a new color.
 * This method recreates a new icon to represent the color.
 * @param color the new color.
 */
    public final void setColor(Color color) {
        itemColor = color;
        itemIcon = createIcon(color);
    }

    /**
 * @return the name of the Color
 */
    public String getName() {
        return itemName;
    }

    /**
 * @return the icon for this ComboBox item.
 */
    public Icon getIcon() {
        return itemIcon;
    }

    /**
 * Creates and returns an icon that represents the specified color.
 * @param color create an icon for this color.
 * @return an Icon.
 */
    private Icon createIcon(Color color) {
        int width = 45;
        int height = 14;
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.black);
        graphics.drawRect(1, 1, width - 3, height - 3);
        graphics.setColor(color);
        graphics.fillRect(2, 2, width - 4, height - 4);
        graphics.dispose();

        return new ImageIcon(image);
    }
}
