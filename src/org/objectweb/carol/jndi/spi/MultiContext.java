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
 * $Id: MultiContext.java,v 1.3 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.spi;

//java import
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.carol.util.configuration.CarolCurrentConfiguration;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Class <code> MultiContext </code> is the CAROL JNDI SPI Context for multi
 * Context management. this class use the protocol current for management of
 * multi protocol
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @see javax.naming.Context
 * @see javax.naming.InitialContext
 * @see org.objectweb.util.multi.ProtocolCurrent
 * @version 1.0, 15/07/2002
 */
public class MultiContext implements Context {

    /**
     * The ProtocolCurrent for management of active Context
     */
    private CarolCurrentConfiguration cccf = null;

    /**
     * Active Contexts, this variable is just a cache of the protocol current
     * context array
     */
    private Hashtable activesInitialsContexts = null;

    /**
     * String for rmi name
     */
    private String rmiName = null;

    /**
     * Constructor, load communication framework and instaciate initial contexts
     */
    public MultiContext() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.MultiContext()");
        }
        try {
            cccf = CarolCurrentConfiguration.getCurrent();
            activesInitialsContexts = cccf.getNewContextHashtable();
        } catch (Exception e) {
            String msg = "MultiContext.MultiContext() failed: " + e;
            throw new NamingException(msg);
        }
    }

    // Inital context wrapper see the Context documentation for this methods
    public Object lookup(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.lookup(\"" + name + "\")/rmi name=\"" + cccf.getCurrentRMIName()
                    + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().lookup(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.lookup(\"" + name + "\") failed: " + e;
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
            TraceCarol.debugJndiCarol("MultiContext.lookup(\"" + name + "\")/rmi name=\"" + cccf.getCurrentRMIName()
                    + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().lookup(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.lookup(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void bind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.bind(String, Object)");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).bind(name, obj);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.bind()";
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void bind(Name name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.bind()");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).bind(name, obj);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.bind(Name name, Object obj) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rebind(String name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.rebind()");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).rebind(name, obj);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.rebind(String name, Object obj) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rebind(Name name, Object obj) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.rebind()");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).rebind(name, obj);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.rebind(Name name, Object obj) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void unbind(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.unbind(\"" + name + "\")");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).unbind(name);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.unbind(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void unbind(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.unbind(\"" + name + "\")");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).unbind(name);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.unbind(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rename(String oldName, String newName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.rename(\"" + oldName + "\",\"" + newName + "\")");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).rename(oldName, newName);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.rename(String oldName, String newName) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.rename(\"" + oldName + "\",\"" + newName + "\")");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).rename(oldName, newName);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.rename(Name oldName, Name newName) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration list(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.list(\"" + name + "\")/rmi name=\"" + cccf.getCurrentRMIName()
                    + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().list(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.list(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration list(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.list(\"" + name + "\")/rmi name=\"" + cccf.getCurrentRMIName()
                    + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().list(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.list(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.listBindings(\"" + name + "\")/rmi name=\""
                    + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().listBindings(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.listBindings(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.listBindings(\"" + name + "\")/rmi name=\""
                    + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().listBindings(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.listBindings(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void destroySubcontext(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.destroySubcontext(\"" + name + "\")");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).destroySubcontext(name);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.destroySubcontext(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void destroySubcontext(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.destroySubcontext(\"" + name + "\")");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).destroySubcontext(name);
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.destroySubcontext(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Context createSubcontext(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.createSubcontext(\"" + name + "\")");
        }
        try {
            return cccf.getCurrentInitialContext().createSubcontext(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.createSubcontext(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Context createSubcontext(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.createSubcontext(\"" + name + "\")");
        }
        try {
            return cccf.getCurrentInitialContext().createSubcontext(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.createSubcontext(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Object lookupLink(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.lookupLink(\"" + name + "\")/rmi name=\""
                    + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().lookupLink(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.lookupLink(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Object lookupLink(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.lookupLink(\"" + name + "\")/rmi name=\""
                    + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().lookupLink(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.lookupLink(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NameParser getNameParser(String name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.getNameParser(\"" + name + "\")/rmi name=\""
                    + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().getNameParser(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.getNameParser(String name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public NameParser getNameParser(Name name) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.getNameParser(\"" + name + "\")/rmi name=\""
                    + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().getNameParser(name);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.getNameParser(Name name) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public String composeName(String name, String prefix) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.composeName(" + name + "," + prefix + ")/rmi name="
                    + cccf.getCurrentRMIName());
        }
        return name;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.composeName(\"" + name + "," + prefix + "\")/rmi name=\""
                    + cccf.getCurrentRMIName() + "\"");
        }
        return cccf.getCurrentInitialContext().composeName(name, prefix);
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        try {
            if (TraceCarol.isDebugJndiCarol()) {
                TraceCarol.debugJndiCarol("MultiContext.addToEnvironment()");
            }
            return cccf.getCurrentInitialContext().addToEnvironment(propName, propVal);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.addToEnvironment(String propName, Object propVal)  failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.removeFromEnvironment(\"" + propName + "\")");
        }
        try {
            return cccf.getCurrentInitialContext().removeFromEnvironment(propName);
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.removeFromEnvironment(String propName) failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public Hashtable getEnvironment() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.getEnvironment()/rmi name=\"" + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().getEnvironment();
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.getEnvironment() failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public void close() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.close()");
        }
        try {
            for (Enumeration e = activesInitialsContexts.keys(); e.hasMoreElements();) {
                rmiName = (String) e.nextElement();
                cccf.setRMI(rmiName);
                ((Context) activesInitialsContexts.get(rmiName)).close();
                cccf.setDefault();
            }
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.close() failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

    public String getNameInNamespace() throws NamingException {
        if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("MultiContext.getNameInNamespace()/rmi name=" + cccf.getCurrentRMIName() + "\"");
        }
        try {
            return cccf.getCurrentInitialContext().getNameInNamespace();
        } catch (NamingException e) {
            if (TraceCarol.isDebugJndiCarol()) {
                String msg = "MultiContext.getNameInNamespace() failed: " + e;
                TraceCarol.debugJndiCarol("Error: " + msg + " " + e);
            }
            throw e;
        }
    }

}

