package org.jboss.verifier.factory;

/*
 * Class org.jboss.verifier.factory.DefaultEventFactory
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
 * $Id: DefaultEventFactory.java,v 1.3 2000/10/15 20:52:29 juha Exp $
 */

 
// standard imports
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import java.io.InputStream;
import java.io.IOException;

// non-standard class dependencies
import org.jboss.verifier.strategy.VerificationContext;
import org.jboss.verifier.strategy.EJBVerifier11;
import org.jboss.verifier.Section;

import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationEventGenerator;

/**
 * <p>
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors   (jplindfo@helsinki.fi)
 * @version $Revision: 1.3 $
 * @since  	JDK 1.3
 */
public class DefaultEventFactory implements VerificationEventFactory {

    public final static String MESSAGE_BUNDLE = 
        "/org/jboss/verifier/DefaultMessages.properties";
        
    private Map msgTable = null;
    
    /*
     * Default constructor.
     */
    public DefaultEventFactory()    {
    
        msgTable = loadErrorMessages();
    }
    
    
    public VerificationEvent createSpecViolationEvent(
                                    VerificationEventGenerator source,
                                    Section section) {
                                        
        VerificationEvent event = new VerificationEvent(source);
        event.setState(VerificationEvent.WARNING);
        event.setSection(section);
        event.setMessage((String)msgTable.get(section.getSection()));
        
        return event;                                        
    }
        
    
    public VerificationEvent createBeanVerifiedEvent(VerificationEventGenerator source)  {
        
        VerificationEvent event = new VerificationEvent(source);
        
        event.setState(VerificationEvent.OK);
        event.setMessage("Verified.");
        
        return event;
    }

    

/*
 *****************************************************************************
 *
 *  PRIVATE INSTANCE METHODS
 *
 *****************************************************************************
 */

    
    /*
     * loads messages from a property file
     */
    private Map loadErrorMessages() {
        
        try {
            InputStream in    = getClass().getResourceAsStream(MESSAGE_BUNDLE);
            Properties  props = new Properties();
        
            props.load(in);
            
            return props;
        }
        catch (IOException e) {
            throw new MissingResourceException("I/O failure: " + e.getMessage(),
                                               MESSAGE_BUNDLE, "");
        }
        catch (NullPointerException e) {
            throw new MissingResourceException("Resource not found.",
                                               MESSAGE_BUNDLE, "");
        }        
    }
    
    
}


