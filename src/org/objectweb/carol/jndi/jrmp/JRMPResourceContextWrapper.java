/*
 * Created on Oct 2, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.objectweb.carol.jndi.jrmp;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import org.objectweb.carol.jndi.reference.JNDIRemoteResource;
import org.objectweb.carol.jndi.reference.JNDIResourceWrapper;
import org.objectweb.carol.util.multi.ProtocolCurrent;

/**
 * @author riviereg
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JRMPResourceContextWrapper implements Context {

	/**
	 * the JRMP JNDI context
	 * @see #JRMPResourceContextWrapper
	 */
	 private static Context jrmpContext = null;
	
	/**
	 * the JRMP Wrapper JNDI context
	 * @see #JRMPResourceContextWrapper
	 */
	 private static Context single = null;
	 
	 
	/**
	 * the Exported Wrapper Hashtable
	 *
	 */
	private static Hashtable wrapperHash = null;

    
	/**
	 * Constructs an JRMP Wrapper context 
	 * @param jrmpContext the inital JRMP context
	 *
	 * @throws NamingException if a naming exception is encountered
	 */
	private JRMPResourceContextWrapper (Context jrmpCtx) throws NamingException {
	jrmpContext = jrmpCtx;
	wrapperHash = new Hashtable();
	}

	/**
	* 
	* @param o
	* @param name
	* @return
	* @throws NamingException
	*/
	public static Context getSingleInstance(Hashtable env) throws NamingException {
		if (single==null) {
			env.put("java.naming.factory.initial","com.sun.jndi.rmi.registry.RegistryContextFactory");
			single = new JRMPResourceContextWrapper (new InitialContext(env));
		}
		return single;
	}
	
	/**
	* 
	* @param jrmpCtx
	* @return the JRMP Wrapper JNDI Context
	* @throws NamingException
	*/
	public static Context getSingleInstance(Context jrmpCtx) throws NamingException {
		if (single==null) {
			single = new JRMPResourceContextWrapper (jrmpCtx);
		}
		return single;
	}

	/**
	 * Resolve a Remote Object: 
	 * If this object is a reference return the reference 
	 *
	 * @param o the object to resolve
	 * @return a <code>Referenceable ((JNDIRemoteReference)o).getReference()</code> if o is a JNDIRemoteReference
	 *         and the inititial object o if else
	 */
	private Object resolveObject(Object o, Name name) throws NamingException {
	try {
		//TODO: May we can do a narrow ? 
	    if (o instanceof JNDIRemoteResource) {
		return ((JNDIRemoteResource)o).getResource();
		} else {
		return o;
		}
	} catch (Exception e) {
		e.printStackTrace();
		throw new NamingException("" + e);
	}
	}

	/**
	 * Encode an Object :
	 * If the object is a reference wrap it into a JRMPResourceWrapper Object
	 * here the good way is to contact the carol configuration to get the jrmp
	 * protable remote object
	 *
	 * @param o the object to encode
	 * @return  a <code>Remote JNDIRemoteReference Object</code> if o is a ressource
	 *          o if else
	 */
	private Object encodeObject(Object o, Object name, boolean replace) throws NamingException {
	try {
        if ((!(o instanceof Remote)) && (!(o instanceof Referenceable)) 
        && (!(o instanceof Reference)) && (o instanceof Serializable)) {
		JNDIResourceWrapper irw =  new JNDIResourceWrapper((Serializable) o);
		ProtocolCurrent.getCurrent().getCurrentPortableRemoteObject().exportObject(irw);
		JNDIResourceWrapper oldObj = (JNDIResourceWrapper) wrapperHash.put(name, irw);
		if (oldObj != null) {
			if (replace) {
			ProtocolCurrent.getCurrent().getCurrentPortableRemoteObject().unexportObject(oldObj);
			} else {
			ProtocolCurrent.getCurrent().getCurrentPortableRemoteObject().unexportObject(irw);
			wrapperHash.put(name, oldObj);
			throw new NamingException("Object already bind");
			}
		} 
		return irw;
		} else {
		return o;
		}
	} catch (Exception e) {
		throw new NamingException("" +e);
	}
	}
    

