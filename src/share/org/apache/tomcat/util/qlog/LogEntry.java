/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.qlog;

/**
 * This is an entry that is created in response to every
 * Logger.log(...) call.
 *
 * @author Anil V (akv@eng.sun.com)
 * @since  Tomcat 3.1
 */
public final  class LogEntry {
    String logName;
    long date=0;
    String prefix;
    String message;
    Throwable t;
    QueueLogger l;

    LogEntry(QueueLogger l) {
	this.l=l;
    }
    
    QueueLogger getLogger() {
	return l;
    }
    

    void setDate(long date ) {
	this.date = date;
    }
    void setPrefix( String prefix ) {
	this.prefix=prefix;
    }
    
    void setMessage( String message ) {
	this.message = message;
    }
    void setThrowable( Throwable t) {
	this.t = t;
    }
    
    // XXX should move to LogFormat !!!
    public void print( StringBuffer outSB) {
	if (date!=0) {
	    l.formatTimestamp( date, outSB );
	    outSB.append(" - ");
	}
	if (prefix != null) {
	    outSB.append(prefix).append( ": ");
	}

	if (message != null) 
	    outSB.append(message);
	
	if (t != null) {
	    outSB.append(" - ");
	    outSB.append(l.throwableToString( t ));
	}
    }
}
