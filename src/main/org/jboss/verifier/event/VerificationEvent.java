package org.jboss.verifier.event;

/*
 * Class org.jboss.verifier.event.VerificationEvent
 * Copyright (C) 2000  Juha Lindfors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This package and its source code is available at www.gjt.org
 * $Id: VerificationEvent.java,v 1.1 2000/05/29 18:26:31 juha Exp $
 *
 * You can reach the author by sending email to jpl@gjt.org or
 * directly to jplindfo@helsinki.fi.
 */


// standard imports
import java.util.EventObject;


// non-standard class dependencies


/**
 * << DESCRIBE THE CLASS HERE >>
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors
 * @version $Revision: 1.1 $
 * @since  	JDK 1.3
 */
public class VerificationEvent extends EventObject {

    public static final String WARNING  = "WARNING";
    public static final String OK       = "OK";
    
    private boolean isOk      = false;
    private boolean isWarning = false;
    

    /*
     * Contains a short, one line message for this event.
     */
    private String message = "<undefined>";

    /*
     * Contains a more verbose description of this event.
     */
    private String verbose = "";
    
    
    /*
     * Constructor
     */
    public VerificationEvent(VerificationEventGenerator source) {
        super(source);
    }
    
    public void setState(String state) {
        
        if (WARNING.equalsIgnoreCase(state)) {
            isWarning = true;
            isOk      = false;
        }
            
        else if (OK.equalsIgnoreCase(state)) {
            isOk      = true;
            isWarning = false;
        }
            
        else 
            throw new IllegalArgumentException(STATE_NOT_RECOGNIZED + ": " + state);
    }
    
    public boolean isOk() {
        return isOk;
    }
    
    public boolean isWarning() {
        return isWarning;
    }
    
    public void setMessage(String msg) {
        this.message = msg;
    }
    
    public void setVerbose(String msg) {
        this.verbose = msg;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getVerbose() {
        return verbose;
    }
    
    /*
     * String constants
     */
    private final static String STATE_NOT_RECOGNIZED =
        "Unknown event state";

}