// Context methods
// The Javadoc is deferred to the Context interface.
   
	public Object lookup(String name) throws NamingException {
	return resolveObject(jrmpContext.lookup(name), new CompositeName(name));
	}

	public Object lookup(Name name) throws NamingException {
	return resolveObject(jrmpContext.lookup(name), name);
	}

	public void bind(String name, Object obj) throws NamingException {
	jrmpContext.bind(name,encodeObject(obj, name, false));
	}

	public void bind(Name name, Object obj) throws NamingException {
	jrmpContext.bind(name,encodeObject(obj, name, false));
	}

	public void rebind(String name, Object obj) throws NamingException {
	jrmpContext.rebind(name,encodeObject(obj, name, true));
	}

	public void rebind(Name name, Object obj) throws NamingException {
	jrmpContext.rebind(name,encodeObject(obj, name, true));
	}

	public void unbind(String name) throws NamingException  {
	try {
		jrmpContext.unbind(name);
		if(wrapperHash.containsKey(name)){
		ProtocolCurrent.getCurrent().getCurrentPortableRemoteObject().unexportObject((Remote)wrapperHash.remove(name));
		}
	} catch (Exception e) {
		throw new NamingException("" +e);  
	}
	}

	public void unbind(Name name) throws NamingException  {
	try {
		jrmpContext.unbind(name);
		if(wrapperHash.containsKey(name)){
		ProtocolCurrent.getCurrent().getCurrentPortableRemoteObject().unexportObject((Remote)wrapperHash.remove(name));
		}
	} catch (Exception e) {
		throw new NamingException("" +e);  
	}
	}

	public void rename(String oldName, String newName) throws NamingException {	
	if(wrapperHash.containsKey(oldName)){
		wrapperHash.put( newName, wrapperHash.remove(oldName));
	}
	jrmpContext.rename(oldName, newName);
	}

	public void rename(Name oldName, Name newName) throws NamingException  {
	if(wrapperHash.containsKey(oldName)){
		wrapperHash.put(newName, wrapperHash.remove(oldName));
	}
	jrmpContext.rename(oldName, newName);	
	}

	public NamingEnumeration list(String name) throws NamingException {
	return jrmpContext.list(name);
	}

	public NamingEnumeration list(Name name) throws NamingException  {
	return jrmpContext.list(name);
	}

	public NamingEnumeration listBindings(String name)
		throws NamingException  {
	return jrmpContext.listBindings(name);
	}

	public NamingEnumeration listBindings(Name name)
		throws NamingException  {
	return jrmpContext.listBindings(name);
	}

	public void destroySubcontext(String name) throws NamingException  {
	jrmpContext.destroySubcontext(name);	     
	}

	public void destroySubcontext(Name name) throws NamingException  {
	jrmpContext.destroySubcontext(name);	
	}

	public Context createSubcontext(String name) throws NamingException  {
	return jrmpContext.createSubcontext(name);
	}

	public Context createSubcontext(Name name) throws NamingException  {
	return jrmpContext.createSubcontext(name);
	}

	public Object lookupLink(String name) throws NamingException  {
	return jrmpContext.lookupLink(name);
	}

	public Object lookupLink(Name name) throws NamingException {
	return jrmpContext.lookupLink(name);
	}

	public NameParser getNameParser(String name) throws NamingException {
	return jrmpContext.getNameParser(name);
	} 

	public NameParser getNameParser(Name name) throws NamingException {
	return jrmpContext.getNameParser(name);
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
	return jrmpContext.addToEnvironment(propName, propVal);
	}

	public Object removeFromEnvironment(String propName) 
		throws NamingException {
	return jrmpContext.removeFromEnvironment(propName);
	}

	public Hashtable getEnvironment() throws NamingException {
	return jrmpContext.getEnvironment();
	}

	public void close() throws NamingException {
	// do nothing for the moment
	}

	public String getNameInNamespace() throws NamingException {
	return jrmpContext.getNameInNamespace();
	}	    

}
