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
	try {
	    pcur = ProtocolCurrent.getCurrent();
	    activesInitialsContexts = pcur.getContextHashtable();
	} catch (Exception e) {
	    throw new NamingException("multi initial context init fail:\n" +
				      getProperties() 
				      + e);
	}
    }


    // Inital context wrapper see the Context documentation for this methods
    public Object lookup(String name) throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().lookup(name);
	} catch (Exception e) {
	    throw new NamingException("lookup fail:\n" +
				      getProperties() 
				      + e);
	}
	
    }
    
    public Object lookup(Name name) throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().lookup(name);
	} catch (Exception e) {
	    throw new NamingException("lookup fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public void bind(String name, Object obj) throws NamingException {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).bind(name, obj);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("bind fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public void bind(Name name, Object obj) throws NamingException {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).bind(name, obj);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("bind fail:\n"  +
				      getProperties() 
				      + e);
	}
    }

    public void rebind(String name, Object obj) throws NamingException {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);
		((Context)activesInitialsContexts.get(rmiName)).rebind(name, obj);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("rebind fail:\n" + 
				      getProperties() 
				      + e);
	}
    }

    public void rebind(Name name, Object obj) throws NamingException {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).rebind(name, obj);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("rebind fail:\n"  +
				      getProperties() 
				      + e);
	}	
    }
    
    public void unbind(String name) throws NamingException  {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).unbind(name);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("unbind fail:\n"  +
				      getProperties() 
				      + e);
	}	
    }

    public void unbind(Name name) throws NamingException  {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).unbind(name);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("unbind fail:\n" + 
				      getProperties() 
				      + e);
	}	
    }

    public void rename(String oldName, String newName) throws NamingException {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).rename(oldName, newName);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("rename fail:\n" + 
				      getProperties() 
				      + e);
	}	
    }

    public void rename(Name oldName, Name newName) throws NamingException  {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).rename(oldName, newName);	
		pcur.setDefault();
	    }
	} catch (Exception e) {
	    throw new NamingException("rename fail:\n" + 
				      getProperties() 
				      + e);
	}	
    }
    
    public NamingEnumeration list(String name) throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().list(name);
	} catch (Exception e) {
	    throw new NamingException("list fail:\n" + 
				      getProperties() 
				      + e);
	}
    }
    
    public NamingEnumeration list(Name name) throws NamingException  {
	try {
	    return pcur.getCurrentInitialContext().list(name);
	} catch (Exception e) {
	    throw new NamingException("list fail:\n" + 
				      getProperties() 
				      + e);
	}
    }
    
    public NamingEnumeration listBindings(String name)
	throws NamingException  {
	try {
	    return pcur.getCurrentInitialContext().listBindings(name);
	} catch (Exception e) {
	    throw new NamingException("list fail:\n" + 
				      getProperties() 
				      + e);
	}
    }
    
    public NamingEnumeration listBindings(Name name)
	throws NamingException  {
	try {
	    return pcur.getCurrentInitialContext().listBindings(name);
	} catch (Exception e) {
	    throw new NamingException(" listBindings fail:\n"  +
				      getProperties() 
				      + e);
	}
    }

    public void destroySubcontext(String name) throws NamingException  {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).destroySubcontext(name);	
		pcur.setDefault();
	    } 
	} catch (Exception e) {
	    throw new NamingException("destroySubcontext fail:\n" + 
				      getProperties() 
				      + e);
	}   
    }

    public void destroySubcontext(Name name) throws NamingException  {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).destroySubcontext(name);	
		pcur.setDefault();
	    }  
	} catch (Exception e) {
	    throw new NamingException("destroySubcontext fail:\n" + 
				      getProperties() 
				      + e);
	} 
    }
    
    public Context createSubcontext(String name) throws NamingException  {
	try {
	    return pcur.getCurrentInitialContext().createSubcontext(name);
	} catch (Exception e) {
	    throw new NamingException("createSubcontext fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public Context createSubcontext(Name name) throws NamingException  {
	try {
	    return pcur.getCurrentInitialContext().createSubcontext(name);
	} catch (Exception e) {
	    throw new NamingException("createSubcontext fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public Object lookupLink(String name) throws NamingException  {
	try {
	    return pcur.getCurrentInitialContext().lookupLink(name);
	} catch (Exception e) {
	    throw new NamingException("lookupLink fail:\n"  +
				      getProperties() 
				      + e);
	}
    }

    public Object lookupLink(Name name) throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().lookupLink(name);
	} catch (Exception e) {
	    throw new NamingException("lookupLink fail:\n"  +
				      getProperties() 
				      + e);
    }
    }

    public NameParser getNameParser(String name) throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().getNameParser(name);
	} catch (Exception e) {
	    throw new NamingException("getNameParser fail:\n" + 
				      getProperties() 
				      + e);
	}
    } 
    
    public NameParser getNameParser(Name name) throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().getNameParser(name);
	} catch (Exception e) {
	    throw new NamingException("getNameParser fail:\n" + 
				      getProperties() 
				      + e);
	}
    }
    
    public String composeName(String name, String prefix)
	throws NamingException {
	return name;
    }
    
    public Name composeName(Name name, Name prefix) throws NamingException {
	try {
	    return (Name)name.clone();
	} catch (Exception e) {
	    throw new NamingException("composeName fail:\n"  +
				      getProperties() 
				      + e);
	}
    }

    public Object addToEnvironment(String propName, Object propVal) 
	throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().addToEnvironment(propName, propVal);
	} catch (Exception e) {
	    throw new NamingException("addToEnvironment fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public Object removeFromEnvironment(String propName) 
	throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().removeFromEnvironment(propName);
	} catch (Exception e) {
	    throw new NamingException("removeFromEnvironment fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public Hashtable getEnvironment() throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().getEnvironment();
	} catch (Exception e) {
	    throw new NamingException("getEnvironment fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public void close() throws NamingException {
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).close();	
		pcur.setDefault();
	    } 
	} catch (Exception e) {
	    throw new NamingException("close fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    public String getNameInNamespace() throws NamingException {
	try {
	    return pcur.getCurrentInitialContext().getNameInNamespace();
	} catch (Exception e) {
	    throw new NamingException("getNameInNamespace fail:\n"  +
				      getProperties() 
				      + e);
	}
    }
    
    /**
     * Private method for building property string
     */
    private String getProperties() {
	try {
	    return "CAROL configuration:\n" 
		+ org.objectweb.carol.util.configuration.CommunicationConfiguration.getAllRMIConfiguration() 
		+" Classpath: " + System.getProperty("java.class.path") + "\n";
	} catch (Exception e) {
	    return "Can't check CAROL configuration:\n" + e 
		+" Classpath: " + System.getProperty("java.class.path") + "\n";
	}
    }
}
