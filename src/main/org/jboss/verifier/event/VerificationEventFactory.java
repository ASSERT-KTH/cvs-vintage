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
 * This package and its source code is available at www.jboss.org
 * $Id: VerificationEventFactory.java,v 1.4 2000/06/03 21:43:56 juha Exp $
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
 * @author 	Juha Lindfors   (jplindfo@helsinki.fi)
 * @version $Revision: 1.4 $
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

            
        else if (EJBVerifier11.SECTION_6_5_3_a.equals(section))
            buildStatelessSessionCannotSynchEvent(event, name);
            
        else if (EJBVerifier11.SECTION_6_5_3_b.equals(section))
            buildBeanManagedTxSessionCannotSynchEvent(event, name);
        
        
        else if (EJBVerifier11.SECTION_6_5_5.equals(section))
            buildNoEJBCreateMethodEvent(event, name);
        
        
        else if (EJBVerifier11.SECTION_6_6_1.equals(section))
            buildBeanManagedTxBeanCannotSynchEvent(event, name);
            

        else if (EJBVerifier11.SECTION_6_10_2_a.equals(section))
            buildSessionBeanClassNotPublicEvent(event, name);
            
        else if (EJBVerifier11.SECTION_6_10_2_b.equals(section))
            buildSessionBeanClassIsFinalEvent(event, name);

        else if (EJBVerifier11.SECTION_6_10_2_c.equals(section))
            buildSessionBeanClassIsAbstractEvent(event, name);
        
        else if (EJBVerifier11.SECTION_6_10_2_d.equals(section))
            buildNoDefaultConstructorInSessionBeanEvent(event, name);
        
        else if (EJBVerifier11.SECTION_6_10_2_e.equals(section))
            buildSessionBeanDefinesFinalizerEvent(event, name);

        
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

    private void buildStatelessSessionCannotSynchEvent(VerificationEvent event, String name) {
        
        event.setMessage(STATELESS_MUST_NOT_IMPLEMENT_SYNCHRONIZATION);
    //    event.setVerbose();
    }
    
    private void buildBeanManagedTxSessionCannotSynchEvent(VerificationEvent event, String name) {
        
    //    event.setMessage();
    //    event.setVerbose();
    }
    
    private void buildNoEJBCreateMethodEvent(VerificationEvent event, String name) {
        
        event.setMessage(SESSION_DOES_NOT_DEFINE_EJBCREATE);
        event.setVerbose(SPEC_SECTION_6_5_5);
    }
    
    private void buildBeanManagedTxBeanCannotSynchEvent(VerificationEvent event, String name) {
        
    //    event.setMessage();
    //    event.setVerbose();
    }
    
    private void buildSessionBeanClassNotPublicEvent(VerificationEvent event, String name) {
        
        event.setMessage(SESSION_BEAN_CLASS_MUST_BE_PUBLIC);
    //    event.setVerbose();
    }

    private void buildSessionBeanClassIsFinalEvent(VerificationEvent event, String name) {
        
        event.setMessage(SESSION_BEAN_CLASS_MUST_NOT_BE_FINAL);
    //    event.setVerbose();
    }

    private void buildSessionBeanClassIsAbstractEvent(VerificationEvent event, String name) {
        
        event.setMessage(SESSION_BEAN_CLASS_MUST_NOT_BE_ABSTRACT);
    //    event.setVerbose();
    }

    private void buildNoDefaultConstructorInSessionBeanEvent(VerificationEvent event, String name) {
        
        event.setMessage(SESSION_BEAN_CLASS_MUST_DEFINE_DEFAULT_CONSTRUCTOR);
    //    event.setVerbose();
    }

    private void buildSessionBeanDefinesFinalizerEvent(VerificationEvent event, String name) {
        
    //    event.setMessage();
    //    event.setVerbose();
    }

    private void buildEJBClassNotFoundEvent(VerificationEvent event, String name) {
        
        event.setMessage(EJB_CLASS_ELEMENT_NOT_FOUND);
        event.setVerbose(DEPLOYMENT_DTD_EJB_CLASS);
    }
    
    
    
    /*
     * [TODO] these are the messages for verification events, mostly citing the
     *        ejb bible. they should be moved to separate resource files, probably
     *        wrapped into something nice, like XML, so that we can format the
     *        messages properly depending on the situation. For example, the EJX
     *        might like to have nice purdy styles for GUI stuff, maybe links to
     *        the spec, or something more fancy.
     *
     *        cause I'm lazy, I haven't done this yet.
     */
    private final static String SESSION_DOES_NOT_IMPLEMENT_SESSIONBEAN =
        "Session bean does not implement the required SessionBean interface.";        
        
    private final static String SPEC_SECTION_6_5_1 = 
    
        VerificationContext.VERSION_1_1      + " " +
        "Section 6.5.1"                      + " " +
        "The required SessionBean interface" + " " +
        
        "\n" +
        
        "All session beans must implement the SessionBean interface.";


    private final static String STATELESS_MUST_NOT_IMPLEMENT_SYNCHRONIZATION =
        "A stateless Session bean must not implement the SessionSynchronization interface.";
        

    private final static String SESSION_BEAN_CLASS_MUST_BE_PUBLIC     =
        "Session bean class must be defined as public.";

    private final static String SESSION_BEAN_CLASS_MUST_NOT_BE_ABSTRACT =
        "Session bean class must not be abstract.";

    private final static String SESSION_BEAN_CLASS_MUST_NOT_BE_FINAL  =
        "Session bean class must not be final.";

    private final static String SESSION_BEAN_CLASS_MUST_DEFINE_DEFAULT_CONSTRUCTOR =
        "Session bean class must have a public constructor that takes no parameters.";
        
        
    private final static String SESSION_DOES_NOT_DEFINE_EJBCREATE     =
        "Session bean does not define the required ejbCreate method.";

    private final static String SPEC_SECTION_6_5_5 =

        VerificationContext.VERSION_1_1         + " " +
        "Section 6.5.5"                         + " " +
        "Session bean's ejbCreate(...) methods" + " " +
        
        "\n" +
        
        "Each session bean class must have at least one ejbCreate method. " +
        "The number and signatures of a session bean's create methods are " +
        "specific to each session bean class.";
        
        
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


