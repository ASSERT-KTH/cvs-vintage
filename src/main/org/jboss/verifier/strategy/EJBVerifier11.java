package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.EJBVerifier11
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
 * $Id: EJBVerifier11.java,v 1.1 2000/05/29 18:26:30 juha Exp $
 *
 * You can reach the author by sending email to jpl@gjt.org or
 * directly to jplindfo@helsinki.fi.
 */


// standard imports
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;


// non-standard class dependencies
import org.gjt.lindfors.pattern.StrategyContext;

import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationEventFactory;

import com.dreambean.ejx.ejb.Session;
import com.dreambean.ejx.ejb.Entity;


/**
 * Concrete implementation of the <code>VerificationStrategy</code> interface.
 * This class implements the verification of both session and entity beans for
 * Enterprise JavaBeans v1.1 specification.
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
public class EJBVerifier11 implements VerificationStrategy {

    private VerificationContext context      = null;
    private VerificationEventFactory factory = null;
    private ClassLoader classloader          = null;
        
        
        
    /*
     *  Constructor
     */
    public EJBVerifier11(VerificationContext context) {
        
        URL[] list = { context.getJarLocation() };

        ClassLoader parent = getClass().getClassLoader();
        URLClassLoader cl  = new URLClassLoader(list, parent);

        this.classloader   = cl;
        this.context       = context;
        
        this.factory       = new VerificationEventFactory();
    }


    
    
    
    public void checkSession(Session session) {

        boolean beanVerified   = false;
        boolean homeVerified   = false;
        boolean remoteVerified = false;
                
                
        beanVerified   = verifySessionBean(session.getEjbClass());   
        homeVerified   = verifySessionHome(session.getHome());
        remoteVerified = verifySessionRemote(session.getRemote());


        if ( beanVerified && homeVerified && remoteVerified ) {
            /*
             * Verification for this session bean done. Fire the event
             * to tell listeneres everything is ok.
             */
            VerificationEvent event = 
                    factory.createBeanVerifiedEvent(context, session.getEjbClass());
            
            context.fireBeanChecked(event);
        }
    }

    
    public void checkEntity(Entity entity) {

        // NO IMPLEMENTATION
    }

    
    
    
    /*
     ***********************************************************************
     *
     *    IMPLEMENTS VERIFICATION STRATEGY INTERFACE
     *
     ***********************************************************************
     */
    
    public StrategyContext getContext() {
        return context;
    }

    public void checkSessions(Iterator beans) {

        while (beans.hasNext()) { 
            try {
                checkSession((Session)beans.next());
            }
            catch (ClassCastException e) {
                System.err.println(e);
                // THROW INTERNAL ERROR   
            }
        }
    }            

    public void checkEntities(Iterator beans) {
        
        while (beans.hasNext()) {
            try {
                checkEntity((Entity)beans.next());
            }
            catch (ClassCastException e) {
                System.err.println(e);
                // THROW INTERNAL ERROR
            }
        }
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
 
    private boolean verifySessionHome(String name) {
        
        // NO IMPLEMENTATION
        
        return true;
    }
    
    private boolean verifySessionRemote(String name) {
        
        // NO IMPLEMENTATION
        
        return true;
    }
    
    private boolean verifySessionBean(String name) {

        boolean status = true;
        
        try {
            Class home = classloader.loadClass(name);

            if (!hasSessionBeanInterface(home)) {
                
                VerificationEvent event = 
                        factory.createSpecViolationEvent(context, SECTION_6_5_1, name);
                
                context.fireBeanChecked(event);
                
                status = false;
            }
            
        }
        catch (ClassNotFoundException e) {
            
            VerificationEvent event = 
                    factory.createSpecViolationEvent(context, DTD_EJB_CLASS, name);
            
            context.fireBeanChecked(event);
            
            status = false;
        }
        
        return status;
    }
    
    
    /*
     * Finds java.ejb.SessionBean interface from the class
     */
    private boolean hasSessionBeanInterface(Class c) {

        Class[] interfaces = c.getInterfaces();
        
        for (int i = 0; i < interfaces.length; ++i) {
            
            if ((SESSIONBEAN_INTERFACE).equals(interfaces[i].getName()))
                return true;
        }
                
        return false;
    }


    /*
     * String constants
     */
    private final static String SESSIONBEAN_INTERFACE =
        "javax.ejb.SessionBean";

    public final static String SECTION_6_5_1         =
        "Section 6.5.1 Required Sessionbean interface";        
    
    public final static String DTD_EJB_CLASS         =
        "Deployment descriptor DTD: ejb-class";
        
}

