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
 * This package and its source code is available at www.jboss.org
 * $Id: VerificationEvent.java,v 1.4 2000/10/15 20:52:28 juha Exp $
 */


// standard imports
import java.util.EventObject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


// non-standard class dependencies
import org.jboss.verifier.Section;


/**
 *
 * @author 	Juha Lindfors   (jplindfo@helsinki.fi)
 * @version $Revision: 1.4 $
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
    private String message  = "<undefined>";

    /*
     * Contains a more verbose description of this event.
     */
    private String verbose  = "";

    private String beanName = "<unnamed>";
    
    private Method method   = null;
    
    private String section  = null;
 
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 

    /*
     * Constructor
     */
    public VerificationEvent(VerificationEventGenerator source) {
        super(source);
    }

    public VerificationEvent(VerificationEventGenerator source, String message) {
        this(source);
        setMessage(message);
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

    public void setName(String name) {
        this.beanName = name;
    }
    
    public void setSection(Section section) {
        this.section = section.getSection();
    }
    
    public void setMethod(Method method) {
        if (method == null) 
            return;
            
        this.method = method;
    }
    
    public String getMessage() {
        return beanName + ": " + message;
    }

    public String getVerbose() {
        
        StringBuffer buf = new StringBuffer();
        String linebreak = System.getProperty("line.separator");
        
        buf.append(linebreak + "Class  : " + beanName + linebreak);
        
        if (method != null) {
            String returnClassName = method.getReturnType().getName();
            int len     = returnClassName.length();
            int roffset = returnClassName.lastIndexOf(".");
            
            if (roffset == -1)
                roffset = 0;
            
            String returnType = returnClassName.substring(roffset+1, len);
            
            Class[] exceptions  = method.getExceptionTypes();
            StringBuffer excbuf = new StringBuffer(100);
            
            for (int i = 0; i < exceptions.length; ++i) {
                String exceptionClassName = exceptions[i].getName();
                int elen    = exceptionClassName.length();
                int eoffset = exceptionClassName.lastIndexOf(".");
                
                if (eoffset == -1)
                    eoffset = 0;
                    
                excbuf.append(exceptionClassName.substring(eoffset+1, elen))
                      .append(", ");
            }
            excbuf.delete(excbuf.length()-2, excbuf.length());
            
            buf.append("Method : " + Modifier.toString(method.getModifiers()) + " " +
                                     returnType        + " " + 
                                     method.getName()  + "() throws " +
                                     excbuf.toString() + 
                                     linebreak);
        }
        
        int offset = section.lastIndexOf(".");
        if (!Character.isDigit(section.charAt(offset+1)))            
             buf.append("Section: " + section.substring(0, offset)  + linebreak);
        else buf.append("Section: " + section + linebreak);
        
        buf.append("Warning: " + message  + linebreak);
        
        return buf.toString();
    }

    public String getName() {
        return beanName;
    }
    
    
    /*
     * String constants
     */
    private final static String STATE_NOT_RECOGNIZED =
        "Unknown event state";

}

