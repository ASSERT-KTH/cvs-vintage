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

package org.columba.core.gui.util;

import java.awt.Color;

import java.util.Hashtable;
import java.util.Map;

/**
 * Factory class that creates <code>Color</code> objects.
 * The factory returns a <code>Color</code> object that should not
 * be altered.
 *
 * @author redsolo
 */
public class ColorFactory {
    private static Map colors = new Hashtable();

    /**
 * Returns a <code>Color</code> object for the specified rgb value.
 * The method returns the same object if it is accessed with the same
 * rgb value.
 * @param rgb the rgb value.
 * @return a <code>Color</code> object.
 */
    public static Color getColor(int rgb) {
        Integer key = new Integer(rgb);
        Color color = (Color) colors.get(key);

        if (color == null) {
            color = new Color(rgb);
            colors.put(key, color);
        }

        return color;
    }

    /**
 * Clears all Colors from this factory.
 */
    public static void clear() {
        colors.clear();
    }
}
