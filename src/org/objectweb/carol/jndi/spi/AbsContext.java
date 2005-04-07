/**
 * Copyright (C) 2005 - Bull S.A.
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
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
 * --------------------------------------------------------------------------
 * $Id: AbsContext.java,v 1.4 2005/04/07 15:07:08 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

import java.rmi.Remote;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.carol.jndi.wrapping.JNDIRemoteResource;
import org.objectweb.carol.rmi.exception.NamingExceptionHelper;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * This abstract class define the common methods used for all existing protocol
 * implementation of their SPI context. When integrating a new protocol, define
 * a new SPI class extending this one. You only have to focus on specific
 * implementation and let in major case all default implementation.
 * @author Florent Benoit
 */
public abstract class AbsContext implements Context {

    /**
     * The wrapping context (should be protocol context)
     */
    private Context wrappedContext = null;

    /**
     * Exported Wrapper Hashtable
     */
    private Hashtable exportedObjects = new Hashtable();

    /**
     * Constructs a wrapper on the given protocol context
     * @param ctx the given context (could be jrmp context, jeremie context,
     *        iiop context,...).
     */
    protected AbsContext(Context ctx) {
        this.wrappedContext = ctx;
    }

    /**
     * If this object is a reference wrapper return the reference If this object
     * is a resource wrapper return the resource
     * @param o the object to resolve
     * @param name name of the object to unwrap
     * @return the unwrapped object
     * @throws NamingException if the object cannot be unwraped
     */
    protected abstract Object unwrapObject(Object o, Name name) throws NamingException;

    /**
     * Wrap an Object : If the object is a reference wrap it into a Reference
     * Wrapper Object here the good way is to contact the carol configuration to
     * get the portable remote object
     * @param o the object to encode
     * @param name of the object
     * @param replace if the object need to be replaced
     * @return a <code>Remote JNDIRemoteReference Object</code> if o is a
     *         resource o if else
     * @throws NamingException if object cannot be wrapped
     */
    protected abstract Object wrapObject(Object o, Name name, boolean replace) throws NamingException;

    /**
     * Default implementation of unwrapObject method If this object is a
     * reference wrapper return the reference If this object is a resource
     * wrapper return the resource
     * @param o the object to resolve
     * @param name name of the object to unwrap
     * @return the unwrapped object
     * @throws NamingException if the object cannot be unwraped
     */
    protected Object defaultUnwrapObject(Object o, Name name) throws NamingException {
        try {
            if (o instanceof JNDIRemoteResource) {
                return ((JNDIRemoteResource) o).getResource();
            } else {
                return o;
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot unwrap object '" + o + "' with name '" + name + "' :"
                    + e.getMessage(), e);
        }
    }

