/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 1999-2005 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: CompNamingContext.java,v 1.4 2005/03/10 16:50:22 benoitf Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.jndi.enc.java;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.RefAddr;
import javax.naming.Reference;

import org.objectweb.carol.util.configuration.TraceCarol;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Implementation of Context interface for EJB Environment. Must handle
 * subContexts (because of jndi/, ejb/, ...)
 * @author Philippe Durieux
 * @author Philippe Coq monolog
 * @author Florent Benoit handle Reference object for the lookup.
 */
public class CompNamingContext implements Context {

    /**
     * Logger
     */
    private static Logger logger = null;

    /**
     * Environment
     */
    private Hashtable myEnv = null;

    /**
     * Bindings
     */
    private Hashtable bindings = new Hashtable();

    /**
     * Parser
     */
    private static NameParser myParser = new JavaNameParser();

    /**
     * Naming id
     */
    private String compId;

    /**
     * @return the logger
     */
    protected Logger getLogger() {
        return TraceCarol.getJndiCarolLogger();
    }

    /**
     * Constructor
     * @param id id of the context.
     * @param env initial environment.
     */
    public CompNamingContext(String id, Hashtable env) {
        if (env != null) {
            // clone env to be able to change it.
            myEnv = (Hashtable) (env.clone());
        }
        compId = id;
        logger = getLogger();
    }

