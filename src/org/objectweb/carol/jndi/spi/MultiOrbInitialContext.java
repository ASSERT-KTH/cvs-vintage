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
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.carol.util.configuration.TraceCarol;
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
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.MultiOrbInitialContext()");
        }
	try {
	    pcur = ProtocolCurrent.getCurrent();
	    activesInitialsContexts = pcur.getContextHashtable();
	} catch (Exception e) {
	    String msg = "MultiOrbInitialContext.MultiOrbInitialContext() failed: " + e; 
	    TraceCarol.error(msg,e);
	    throw new NamingException(msg);
	}
    }


    // Inital context wrapper see the Context documentation for this methods
    public Object lookup(String name) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.lookup(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().lookup(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.lookup(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw new NamingException(msg);
	}
	
    }
    
    public Object lookup(Name name) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.lookup(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().lookup(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.lookup(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }
    
    public void bind(String name, Object obj) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.bind(\""+name+"\","+simpleClass(obj.getClass().getName())+" object)");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).bind(name, obj);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.bind(String name, Object obj) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }
    
    public void bind(Name name, Object obj) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.bind(\""+name+"\","+simpleClass(obj.getClass().getName())+" object)");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).bind(name, obj);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.bind(Name name, Object obj) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }

    public void rebind(String name, Object obj) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.rebind(\""+name+"\","+simpleClass(obj.getClass().getName())+" object)");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);
		((Context)activesInitialsContexts.get(rmiName)).rebind(name, obj);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.rebind(String name, Object obj) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }

    public void rebind(Name name, Object obj) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.rebind(\""+name+"\","+simpleClass(obj.getClass().getName())+" object)");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).rebind(name, obj);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.rebind(Name name, Object obj) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}	
    }
    
    public void unbind(String name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.unbind(\""+name+"\")");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).unbind(name);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.unbind(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}	
    }

    public void unbind(Name name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.unbind(\""+name+"\")");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).unbind(name);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.unbind(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}	
    }

    public void rename(String oldName, String newName) throws NamingException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.rename(\""+oldName+"\",\""+newName+"\")");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).rename(oldName, newName);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.rename(String oldName, String newName) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}	
    }

    public void rename(Name oldName, Name newName) throws NamingException  {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.rename(\""+oldName+"\",\""+newName+"\")");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).rename(oldName, newName);	
		pcur.setDefault();
	    }
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.rename(Name oldName, Name newName) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}	
    }
    
    public NamingEnumeration list(String name) throws NamingException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.list(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().list(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.list(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }
    
    public NamingEnumeration list(Name name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.list(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().list(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.list(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }
    
    public NamingEnumeration listBindings(String name)
	throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.listBindings(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().listBindings(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.listBindings(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }
    
    public NamingEnumeration listBindings(Name name)
	throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.listBindings(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().listBindings(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.listBindings(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }

    public void destroySubcontext(String name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.destroySubcontext(\""+name+"\")");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).destroySubcontext(name);	
		pcur.setDefault();
	    } 
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.destroySubcontext(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}   
    }

    public void destroySubcontext(Name name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.destroySubcontext(\""+name+"\")");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).destroySubcontext(name);	
		pcur.setDefault();
	    }  
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.destroySubcontext(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    
	    throw e;
	} 
    }
    
    public Context createSubcontext(String name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.createSubcontext(\""+name+"\")");
        }
	try {
	    return pcur.getCurrentInitialContext().createSubcontext(name);
	} catch (NamingException e) {
	    String msg ="MultiOrbInitialContext.createSubcontext(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }
    
    public Context createSubcontext(Name name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.createSubcontext(\""+name+"\")");
        }
	try {
	    return pcur.getCurrentInitialContext().createSubcontext(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.createSubcontext(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    
	    throw e;
	}
    }
    
    public Object lookupLink(String name) throws NamingException  {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.lookupLink(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().lookupLink(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.lookupLink(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
	    throw e;
	}
    }

    public Object lookupLink(Name name) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.lookupLink(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().lookupLink(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.lookupLink(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }

    public NameParser getNameParser(String name) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.getNameParser(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().getNameParser(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.getNameParser(String name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    } 
    
    public NameParser getNameParser(Name name) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.getNameParser(\""+name+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().getNameParser(name);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.getNameParser(Name name) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }
    
    public String composeName(String name, String prefix)
	throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.composeName("+name+","+prefix+")/rmi name="+pcur.getCurrentRMIName());
        }
	return name;
    }
    
    public Name composeName(Name name, Name prefix) throws NamingException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.composeName(\""+name+","+prefix+"\")/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	return (Name)name.clone();
    }

    public Object addToEnvironment(String propName, Object propVal) 
	throws NamingException {
	try {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.addToEnvironment(\""+propName+"\","+simpleClass(propVal.getClass().getName())+" object)");
        }
	    return pcur.getCurrentInitialContext().addToEnvironment(propName, propVal);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.addToEnvironment(String propName, Object propVal)  failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }
    
    public Object removeFromEnvironment(String propName) 
	throws NamingException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.removeFromEnvironment(\""+propName+"\")");
        }
	try {
	    return pcur.getCurrentInitialContext().removeFromEnvironment(propName);
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.removeFromEnvironment(String propName) failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }
    
    public Hashtable getEnvironment() throws NamingException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.getEnvironment()/rmi name=\""+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().getEnvironment();
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.getEnvironment() failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }
    
    public void close() throws NamingException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.close()");
        }
	try {
	    for (Enumeration e = activesInitialsContexts.keys() ; e.hasMoreElements() ;) {
		rmiName =  (String)e.nextElement();
		pcur.setRMI(rmiName);	    
		((Context)activesInitialsContexts.get(rmiName)).close();	
		pcur.setDefault();
	    } 
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.close() failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }
    
    public String getNameInNamespace() throws NamingException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiOrbInitialContext.getNameInNamespace()/rmi name="+pcur.getCurrentRMIName()+"\"");
        }
	try {
	    return pcur.getCurrentInitialContext().getNameInNamespace();
	} catch (NamingException e) {
	    String msg = "MultiOrbInitialContext.getNameInNamespace() failed: " + e; 
	    TraceCarol.debugJndiCarol("Error: " + msg + " " + e);	    
	    throw e;
	}
    }

    /**
     * Just the name of the class without the package
     */
    private String simpleClass(String c) {
	return c.substring(c.lastIndexOf('.') +1);
    }
}
