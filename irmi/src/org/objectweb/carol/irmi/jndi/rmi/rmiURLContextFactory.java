/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
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
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi.jndi.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import javax.rmi.PortableRemoteObject;

/**
 * rmiURLContextFactory
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class rmiURLContextFactory implements ObjectFactory {

    public rmiURLContextFactory() {}

    public Object getObjectInstance(Object obj, Name name, Context ctx,
                                    Hashtable env) throws Exception {
        return new WrapperContext(NamingManager.getURLContext("rmi", null));
    }

    private static class WrapperContext implements Context {

        private Context m_wrapped;

        WrapperContext(Context ctx) {
            m_wrapped = ctx;
        }

        private Object wrap(Object obj) throws NamingException {
            if (obj instanceof Remote) {
                try {
                    return PortableRemoteObject.toStub((Remote) obj);
                } catch (NoSuchObjectException e) {
                    throw (NamingException) new NamingException().initCause(e);
                }
            } else {
                return obj;
            }
        }

        public Object addToEnvironment(String propName, Object propVal) throws NamingException {
            return m_wrapped.addToEnvironment(propName, propVal);
        }

        public void bind(Name name, Object obj) throws NamingException {
            m_wrapped.bind(name, wrap(obj));
        }

        public void bind(String name, Object obj) throws NamingException {
            m_wrapped.bind(name, wrap(obj));
        }

        public void close() throws NamingException {
            m_wrapped.close();
        }

        public Name composeName(Name name, Name prefix) throws NamingException {
            return m_wrapped.composeName(name, prefix);
        }

        public String composeName(String name, String prefix) throws NamingException {
            return m_wrapped.composeName(name, prefix);
        }

        public Context createSubcontext(Name name) throws NamingException {
            return m_wrapped.createSubcontext(name);
        }

        public Context createSubcontext(String name) throws NamingException {
            return m_wrapped.createSubcontext(name);
        }

        public void destroySubcontext(Name name) throws NamingException {
            m_wrapped.destroySubcontext(name);
        }

        public void destroySubcontext(String name) throws NamingException {
            m_wrapped.destroySubcontext(name);
        }

        public Hashtable getEnvironment() throws NamingException {
            return m_wrapped.getEnvironment();
        }

        public String getNameInNamespace() throws NamingException {
            return m_wrapped.getNameInNamespace();
        }

        public NameParser getNameParser(Name name) throws NamingException {
            return m_wrapped.getNameParser(name);
        }

        public NameParser getNameParser(String name) throws NamingException {
            return m_wrapped.getNameParser(name);
        }

        public NamingEnumeration list(Name name) throws NamingException {
            return m_wrapped.list(name);
        }

        public NamingEnumeration list(String name) throws NamingException {
            return m_wrapped.list(name);
        }

        public NamingEnumeration listBindings(Name name) throws NamingException {
            return m_wrapped.listBindings(name);
        }

        public NamingEnumeration listBindings(String name) throws NamingException {
            return m_wrapped.listBindings(name);
        }

        public Object lookup(Name name) throws NamingException {
            return m_wrapped.lookup(name);
        }

        public Object lookup(String name) throws NamingException {
            return m_wrapped.lookup(name);
        }

        public Object lookupLink(Name name) throws NamingException {
            return m_wrapped.lookupLink(name);
        }

        public Object lookupLink(String name) throws NamingException {
            return m_wrapped.lookupLink(name);
        }

        public void rebind(Name name, Object obj) throws NamingException {
            m_wrapped.rebind(name, wrap(obj));
        }

        public void rebind(String name, Object obj) throws NamingException {
            m_wrapped.rebind(name, wrap(obj));
        }

        public Object removeFromEnvironment(String propName) throws NamingException {
            return m_wrapped.removeFromEnvironment(propName);
        }

        public void rename(Name oldName, Name newName) throws NamingException {
            m_wrapped.rename(oldName, newName);
        }

        public void rename(String oldName, String newName) throws NamingException {
            m_wrapped.rename(oldName, newName);
        }

        public void unbind(Name name) throws NamingException {
            m_wrapped.unbind(name);
        }

        public void unbind(String name) throws NamingException {
            m_wrapped.unbind(name);
        }
    }

}
