package org.jboss.verifier.event;

/*
 * Class org.jboss.verifier.event.VerificationEventFactory
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
 * $Id: VerificationEventFactory.java,v 1.1 2000/05/29 18:26:31 juha Exp $
 *
 * You can reach the author by sending email to jpl@gjt.org or
 * directly to jplindfo@helsinki.fi.
 */

 
// standard imports


// non-standard class dependencies
import org.jboss.verifier.strategy.VerificationContext;
import org.jboss.verifier.strategy.EJBVerifier11;


/**
 * Factory of verification events.
 * 
 * <p>
 *
 * This should actually be defined as an interface instead of concrete class
 * and provide implementations for this interface returning different types
 * of verification events (for example, a gui might want the event messages
 * to be styled).
 *
 * <p>
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
public class VerificationEventFactory {

    /*
     * Default constructor.
     */
    public VerificationEventFactory()    {   }
    
    
    public VerificationEvent createSpecViolationEvent(

                                    VerificationEventGenerator source,
                                    String section,
                                    String name
                                                      )  {
                     
        VerificationEvent event = new VerificationEvent(source);

        
        if (EJBVerifier11.SECTION_6_5_1.equals(section))
            buildNoSessionBeanEvent(event, name);
            
        else if (EJBVerifier11.DTD_EJB_CLASS.equals(section))
            buildEJBClassNotFoundEvent(event, name);
            

        return event;                                     
    }
    
    public VerificationEvent createBeanVerifiedEvent(
    
                                    VerificationEventGenerator source,
                                    String name
                                                     )  {
        
        VerificationEvent event = new VerificationEvent(source);
        
        event.setState(VerificationEvent.OK);
        event.setMessage(name + " verified.");
        
        return event;
    }

    

/*
 *****************************************************************************
 *****************************************************************************
 *
 *  PRIVATE INSTANCE METHODS
 *
 *****************************************************************************
 *****************************************************************************
 */


    private void buildNoSessionBeanEvent(VerificationEvent event, String name) {
        
        event.setMessage(SESSION_DOES_NOT_IMPLEMENT_SESSIONBEAN);
        event.setVerbose(SPEC_SECTION_6_5_1);
    }
    
    private void buildEJBClassNotFoundEvent(VerificationEvent event, String name) {
        
        event.setMessage(EJB_CLASS_ELEMENT_NOT_FOUND);
        event.setVerbose(DEPLOYMENT_DTD_EJB_CLASS);
    }
    
    
    
    
    private final static String SESSION_DOES_NOT_IMPLEMENT_SESSIONBEAN =
        "Session bean does not implement the required SessionBean interface.";
        
    private final static String SPEC_SECTION_6_5_1 = 
    
        VerificationContext.VERSION_1_1      + " " +
        "Section 6.5.1"                      + " " +
        "The required SessionBean interface" + " " +
        
        "\n" +
        
        "All session beans must implement the SessionBean interface";

        
    private final static String EJB_CLASS_ELEMENT_NOT_FOUND =
        "The enterprise bean's class was not found.";
        
    private final static String DEPLOYMENT_DTD_EJB_CLASS =
    
        VerificationContext.VERSION_1_1     + " " +
        "Section 16.5"                      + " " +
        "Deployment Description DTD"        + " " +
        
        "\n" +
        
        "The ejb-class element contains the fully-qualified name of the " + 
        "enterprise bean's class."                                        +
        
        "\n" +
        
        "Used in: entity and session"                                     +
        
        "\n" +
        
        "Example: <ejb-class>com.wombat.empl.EmployeeServiceBean</ejb-class>";


}


