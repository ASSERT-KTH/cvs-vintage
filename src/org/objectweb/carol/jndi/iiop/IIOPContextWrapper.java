/*
 * @(#)IIOPContextWrapper.java	1.1 02/07/15
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
 */
package org.objectweb.carol.jndi.iiop;

// java import 
import java.io.Serializable;
import java.util.Hashtable;
import java.rmi.Remote;

// javax import
import javax.naming.Context; 
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Referenceable;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

// carol import 
import org.objectweb.carol.util.multi.ProtocolCurrent;
import org.objectweb.carol.util.configuration.TraceCarol;

/*
 * Class <code>IIOPRemoteContextWrapper</code> is the CAROL JNDI Context. This context make the 
 * iiop referenceable reference wrapping only from a remote object.
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see javax.naming.Context
 * @version 1.1, 15/07/2002
 */
public class IIOPContextWrapper implements Context {
    
    /**
     * the IIOP JNDI context
     * @see #IIOPContextWrapper
     */
     private static Context iiopContext = null;

    
    /**
     * Constructs an IIOP Wrapper context 
     * @param iiopContext the inital IIOP context
     *
     * @throws NamingException if a naming exception is encountered
     */
    public IIOPContextWrapper (Context iiopContext ) throws NamingException {
	this.iiopContext = iiopContext;
    }


    /**
     * Resolve a Remote Object: 
     * If this object is a reference return the reference 
     *
     * @param o the object to resolve
     * @return a <code>Referenceable ((IIOPRemoteReference)o).getReference()</code> if o is a IIOPRemoteReference
     *         and the inititial object o if else
     */
    private Object resolveObject(Object o) {
	try {
	    if (o instanceof IIOPRemoteReference) {
		// build of the Referenceable object with is Reference
		Reference objRef = ((IIOPRemoteReference)o).getReference();
		ObjectFactory objFact = (ObjectFactory)(Class.forName(objRef.getFactoryClassName())).newInstance(); 
		return (Referenceable)objFact.getObjectInstance(objRef,null,null,null);
	    } else if (o instanceof IIOPRemoteResource) {
		return ((IIOPRemoteResource)o).getResource();
	    } else {
		return o;
	    }
	} catch (Exception e) {	    
	    TraceCarol.error("IIOPContextWrapper.resolveObject()", e);
	    return o;
	}
    }
    

// Context methods
// The Javadoc is deferred to the Context interface.
   
    public Object lookup(String name) throws NamingException {
	return resolveObject(iiopContext.lookup(name));
    }

    public Object lookup(Name name) throws NamingException {
	return resolveObject(iiopContext.lookup(name));
    }

    public void bind(String name, Object obj) throws NamingException {
	iiopContext.bind(name,obj);
    }

    public void bind(Name name, Object obj) throws NamingException {
	iiopContext.bind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
	iiopContext.rebind(name,obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
	iiopContext.rebind(name,obj);
    }

    public void unbind(String name) throws NamingException  {
	    iiopContext.unbind(name);
    }

    public void unbind(Name name) throws NamingException  {
	    iiopContext.unbind(name);
    }

    public void rename(String oldName, String newName) throws NamingException {	
	iiopContext.rename(oldName, newName);
    }

    public void rename(Name oldName, Name newName) throws NamingException  {
	iiopContext.rename(oldName, newName);	
    }

    public NamingEnumeration list(String name) throws NamingException {
	return iiopContext.list(name);
    }

    public NamingEnumeration list(Name name) throws NamingException  {
	return iiopContext.list(name);
    }

    public NamingEnumeration listBindings(String name)
	    throws NamingException  {
	return iiopContext.listBindings(name);
    }

    public NamingEnumeration listBindings(Name name)
	    throws NamingException  {
	return iiopContext.listBindings(name);
    }

    public void destroySubcontext(String name) throws NamingException  {
	iiopContext.destroySubcontext(name);	     
    }

    public void destroySubcontext(Name name) throws NamingException  {
	iiopContext.destroySubcontext(name);	
    }

    public Context createSubcontext(String name) throws NamingException  {
	return iiopContext.createSubcontext(name);
    }

    public Context createSubcontext(Name name) throws NamingException  {
	return iiopContext.createSubcontext(name);
    }

    public Object lookupLink(String name) throws NamingException  {
	return iiopContext.lookupLink(name);
    }

    public Object lookupLink(Name name) throws NamingException {
	return iiopContext.lookupLink(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
	return iiopContext.getNameParser(name);
    } 

    public NameParser getNameParser(Name name) throws NamingException {
	return iiopContext.getNameParser(name);
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
	return iiopContext.addToEnvironment(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) 
	    throws NamingException {
	return iiopContext.removeFromEnvironment(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
	return iiopContext.getEnvironment();
    }

    public void close() throws NamingException {
	iiopContext.close();
    }

    public String getNameInNamespace() throws NamingException {
	return iiopContext.getNameInNamespace();
    }	    
}
