/*
 * Copyright (c) 2001 Peter Antman Tim <peter.antman@tim.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.jms.ra;


/**
 * Level.java
 *
 *
 * Created: Tue Apr 17 21:26:13 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class Level  {
        private static final Level[] levels = {new Level("OFF", 0),
					    new Level("SEVERE", 1),
					    new Level("WARNING", 2),
					    new Level("INFO", 3),
					    new Level("CONFIG", 4),
					    new Level("FINE", 5),
					    new Level("ALL", 6)};
    
    public static final Level OFF = levels[0];
    public static final Level SEVERE = levels[1];
    public static final Level WARNING = levels[2];
    public static final Level INFO = levels[3];
    public static final Level CONFIG =levels[4];
    public static final Level FINE = levels[5];
    public static final Level ALL = levels[6];

    private String name;
    private int value;
    
protected Level(String name, int value) {
	this.name = name;
	this.value = value;
    }

    public boolean equals(Object o) {
	Level l = (Level)o;
	return(this.value == l.intValue());
    }
    
    public final int intValue() {
	return value;
    }
    
    public String toString() {
	return name;
    }

    public static Level parse(String name) throws IllegalArgumentException{
	// Try convert to int first
	try {
	    int l = new Integer(name).intValue();
	    if (l < levels.length)
		return levels[l];
	    else
		throw new IllegalArgumentException("No level for number: " + l);
	    
	}catch(NumberFormatException ex) {//NOOP, try string instead
	}
	
	// are we here, do string stuff
	for (int i = 0; i < levels.length;i++) {
	    if (name.equals(levels[i].toString())) {
		return levels[i];
	    }
	}
	// If we go here we have an error
	throw new IllegalArgumentException("No level for string: " + name);
    }
} // Level
