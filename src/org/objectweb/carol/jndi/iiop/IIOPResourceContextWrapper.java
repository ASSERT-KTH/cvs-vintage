/*
 * @(#)IIOPRessourceContextWrapper.java	1.0 02/07/15
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
import java.rmi.Remote;
import java.util.Hashtable;

// javax import
import javax.naming.Context; 
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

// carol import 
import  org.objectweb.carol.util.multi.ProtocolCurrent;

/*
 * Class <code>IIOPRemoteResourceContextWrapper</code> is the CAROL JNDI Context. This context make the 
 * iiop serializable resource wrapping to/from a remote object
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see javax.naming.Context
 * @version 1.0, 15/07/2002
 */
public class IIOPResourceContextWrapper implements Context {
    
    /**
     * the IIOP JNDI context
     * @see #IIOPResourceContextWrapper
     */
     private static Context iiopContext = null; 

    
    /**
     * Constructs an IIOP Wrapper context 
     * @param iiopContext the inital IIOP context
     *
     * @throws NamingException if a naming exception is encountered
     */
    public IIOPResourceContextWrapper (Context iiopContext ) throws NamingException {
	this.iiopContext = iiopContext;
    }


    /**
     * Resolve a Remote Object: 
     * If this object is a resource return the resource 
     *
     * @param o the object to resolve
     * @return a <code>Serializable ((IIOPRemoteResource)o).getResource()</code> if o is a IIOPRemoteResource
     *         and the inititial object o if else
     */
    private Object resolveObject(Object o) {
	try {
	    if (o instanceof IIOPRemoteResource) {
		return ((IIOPRemoteResource)o).getResource();
	    } else {
		return o;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return o;
	}
    }

    /**
     * Encode an Object :
     * If the object is a resource wrap it into a IIOPResourceWrapper Object
     * here the good way is to contact the carol configuration to get the iiop
     * protable remote object
     *
     * @param o the object to encode
     * @return  a <code>Remote IIOPRemoteResource Object</code> if o is a ressource
     *          o if else
     */
    private Object encodeObject(Object o) {
	try {
	    if ((!(o instanceof Remote)) && (o instanceof Serializable)) {
		IIOPResourceWrapper irw =  new IIOPResourceWrapper((Serializable) o);
		ProtocolCurrent.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
		return irw;
	    } else {
		return o;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
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
	iiopContext.bind(name,encodeObject(obj));
    }

    public void bind(Name name, Object obj) throws NamingException {
	iiopContext.bind(name,encodeObject(obj));
    }

    public void rebind(String name, Object obj) throws NamingException {
	iiopContext.rebind(name,encodeObject(obj));
    }

    public void rebind(Name name, Object obj) throws NamingException {
	iiopContext.rebind(name,encodeObject(obj));
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
