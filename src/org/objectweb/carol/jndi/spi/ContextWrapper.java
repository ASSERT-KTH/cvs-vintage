/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
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
 * --------------------------------------------------------------------------
 * $Id: ContextWrapper.java,v 1.4 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

//java import
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> ContextWrapper </code> is the CAROL JNDI SPI Context for multi
 * Context management.
 * @author Guillaume (Guillaume.Riviere@inrialpes.fr)
 * @author Jacques Cayuela (Jacques.Cayuela@bull.net)
 * @see javax.naming.Context
 * @see javax.naming.InitialContext
 * @version 1.0, 15/07/2002
 */
public class ContextWrapper implements Context {

    /**
     * Active context (single or multi)
     */
    private Context ac = null;

    /**
     * The env Hashtable passed through new InitialContext(env)
     */

    private Hashtable envpar = null;

    /**
     * True is env is not null (only one context)
     */

    private boolean isSingle;

    /**
     * Constructor, load communication framework and instanciate initial
     * contexts
     */
    public ContextWrapper(Hashtable env) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.ContextWrapper()");
        }
        try {
            envpar = env;

            String jndiURL = (String) env.get(CarolDefaultValues.JNDI_URL_PREFIX);
            isSingle = ((env != null) && (jndiURL != null));
            if (isSingle) {

                String rmiName = CarolDefaultValues
                        .getRMIProtocol((String) env.get(CarolDefaultValues.JNDI_URL_PREFIX));
                Properties prop = CarolCurrentConfiguration.getCurrent().getRMIProperties(rmiName);
                env.put(CarolDefaultValues.JNDI_FACTORY_PREFIX, prop.get(CarolDefaultValues.JNDI_FACTORY_PREFIX));
                ac = new InitialContext(env);
            } else {
                ac = new MultiContext();
            }
        } catch (Exception e) {
            String msg = "ContextWrapper.ContextWrapper() failed: " + e;
            throw new NamingException(msg);
        }
    }

    // Inital context wrapper see the Context documentation for this methods
    public Object lookup(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.lookup(" + name + ")");
        }
        try {
            return ac.lookup(encode(name));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.lookup(" + name + ") failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg);
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            throw new NamingException(e.toString() + "\nCaused by: " + sw.toString() + "\n<End of Cause>");
        }

    }

    public Object lookup(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.lookup(" + name + ")");
        }
        try {
            return ac.lookup(encode(name));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.lookup(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void bind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.bind(" + name + "," + simpleClass(obj.getClass().getName())
                    + " object)");
        }
        try {

            ac.bind(encode(name), obj);

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.bind(" + name + "," + simpleClass(obj.getClass().getName())
                        + " object) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void bind(Name name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.bind(" + name + "," + simpleClass(obj.getClass().getName())
                    + " object)");
        }
        try {

            ac.bind(encode(name), obj);

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.bind(Name name, Object obj) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rebind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.rebind(" + name + "," + simpleClass(obj.getClass().getName())
                    + " object)");
        }
        try {
            ac.rebind(encode(name), obj);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.rebind(String name, Object obj) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rebind(Name name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.rebind(" + name + "," + simpleClass(obj.getClass().getName())
                    + " object)");
        }
        try {

            ac.rebind(encode(name), obj);

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.rebind(Name name, Object obj) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void unbind(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.unbind(" + name + ")");
        }
        try {

            ac.unbind(encode(name));

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.unbind(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void unbind(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.unbind(" + name + ")");
        }
        try {
            ac.unbind(encode(name));

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.unbind(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rename(String oldName, String newName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.rename(" + oldName + "," + newName + ")");
        }
        try {

            ac.rename(encode(oldName), encode(newName));

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.rename(String oldName, String newName) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.rename(" + oldName + "," + newName + ")");
        }
        try {

            ac.rename(encode(oldName), encode(newName));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.rename(Name oldName, Name newName) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration list(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.list(" + name + ")");
        }
        try {
            // must decode the individually returned names
            return (new WrapEnum(ac.list(encode(name))));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.list(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration list(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.list(" + name + ")");
        }
        try {
            // must decode the individually returned names
            return (new WrapEnum(ac.list(encode(name))));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.list(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.listBindings(" + name + ")");
        }
        try {
            // must decode the individually returned names
            return (new WrapEnum(ac.listBindings(encode(name))));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.listBindings(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.listBindings(" + name + ")");
        }
        try {
            // must decode the individually returned names
            return (new WrapEnum(ac.listBindings(encode(name))));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.listBindings(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void destroySubcontext(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.destroySubcontext(" + name + ")");
        }
        try {
            ac.destroySubcontext(encode(name));

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.destroySubcontext(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void destroySubcontext(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.destroySubcontext(" + name + ")");
        }
        try {

            ac.destroySubcontext(encode(name));

        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.destroySubcontext(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Context createSubcontext(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.createSubcontext(" + name + ")");
        }
        try {
            return ac.createSubcontext(encode(name));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.createSubcontext(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Context createSubcontext(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.createSubcontext(" + name + ")");
        }
        try {
            return ac.createSubcontext(encode(name));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.createSubcontext(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Object lookupLink(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.lookupLink(" + name + ")");
        }
        try {
            return ac.lookupLink(encode(name));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.lookupLink(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Object lookupLink(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.lookupLink(" + name + ")");
        }
        try {
            return ac.lookupLink(encode(name));
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.lookupLink(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NameParser getNameParser(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.getNameParser(" + name + ")");
        }
        try {
            return ac.getNameParser(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.getNameParser(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NameParser getNameParser(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.getNameParser(" + name + ")");
        }
        try {
            return ac.getNameParser(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.getNameParser(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public String composeName(String name, String prefix) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.composeName(" + name + "," + prefix + ")");
        }
        return name;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.composeName(" + name + "," + prefix + ")");
        }
        return ac.composeName(name, prefix);
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        try {
            if (TraceCarol.isDebugJndiCarol()) {
                TraceCarol.debugJndiCarol("ContextWrapper.addToEnvironment(" + propName + ","
                        + simpleClass(propVal.getClass().getName()) + " object)");
            }
            return ac.addToEnvironment(propName, propVal);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.addToEnvironment(String propName, Object propVal)  failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.removeFromEnvironment(" + propName + ")");
        }
        try {
            return ac.removeFromEnvironment(propName);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.removeFromEnvironment(String propName) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Hashtable getEnvironment() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.getEnvironment()");
        }
        try {
            return ac.getEnvironment();
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.getEnvironment() failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void close() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.close()");
        }
        try {
            ac.close();
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.close() failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public String getNameInNamespace() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("ContextWrapper.getNameInNamespace()");
        }
        try {
            return ac.getNameInNamespace();
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "ContextWrapper.getNameInNamespace() failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    /**
     * Just the name of the class without the package
     */
    private String simpleClass(String c) {
        return c.substring(c.lastIndexOf('.') + 1);
    }

    private String encode(String name) {
        // Hide special chrs from flat namespace registry.
        // Escape forward and backward slashes, and leading quote character.

        if (name.length() < 1) {
            return name;
        }
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
        if (newname.charAt(0) == '"' || newname.charAt(0) == '\'') {
            newname.insert(0, '\\');
        }
        return newname.toString();
    }

    private String decode(String name) // undo what encode() does
    {

        StringBuffer newname = new StringBuffer(name);
        if (newname.length() >= 2 && (newname.charAt(0) == '"' || newname.charAt(0) == '\'')
                && newname.charAt(0) == newname.charAt(newname.length() - 1)) {
            // we have a quoted string: remove the enclosing quotes
            newname.deleteCharAt(0);
            newname.deleteCharAt(newname.length() - 1);
        } else {
            if (name.indexOf('\\') < 0) {
                return name;
            } // nothing to decode
            int i = 0;
            while (i < newname.length()) {
                if (newname.charAt(i) == '\\') {
                    newname.deleteCharAt(i);
                    i++;
                    continue;
                }
                i++;
            }
        }
        return newname.toString();
    }

    private Name encode(Name name) {
        try {
            return new CompositeName(encode(name.toString()));
        } catch (InvalidNameException e) {
            return name;
        }
    }

    //    private Name decode(Name name) {
    //        try {
    //            return new CompositeName(decode(name.toString()));
    //        } catch (InvalidNameException e) {
    //            return name;
    //        }
    //    }

    class WrapEnum implements NamingEnumeration // Class to wrap enumerations
    {

        NamingEnumeration enum;

        WrapEnum(NamingEnumeration names) {
            this.enum = names;
        }

        public boolean hasMoreElements() {
            return enum.hasMoreElements();
        }

        public boolean hasMore() throws NamingException {
            return enum.hasMore();
        }

        public Object nextElement() {
            javax.naming.NameClassPair ncp;
            ncp = (javax.naming.NameClassPair) enum.nextElement();
            ncp.setName(decode(ncp.getName()));
            return ncp;
        }

        public Object next() throws NamingException {
            javax.naming.NameClassPair ncp;
            ncp = (javax.naming.NameClassPair) enum.next();
            ncp.setName(decode(ncp.getName()));
            return ncp;
        }

        public void close() throws NamingException {
            enum = null;
        }
    }

}

