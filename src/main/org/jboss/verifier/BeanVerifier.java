package org.jboss.verifier;

/*
 * Class org.jboss.verifier.BeanVerifier
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
 * $Id: BeanVerifier.java,v 1.2 2000/06/03 21:43:56 juha Exp $
 */

 
// standard imports
import java.util.Iterator;
import java.io.File;
import java.net.URL;
import java.beans.beancontext.BeanContextServicesSupport;


// non-standard class dependencies
import org.jboss.verifier.strategy.VerificationContext;
import org.jboss.verifier.strategy.VerificationStrategy;
import org.jboss.verifier.strategy.EJBVerifier11;
import org.jboss.verifier.strategy.EJBVerifier20;

import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;
import org.jboss.verifier.event.VerificationEventFactory;
import org.jboss.verifier.event.VerificationEventGeneratorSupport;


import com.dreambean.ejx.ejb.EnterpriseBeans;
import com.dreambean.ejx.xml.ProjectX;
import com.dreambean.ejx.xml.XMLManager;

import org.jboss.ejb.deployment.jBossFileManager;
import org.jboss.ejb.deployment.jBossFileManagerFactory;
import org.jboss.ejb.deployment.jBossEjbJar;



/**
 * Attempts to verify the spec compliance of the beans in a given
 * EJB-JAR file. Works against EJB spec 1.1. Built for use in jBoss
 * project.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors (jplindfo@helsinki.fi)
 * @version $Revision: 1.2 $
 * @since  	JDK 1.3
 */
public class BeanVerifier implements VerificationContext {

    private jBossEjbJar ejbJar = null;
    private URL  ejbURL = null;
                                      
    private VerificationStrategy verifier = null;
                    
    /*
     * Support class which handles the event notification logic.
     */                    
    private VerificationEventGeneratorSupport events =
        new VerificationEventGeneratorSupport();
                              
    /*
     * Will make this look like a real object factory later on. Specify the
     * interface and add an example factory implementation.
     */
    private VerificationEventFactory eventFactory    =
        new VerificationEventFactory();
       
                           
    /*
     * Default constructor.
     */
    public BeanVerifier()   {  }
    
    
     
     
    /*
     * Checks the Enterprise Java Beans found in this Jar for EJB spec
     * compliance (EJB Spec. 1.1). Ensures that the given interfaces
     * and implementation classes implement required methods and follow
     * the contract given in the spec.
     */
    public void verify(URL url) {
        
        ejbURL = url;
        ejbJar = loadBeanJar(url);
         
        setVerifier(VERSION_1_1);
        
        
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        
        Iterator entities     = beans.getEntities();
        Iterator sessions     = beans.getSessions();
        
        verifier.checkEntities(entities);
        verifier.checkSessions(sessions);        
    }


    
   /*
    *************************************************************************
    *
    *   IMPLEMENTS VERIFICATION EVENT GENERATOR INTERFACE
    *
    *************************************************************************
    */    
    public void addVerificationListener(VerificationListener listener) {
        events.addVerificationListener(listener);
    }
    
    public void removeVerificationListener(VerificationListener listener) {
        events.removeVerificationListener(listener);
    }

    public void fireBeanChecked(VerificationEvent event) {
        events.fireBeanChecked(event);
    }

    
  /*
   **************************************************************************
   *
   *   IMPLEMENTS VERIFICATION CONTEXT INTERFACE
   *
   **************************************************************************
   */
    public jBossEjbJar getEJBJar() {
        return ejbJar;
    }
    
    public URL getJarLocation() {
        return ejbURL;
    }
    
    public String getEJBVersion() {
        return VERSION_1_1;
        
        // [TODO] fix this to return a correct version
    }

    
    
    
    /*
     * Will set the correct strategy implementation according to the supplied
     * version information. Might widen the scope to public, but protected
     * will do for now.
     */
    protected void setVerifier(String version) {
        
        if (VERSION_1_1.equals(version))
            verifier = new EJBVerifier11(this);
        
        else if (VERSION_2_0.equals(version))
            verifier = new EJBVerifier20(this);
            
        else 
            throw new IllegalArgumentException(UNRECOGNIZED_VERSION + ": " + version);
    }
    
    
    /*
     * accessor for reference to the verification strategy in use
     */
    protected VerificationStrategy getVerifier() {
        return verifier;
    }
    
    
    
    /**
     * Loads the bean jar file.
     *
     * @param   file    ejb jar
     */
    private jBossEjbJar loadBeanJar(URL file) {
        
        jBossFileManagerFactory factory = new jBossFileManagerFactory();
        BeanContextServicesSupport ctx  = new BeanContextServicesSupport();
        jBossFileManager fm             = (jBossFileManager)factory.createFileManager();
        
        ProjectX  xml  =  new ProjectX();

        ctx.addService(XMLManager.class, xml);
        ctx.add(fm);
        
        
        try {
            return fm.load(file);
        }
        catch (Exception e) {

            // [TODO] a generic exception is no good
            
            System.err.println(e);
            return null;
        }
    }
    
    
    /*
     * String constants
     */
    private final static String UNRECOGNIZED_VERSION =
        "Unknown version string";
        
}


