/*
 * @(#) MultiOrbInitialContext.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 *
 */
package org.objectweb.carol.jndi.spi;

//java import
import java.util.Hashtable;
import java.util.Enumeration;

//javax import
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.Context;
import javax.naming.NameParser;

//carol import
import org.objectweb.carol.util.multi.ProtocolCurrent;

/*
 * Class <code>MultiOrbInitialContext</code> is the CAROL JNDI SPI Context for multi Context management.
 * this class use the protocol current for management of multi protocol 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see javax.naming.Context
 * @see javax.naming.InitialContext
 * @see org.objectweb.util.multi.ProtocolCurrent
 * @version 1.0, 15/07/2002
 */
public class MultiOrbInitialContext implements Context {

    /**
     * Active Contexts, this variable is just a cache of the protocol current context array 
     */
    private static Hashtable activesInitialsContexts = null;

    /**
     * The ProtocolCurrent for management of active Context
     */
     private static ProtocolCurrent pcur = null; 

    /**
     * String for rmi name 
     */
    private static String rmiName = null;
    
    /**
     * Constructor,
     * load communication framework
     * and instaciate initial contexts
     */
    public MultiOrbInitialContext () throws NamingException {
	pcur = ProtocolCurrent.getCurrent();
	activesInitialsContexts = pcur.getContextHashtable();
    }


    // Inital context wrapper see the Context documentation for this methods
    public Object lookup(String name) throws NamingException {
	return pcur.getCurrentInitialContext().lookup(name);
    }

    public Object lookup(Name name) throws NamingException {
	return pcur.getCurrentInitialContext().lookup(name);
    }

    public void bind(String name, Object obj) throws NamingException {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).bind(name, obj);	
	    pcur.setDefault();
	}
    }

    public void bind(Name name, Object obj) throws NamingException {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).bind(name, obj);	
	    pcur.setDefault();
	}
    }

    public void rebind(String name, Object obj) throws NamingException {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);
	    ((Context)activesInitialsContexts.get(rmiName)).rebind(name, obj);	
	    pcur.setDefault();
	}
    }

    public void rebind(Name name, Object obj) throws NamingException {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).rebind(name, obj);	
	    pcur.setDefault();
	}	
    }

    public void unbind(String name) throws NamingException  {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).unbind(name);	
	    pcur.setDefault();
	}	
    }

    public void unbind(Name name) throws NamingException  {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).unbind(name);	
	    pcur.setDefault();
	}	
    }

    public void rename(String oldName, String newName) throws NamingException {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).rename(oldName, newName);	
	    pcur.setDefault();
	}	
    }

    public void rename(Name oldName, Name newName) throws NamingException  {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).rename(oldName, newName);	
	    pcur.setDefault();
	}	
    }

    public NamingEnumeration list(String name) throws NamingException {
	return pcur.getCurrentInitialContext().list(name);
    }

    public NamingEnumeration list(Name name) throws NamingException  {
	return pcur.getCurrentInitialContext().list(name);
    }

    public NamingEnumeration listBindings(String name)
	    throws NamingException  {
	return pcur.getCurrentInitialContext().listBindings(name);
    }

    public NamingEnumeration listBindings(Name name)
	    throws NamingException  {
	return pcur.getCurrentInitialContext().listBindings(name);
    }

    public void destroySubcontext(String name) throws NamingException  {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).destroySubcontext(name);	
	    pcur.setDefault();
	}    
    }

    public void destroySubcontext(Name name) throws NamingException  {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).destroySubcontext(name);	
	    pcur.setDefault();
	}   
    }

    public Context createSubcontext(String name) throws NamingException  {
	return pcur.getCurrentInitialContext().createSubcontext(name);
    }

    public Context createSubcontext(Name name) throws NamingException  {
	return pcur.getCurrentInitialContext().createSubcontext(name);
    }

    public Object lookupLink(String name) throws NamingException  {
	return pcur.getCurrentInitialContext().lookupLink(name);
    }

    public Object lookupLink(Name name) throws NamingException {
	return pcur.getCurrentInitialContext().lookupLink(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
	return pcur.getCurrentInitialContext().getNameParser(name);
    } 

    public NameParser getNameParser(Name name) throws NamingException {
	return pcur.getCurrentInitialContext().getNameParser(name);
    }

    public String composeName(String name, String prefix)
	    throws NamingException {
	return name;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
	return (Name)name.clone();
    }

    public Object addToEnvironment(String propName, Object propVal) 
	    throws NamingException {
	return pcur.getCurrentInitialContext().addToEnvironment(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) 
	    throws NamingException {
	return pcur.getCurrentInitialContext().removeFromEnvironment(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
	return pcur.getCurrentInitialContext().getEnvironment();
    }

    public void close() throws NamingException {
	for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
	    rmiName =  (String)e.nextElement();
	    pcur.setRMI(rmiName);	    
	    ((Context)activesInitialsContexts.get(rmiName)).close();	
	    pcur.setDefault();
	} 
    }

    public String getNameInNamespace() throws NamingException {
	return pcur.getCurrentInitialContext().getNameInNamespace();
    }	    
}
