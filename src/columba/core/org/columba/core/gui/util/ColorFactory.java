/*
 * Created on 2003-nov-02
 */
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