    /**
     * Get the object port to use (0 by default, declare this method in a subclass if you want to change value)
     * @return object port to use
     */
    protected int getObjectPort() {
        return 0;

    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        if (name.isEmpty()) {
            return this;
        }
        return unwrapObject(wrappedContext.lookup(encode(name)), name);
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot bind empty name");
        }
        wrappedContext.bind(encode(name), wrapObject(obj, name, false));
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding. All
     * intermediate contexts and the target context (that named by all but
     * terminal atomic component of the name) must already exist.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(Name name, Object obj) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot rebind empty name");
        }
        wrappedContext.rebind(encode(name), wrapObject(obj, name, true));
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);
    }

    /**
     * Unbinds the named object. Removes the terminal atomic name in
     * <code>name</code> from the target context--that named by all but the
     * terminal atomic part of <code>name</code>.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(Name name) throws NamingException {
        if (name.isEmpty()) {
            throw new NamingException("Cannot unbind empty name");
        }
        try {
            wrappedContext.unbind(encode(name));
            if (exportedObjects.containsKey(name)) {
                ConfigurationRepository.getCurrentConfiguration().getProtocol().getPortableRemoteObject().unexportObject(
                        (Remote) exportedObjects.remove(name));
            }
        } catch (Exception e) {
            throw NamingExceptionHelper.create("Cannot unbind name '" + name + "' : " + e.getMessage(), e);
        }
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name. Both names are relative to this context. Any attributes associated
     * with the old name become associated with the new name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(Name oldName, Name newName) throws NamingException {
        if (exportedObjects.containsKey(oldName)) {
            exportedObjects.put(newName, exportedObjects.remove(oldName));
        }
        wrappedContext.rename(encode(oldName), encode(newName));
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(String oldName, String newName) throws NamingException {
        rename(new CompositeName(oldName), new CompositeName(newName));
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. The contents of any subcontexts are not
     * included.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         <tt>NameClassPair</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(Name name) throws NamingException {
        return new WrappedEnumeration(wrappedContext.list(encode(name)));
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         <tt>NameClassPair</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(String name) throws NamingException {
        return list(new CompositeName(name));
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them. The contents of any subcontexts are not included.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type <tt>Binding</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        return new WrappedEnumeration(wrappedContext.listBindings(encode(name)));
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type <tt>Binding</tt>.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }

    /**
     * Destroys the named context and removes it from the namespace. Any
     * attributes associated with the name are also removed. Intermediate
     * contexts are not destroyed.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(Name name) throws NamingException {
        wrappedContext.destroySubcontext(encode(name));
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(new CompositeName(name));
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(Name name) throws NamingException {
        return wrappedContext.createSubcontext(encode(name));
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompositeName(name));
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>, not following the terminal
     *         link (if any).
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(Name name) throws NamingException {
        return wrappedContext.lookupLink(encode(name));
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>, not following the terminal
     *         link (if any)
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(new CompositeName(name));
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(Name name) throws NamingException {
        return wrappedContext.getNameParser(encode(name));
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(new CompositeName(name));
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws NamingException if a naming exception is encountered
     */
    public String composeName(String name, String prefix) throws NamingException {
        return name;
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        return (Name) name.clone();
    }

    /**
     * Adds a new environment property to the environment of this context. If
     * the property already exists, its value is overwritten. See class
     * description for more details on environment properties.
     * @param propName the name of the environment property to add; may not be
     *        null
     * @param propVal the value of the property to add; may not be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment before
     * @throws NamingException if a naming exception is encountered
     */
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return wrappedContext.addToEnvironment(propName, propVal);
    }

    /**
     * Removes an environment property from the environment of this context. See
     * class description for more details on environment properties.
     * @param propName the name of the environment property to remove; may not
     *        be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment
     * @throws NamingException if a naming exception is encountered
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        return wrappedContext.removeFromEnvironment(propName);
    }

    /**
     * Retrieves the environment in effect for this context. See class
     * description for more details on environment properties.
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable getEnvironment() throws NamingException {
        return wrappedContext.getEnvironment();
    }

    /**
     * Closes this context. This method releases this context's resources
     * immediately, instead of waiting for them to be released automatically by
     * the garbage collector.
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        // do nothing for the moment
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     * @throws NamingException if a naming exception is encountered
     */
    public String getNameInNamespace() throws NamingException {
        return wrappedContext.getNameInNamespace();
    }

    /**
     * Adds a new object to the list of exported objects
     * @param name JNDI name of the exported object
     * @param o exported object
     * @return Returns the wrapperHash.
     */
    protected Object addToExported(Name name, Object o) {
        return exportedObjects.put(name, o);
    }

    /**
     * @return the wrappedContext.
     */
    protected Context getWrappedContext() {
        return wrappedContext;
    }

    /**
     * Hide special characters from flat namespace registry. Escape forward and
     * backward slashes, and leading quote character.
     * @param initialName the name to encode
     * @return the encoded name
     */
    protected Name encode(Name initialName) {
        String name = initialName.toString();

        // nothing to encode
        if (name.length() < 1) {
            return initialName;
        }
        // replace all / and \ by adding a \\ prefix
        StringBuffer newname = new StringBuffer(name);
        int i = 0;
        while (i < newname.length()) {
            char c = newname.charAt(i);
            if (c == '/' || c == '\\') {
                newname.insert(i, '\\');
                i++;
            }
            i++;
        }
        // prefix quote characters
        if (newname.charAt(0) == '"' || newname.charAt(0) == '\'') {
            newname.insert(0, '\\');
        }

        // return encoded name
        try {
            return new CompositeName(newname.toString());
        } catch (InvalidNameException e) {
            return initialName;
        }
    }

    /**
     * undo what encode() does
     * @param name to decode
     * @return decoded String from the given encoding string
     */
    protected String decode(String name) {
        StringBuffer newname = new StringBuffer(name);
        // we have a quoted string : remove the enclosing quotes
        if (newname.length() >= 2 && (newname.charAt(0) == '"' || newname.charAt(0) == '\'')
                && newname.charAt(0) == newname.charAt(newname.length() - 1)) {
            newname.deleteCharAt(0);
            newname.deleteCharAt(newname.length() - 1);
        }
        // nothing to decode, return it directly
        if (name.indexOf('\\') < 0) {
            return newname.toString();
        }
        // Remove all \\ prefix
        int i = 0;
        while (i < newname.length()) {
            if (newname.charAt(i) == '\\') {
                newname.deleteCharAt(i);
                i++;
                continue;
            }
            i++;
        }

        return newname.toString();
    }

    /**
     * This class is used to return a naming enumeration by decoding encoded
     * names
     */
    protected class WrappedEnumeration implements NamingEnumeration {

        /**
         * The existing enumeration which contains encoded names to decode
         */
        private NamingEnumeration wrappedEnumeration;

        /**
         * Build a new enumeration on which we have to decode names
         * @param wrappedEnumeration given enumeration
         */
        WrappedEnumeration(NamingEnumeration wrappedEnumeration) {
            this.wrappedEnumeration = wrappedEnumeration;
        }

        /**
         * Tests if this enumeration contains more elements.
         * @return <code>true</code> if and only if this enumeration object
         *         contains at least one more element to provide;
         *         <code>false</code> otherwise.
         */
        public boolean hasMoreElements() {
            return wrappedEnumeration.hasMoreElements();
        }

        /**
         * Determines whether there are any more elements in the enumeration.
         * This method allows naming exceptions encountered while determining
         * whether there are more elements to be caught and handled by the
         * application.
         * @return true if there is more in the enumeration ; false otherwise.
         * @throws NamingException If a naming exception is encountered while
         *         attempting to determine whether there is another element in
         *         the enumeration.
         */
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        /**
         * Returns the next element of this enumeration if this enumeration
         * object has at least one more element to provide.
         * @return the next element of this enumeration.
         */
        public Object nextElement() {
            javax.naming.NameClassPair ncp;
            ncp = (javax.naming.NameClassPair) wrappedEnumeration.nextElement();
            ncp.setName(decode(ncp.getName()));
            return ncp;
        }

        /**
         * Retrieves the next element in the enumeration.
         * @return the next element in the enumeration.
         * @see java.util.Enumeration#nextElement
         */
        public Object next() {
            return nextElement();
        }

        /**
         * Closes this enumeration.
         * @throws NamingException If a naming exception is encountered while
         *         closing the enumeration.
         */
        public void close() throws NamingException {
            wrappedEnumeration = null;
        }
    }
}
