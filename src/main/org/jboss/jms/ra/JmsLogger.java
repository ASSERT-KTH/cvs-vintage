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

import java.io.PrintWriter;

/**
 * JmsLogger.java
 *
 *
 * Created: Tue Apr 17 21:21:49 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class JmsLogger  {
    private PrintWriter logWriter = null;
    private boolean isOn = true;
    private Level logLevel = Level.ALL;
    
    public JmsLogger() {
	logWriter = new PrintWriter(System.out);

    }
    
    public void setLogWriter(PrintWriter out) {
	this.logWriter = out;
	this.log(Level.FINE, "Setting LogWriter: " + out);
    }

    public void setLogging(boolean log) {
	isOn = log;
    }

    public void setLogLevel(String level) {
	logLevel = Level.parse(level);
    }

    public void log(Level level, String message) {
	if(isOn && logLevel.intValue() >= level.intValue()) {
	    logWriter.println(level.toString() + ": " + message);
	    logWriter.flush();
	}
    }
    

    

} // JmsLogger