    /**
     * Constructor
     * @param id id of the context.
     */
    public CompNamingContext(String id) {
        myEnv = new Hashtable();
        compId = id;
        logger = getLogger();
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        // Just use the string version for now.
        return lookup(name.toString());
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(String name) throws NamingException {
        logger.log(BasicLevel.DEBUG, name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            // Empty name means this context
            logger.log(BasicLevel.DEBUG, "empty name");
            return this;
        }

        if (n.size() == 1) {
            // leaf in the env tree
            Object ret = bindings.get(name);
            if (ret == null) {
                logger.log(BasicLevel.DEBUG, " " + name + " not found.");
                throw new NameNotFoundException(name);
            }
            if (ret instanceof LinkRef) {
                // Handle special case of the LinkRef since I think
                // it's not handled by std NamingManager.getObjectInstance().
                // The name hidden in linkref is in the initial context.

                InitialContext ictx = new InitialContext();
                RefAddr ra = ((Reference) ret).get(0);
                try {
                    ret = ictx.lookup((String) ra.getContent());
                } catch (Exception e) {
                    NamingException ne = new NamingException(e.getMessage());
                    ne.setRootCause(e);
                    logger.log(BasicLevel.WARN, "unexpected exception " + e.getMessage());
                    throw ne;
                }
            } else if (ret instanceof Reference) {
                // Use NamingManager to build an object
                try {
                    Object o = javax.naming.spi.NamingManager.getObjectInstance(ret, n, this, myEnv);
                    ret = o;
                } catch (NamingException e) {
                    throw e;
                } catch (Exception e) {
                    NamingException ne = new NamingException(e.getMessage());
                    ne.setRootCause(e);
                    throw ne;
                }
                if (ret == null) {
                    logger.log(BasicLevel.WARN, "Can not build an object with the reference " + name);
                    throw new NamingException("Can not build an object with the reference '" + name + "'");
                }
            }
            return ret;
        } else {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // should throw exception if sub context not found!
            Context subctx = lookupCtx(n.get(0));
            return subctx.lookup(suffix);
        }
    }

    /**
     * Binds a name to an object. Delegate to the String version.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.directory.InvalidAttributesException
     * @see javax.naming.NameAlreadyBoundException
     */
    public void bind(Name name, Object obj) throws NamingException {
        // Just use the string version for now.
        bind(name.toString(), obj);
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.directory.InvalidAttributesException
     * @see javax.naming.NameAlreadyBoundException
     */
    public void bind(String name, Object obj) throws NamingException {

        logger.log(BasicLevel.DEBUG, name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.log(BasicLevel.ERROR, "CompNamingContext bind empty name ?");
            throw new InvalidNameException("CompNamingContext cannot bind empty name");
        }

        if (n.size() == 1) {
            // leaf in the env tree
            if (bindings.get(name) != null) {
                logger.log(BasicLevel.ERROR, "CompNamingContext: trying to overbind");
                throw new NameAlreadyBoundException("CompNamingContext: Use rebind to bind over a name");
            }
            bindings.put(name, obj);
        } else {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // must create the subcontext first if it does not exist yet.
            Context subctx;
            try {
                subctx = lookupCtx(n.get(0));
            } catch (NameNotFoundException e) {
                subctx = createSubcontext(n.get(0));
            }
            subctx.bind(suffix, obj);
        }
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.directory.InvalidAttributesException
     */
    public void rebind(Name name, Object obj) throws NamingException {
        // Just use the string version for now.
        rebind(name.toString(), obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.directory.InvalidAttributesException
     * @see javax.naming.InvalidNameException
     */
    public void rebind(String name, Object obj) throws NamingException {

        logger.log(BasicLevel.DEBUG, name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.log(BasicLevel.ERROR, "CompNamingContext rebind empty name ?");
            throw new InvalidNameException("CompNamingContext cannot rebind empty name");
        }

        if (n.size() == 1) {
            // leaf in the env tree
            bindings.put(name, obj);
        } else {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // must create the subcontext first if it does not exist yet.
            Context subctx;
            try {
                subctx = lookupCtx(n.get(0));
            } catch (NameNotFoundException e) {
                subctx = createSubcontext(n.get(0));
            }
            subctx.rebind(suffix, obj);
        }
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameNotFoundException
     */
    public void unbind(Name name) throws NamingException {
        // Just use the string version for now.
        unbind(name.toString());
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameNotFoundException
     * @see javax.naming.InvalidNameException
     */
    public void unbind(String name) throws NamingException {

        logger.log(BasicLevel.DEBUG, name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.log(BasicLevel.ERROR, "CompNamingContext unbind empty name ?");
            throw new InvalidNameException("CompNamingContext cannot unbind empty name");
        }

        if (n.size() == 1) {
            // leaf in the env tree
            if (bindings.get(name) == null) {
                logger.log(BasicLevel.ERROR, "CompNamingContext nothing to unbind");
                throw new NameNotFoundException(name);
            }
            bindings.remove(name);
        } else {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // should throw exception if sub context not found!
            Context subctx = lookupCtx(n.get(0));
            subctx.unbind(suffix);
        }
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(Name oldName, Name newName) throws NamingException {
        // Just use the string version for now.
        rename(oldName.toString(), newName.toString());
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(String oldName, String newName) throws NamingException {

        logger.log(BasicLevel.ERROR, "CompNamingContext rename " + oldName + " in " + newName);

        Object obj = lookup(oldName);
        rebind(newName, obj);
        unbind(oldName);
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. The contents of any subcontexts are not
     * included.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(Name name) throws NamingException {
        // Just use the string version for now.
        return list(name.toString());
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration list(String name) throws NamingException {

        logger.log(BasicLevel.DEBUG, name);

        if (name.length() == 0) {
            // List this context
            return new ListOfNames(bindings);
        }
        Object obj = lookup(name);
        if (obj instanceof Context) {
            return ((Context) obj).list("");
        } else {
            logger.log(BasicLevel.ERROR, "CompNamingContext: can only list a Context");
            throw new NotContextException(name);
        }
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them. The contents of any subcontexts are not included. If a
     * binding is added to or removed from this context, its effect on an
     * enumeration previously returned is undefined.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type Binding.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        // Just use the string version for now.
        return listBindings(name.toString());
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type Binding.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration listBindings(String name) throws NamingException {

        logger.log(BasicLevel.DEBUG, name);

        if (name.length() == 0) {
            // List this context
            return new ListOfBindings(bindings);
        }
        Object obj = lookup(name);
        if (obj instanceof Context) {
            return ((Context) obj).listBindings("");
        } else {
            logger.log(BasicLevel.ERROR, "CompNamingContext: can only list a Context");
            throw new NotContextException(name);
        }
    }

    /**
     * Destroys the named context and removes it from the namespace. Not
     * supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(Name name) throws NamingException {
        // Just use the string version for now.
        destroySubcontext(name.toString());
    }

    /**
     * Destroys the named context and removes it from the namespace. Not
     * supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(String name) throws NamingException {

        logger.log(BasicLevel.ERROR, "CompNamingContext try to destroySubcontext " + name);

        throw new OperationNotSupportedException("CompNamingContext: destroySubcontext");
    }

    /**
     * Creates and binds a new context. Creates a new context with the given
     * name and binds it in the target context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.directory.InvalidAttributesException
     * @see javax.naming.NameAlreadyBoundException
     */
    public Context createSubcontext(Name name) throws NamingException {
        // Just use the string version for now.
        return createSubcontext(name.toString());
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.directory.InvalidAttributesException
     * @see javax.naming.NameAlreadyBoundException
     */
    public Context createSubcontext(String name) throws NamingException {

        logger.log(BasicLevel.DEBUG, name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.log(BasicLevel.ERROR, "CompNamingContext createSubcontext with empty name ?");
            throw new InvalidNameException("CompNamingContext cannot create empty Subcontext");
        }

        Context ctx = null; // returned ctx
        if (n.size() == 1) {
            // leaf in the env tree: create ctx and bind it in parent.
            ctx = new CompNamingContext(compId, myEnv);
            bindings.put(name, ctx);
        } else {
            // as for bind, we must create first all the subcontexts
            // if they don't exist yet.
            String suffix = n.getSuffix(1).toString();
            Context subctx;
            name = n.get(0);
            try {
                subctx = lookupCtx(name);
            } catch (NameNotFoundException e) {
                subctx = createSubcontext(name);
            }
            ctx = subctx.createSubcontext(suffix);
        }
        return ctx;
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name. If the object bound to name is not a link,
     * returns the object itself.
     * @param name the name of the object to look up
     * @return the object bound to name, not following the terminal link (if
     *         any).
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(Name name) throws NamingException {
        // Just use the string version for now.
        return lookupLink(name.toString());
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name. If the object bound to name is not a link,
     * returns the object itself.
     * @param name the name of the object to look up
     * @return the object bound to name, not following the terminal link (if
     *         any)
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(String name) throws NamingException {

        logger.log(BasicLevel.DEBUG, name);

        // To be done. For now: just return the object
        logger.log(BasicLevel.ERROR, "CompNamingContext lookupLink not implemented yet!");
        return lookup(name);
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(Name name) throws NamingException {
        return myParser;
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(Name name, Name prefix) throws NamingException {

        logger.log(BasicLevel.ERROR, "CompNamingContext composeName not implemented!");
        throw new OperationNotSupportedException("CompNamingContext composeName");
    }

    /**
     * Composes the name of this context with a name relative to this context:
     * Not supported.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public String composeName(String name, String prefix) throws NamingException {

        logger.log(BasicLevel.ERROR, "CompNamingContext composeName " + name + " " + prefix);

        throw new OperationNotSupportedException("CompNamingContext composeName");
    }

    /**
     * Adds a new environment property to the environment of this context. If
     * the property already exists, its value is overwritten.
     * @param propName the name of the environment property to add; may not be
     *        null
     * @param propVal the value of the property to add; may not be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment before
     * @throws NamingException if a naming exception is encountered
     */
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {

        logger.log(BasicLevel.DEBUG, propName);

        if (myEnv == null) {
            myEnv = new Hashtable();
        }
        return myEnv.put(propName, propVal);
    }

    /**
     * Removes an environment property from the environment of this context.
     * @param propName the name of the environment property to remove; may not
     *        be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment
     * @throws NamingException if a naming exception is encountered
     */
    public Object removeFromEnvironment(String propName) throws NamingException {

        logger.log(BasicLevel.DEBUG, propName);

        if (myEnv == null) {
            return null;
        }
        return myEnv.remove(propName);
    }

    /**
     * Retrieves the environment in effect for this context.
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable getEnvironment() throws NamingException {

        logger.log(BasicLevel.DEBUG, "");

        if (myEnv == null) {
            myEnv = new Hashtable();
        }
        return myEnv;
    }

    /**
     * Closes this context.
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        myEnv = null;
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     */
    public String getNameInNamespace() {
        // this is used today for debug only.
        return compId;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    /**
     * Find if this name is a sub context.
     * @param name the sub context name
     * @return the named Context
     * @throws NamingException When nam?ing fails
     * @see javax.naming.NameNotFoundException
     * @see javax.naming.NameAlreadyBoundException
     */
    private Context lookupCtx(String name) throws NamingException {
        Object obj = bindings.get(name);
        if (obj == null) {
            throw new NameNotFoundException();
        }
        if (obj instanceof CompNamingContext) {
            return (Context) obj;
        } else {
            throw new NameAlreadyBoundException(name);
        }
    }

    // ------------------------------------------------------------------
    // Inner classes for enumerating lists of bindings
    // ------------------------------------------------------------------

    /**
     * Implementation of the NamingEnumeration for list operations Each element
     * is of type NameClassPair.
     */
    protected class ListOfNames implements NamingEnumeration {

        /**
         * list of names
         */
        private Enumeration names;

        /**
         * List of bindings
         */
        private Hashtable bindings;

        /**
         * Constructor. Called by list()
         * @param bindings list of bindings
         */
        ListOfNames(Hashtable bindings) {
            this.bindings = bindings;
            this.names = bindings.keys();
        }

        /**
         * Determines whether there are any more elements in the enumeration.
         * @return true if there is more in the enumeration ; false otherwise.
         * @throws NamingException If a naming exception is encountered while
         *         attempting to determine whether there is another element in
         *         the enumeration.
         */
        public boolean hasMore() throws NamingException {
            return names.hasMoreElements();
        }

        /**
         * Retrieves the next element in the enumeration.
         * @return The possibly null element in the enumeration. null is only
         *         valid for enumerations that can return null (e.g.
         *         Attribute.getAll() returns an enumeration of attribute
         *         values, and an attribute value can be null).
         * @throws NamingException If a naming exception is encountered while
         *         attempting to retrieve the next element. See NamingException
         *         and its subclasses for the possible naming exceptions.
         */
        public Object next() throws NamingException {
            String name = (String) names.nextElement();
            String className = bindings.get(name).getClass().getName();
            return new NameClassPair(name, className);
        }

        /**
         * Closes this enumeration.
         */
        public void close() {
        }

        /**
         * Returns the next element of this enumeration if this enumeration
         * object has at least one more element to provide.
         * @return the next element of this enumeration.
         */
        public Object nextElement() {
            try {
                return next();
            } catch (NamingException e) {
                throw new NoSuchElementException(e.toString());
            }
        }

        /**
         * Tests if this enumeration contains more elements.
         * @return <code>true</code> if and only if this enumeration object
         *         contains at least one more element to provide;
         *         <code>false</code> otherwise.
         */
        public boolean hasMoreElements() {
            try {
                return hasMore();
            } catch (NamingException e) {
                return false;
            }
        }

        /**
         * @return the bindings.
         */
        protected Hashtable getBindings() {
            return bindings;
        }

        /**
         * @return the names.
         */
        protected Enumeration getNames() {
            return names;
        }
    }

    /**
     * Implementation of the NamingEnumeration for listBindings operations
     */
    protected class ListOfBindings extends ListOfNames {

        /**
         * Constructor.
         * @param bindings list of bindings
         */
        ListOfBindings(Hashtable bindings) {
            super(bindings);
        }

        /**
         * It returns a Binding instead of a NameClassPair * Retrieves the next
         * element in the enumeration.
         * @return The possibly null element in the enumeration. null is only
         *         valid for enumerations that can return null (e.g.
         *         Attribute.getAll() returns an enumeration of attribute
         *         values, and an attribute value can be null).
         * @throws NamingException If a naming exception is encountered while
         *         attempting to retrieve the next element. See NamingException
         *         and its subclasses for the possible naming exceptions.
         */
        public Object next() throws NamingException {
            String name = (String) getNames().nextElement();
            return new Binding(name, getBindings().get(name));
        }
    }
}
