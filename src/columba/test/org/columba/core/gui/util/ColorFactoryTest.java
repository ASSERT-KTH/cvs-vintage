/*
 * Created on 2003-nov-02
 */
package org.columba.core.gui.util;

import java.awt.Color;

import org.columba.core.gui.base.ColorFactory;

import junit.framework.TestCase;


/**
 * @author Erik Mattsson
 */
public class ColorFactoryTest extends TestCase {
    /*
 * Test for getColor()
 */
    public void testGetColor() {
        ColorFactory.clear();

        Color col1 = ColorFactory.getColor(0);
        assertNotNull("The factory returned a null object", col1);

        Color col2 = ColorFactory.getColor(1);
        assertNotNull("The factory returned a null object", col2);
        assertNotSame("The factory returned the same object for different values",
            col1, col2);

        Color col1again = ColorFactory.getColor(0);
        assertNotNull("The factory returned a null object", col1again);
        assertSame("The factory did not return the same object for a value",
            col1, col1again);
    }
}
