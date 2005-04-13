/**
 * Copyright (C) 2005 - ObjectWeb (http://www.objectweb.org)
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
 * $Id: UtilDelegateImpl.java,v 1.1 2005/04/13 12:05:36 benoitf Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.util.delegate;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.UtilDelegate;
import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class smoothes out incompatibilities between JDKs provided by different
 * vendors.
 * @author Vadim Nasardinov (vadimn@redhat.com)
 * @since 2005-04-12
 */
public final class UtilDelegateImpl implements UtilDelegate {

    /**
     * Logger
     */
    private static Log logger = LogFactory.getLog(UtilDelegateImpl.class);

    /**
     * Wrapper on UtilDelegate implementation of the JVM used at runtime
     */
    private static UtilDelegate proxied = null;

    /**
     * Default constructor.<br>
     * Build a proxied object which is map to the JVM implementation of
     * UtilDelegate interface.
     */
    public UtilDelegateImpl() {
        String[] vendorDelegates = new String[] {
                "com.sun.corba.se.internal.POA.ShutdownUtilDelegate", // Sun/BEA JDK 1.4
                "com.sun.corba.se.impl.javax.rmi.CORBA.Util", // Sun JDK 1.5
                "com.ibm.CORBA.iiop.UtilDelegateImpl"}; // IBM JDK 1.4

        Class clz = null;
        for (int ii = 0; ii < vendorDelegates.length; ii++) {
            try {
                clz = Class.forName(vendorDelegates[ii]);
                break;
            } catch (ClassNotFoundException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The class '" + vendorDelegates[ii] + "' was not found.", ex);
                }
            }
        }

        if (clz == null) {
            throw new RuntimeException("Couldn't load any of these: " + Arrays.asList(vendorDelegates));
        }

        try {
            proxied = (UtilDelegate) clz.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException(clz.getName(), ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(clz.getName() + " does not have a public constructor", ex);
        }
    }

    /**
     * Copies or connects an object. Used by local stubs to copy an actual
     * parameter, result object, or exception.
     * @param obj the object to copy.
     * @param orb the ORB.
     * @return the copy or connected object.
     * @exception RemoteException if the object could not be copied or
     *            connected.
     */
    public Object copyObject(Object obj, ORB orb) throws RemoteException {
        return proxied.copyObject(obj, orb);
    }

    /**
     * Copies or connects an array of objects. Used by local stubs to copy any
     * number of actual parameters, preserving sharing across parameters as
     * necessary to support RMI semantics.
     * @param objs the objects to copy or connect.
     * @param orb the ORB.
     * @return the copied or connected objects.
     * @exception RemoteException if any object could not be copied or
     *            connected.
     */
    public Object[] copyObjects(Object[] objs, ORB orb) throws RemoteException {
        return proxied.copyObjects(objs, orb);
    }

    /**
     * Returns a singleton instance of a class that implements the interface.
     * @return a class which implements the ValueHandler interface.
     */
    public ValueHandler createValueHandler() {
        return proxied.createValueHandler();
    }

    /**
     * Returns the codebase, if any, for the given class.
     * @param clz the class to get a codebase for.
     * @return a space-separated list of URLs, or null.
     */
    public String getCodebase(Class clz) {
        return proxied.getCodebase(clz);
    }

    /**
     * Returns the tie (if any) for a given target object.
     * @param target the given target from which we want the tie
     * @return the tie or null if no tie is registered for the given target.
     */
    public Tie getTie(Remote target) {
        return proxied.getTie(target);
    }

    /**
     * The <tt>_is_local()</tt> method is provided so that stubs may determine
     * if a particular object is implemented by a local servant and hence local
     * invocation APIs may be used.
     * @param stub the stub to test.
     * @return true if the servant incarnating the object is located in the same
     *         process as the stub and they both share the same ORB instance.
     *         The <tt>_is_local()</tt> method returns false otherwise. The
     *         default behavior of <tt>_is_local()</tt> is to return false.
     * @throws RemoteException The Java to IDL specification does not specify
     *         the conditions that cause a <tt>RemoteException</tt> to be
     *         thrown.
     */
    public boolean isLocal(Stub stub) throws RemoteException {
        return proxied.isLocal(stub);
    }

    /**
     * Returns a class instance for the specified class. This provides an
     * implementation of {@link Util#loadClass(String,String,ClassLoader)} that
     * eliminates an incompatibility between Sun's and IBM's interpretation of
     * Section 1.4.6 <em>Locating Stubs and Ties</em> of <a
     * href="http://www.omg.org/cgi-bin/doc?formal/03-09-04">Java to IDL
     * Language Mapping, v1.3</a>.
     * @param className the name of the class.
     * @param remoteCodebase a space-separated list of URLs at which the class
     *        might be found. May be null.
     * @param loader a <tt>ClassLoader</tt> that may be used to load the class
     *        if all other methods fail.
     * @return the <code>Class</code> object representing the loaded class.
     * @exception ClassNotFoundException if class cannot be loaded.
     */
    public Class loadClass(String className, String remoteCodebase, ClassLoader loader) throws ClassNotFoundException {

        final ClassNotFoundException cnfe;
        try {
            return proxied.loadClass(className, remoteCodebase, loader);
        } catch (ClassNotFoundException ex) {
            cnfe = ex;
        }

        if (className.endsWith("Tie")) {
            final String prefix = "org.omg.stub.";
            final String mangledName;
            if (className.startsWith(prefix)) {
                // IBM interprets Section 1.4.6 to mean that ties should NOT be
                // prefixed with "org.omg.stub."
                mangledName = className.substring(prefix.length());
            } else {
                // Sun hasn't read Section 1.4.6. Its IIOP implementation
                // prefixes ties with "org.omg.stub." the same way that stubs
                // are prefixed.
                mangledName = prefix + className;
            }
            return proxied.loadClass(mangledName, remoteCodebase, loader);
        }
        throw cnfe;
    }

    /**
     * Maps a SystemException to a RemoteException.
     * @param ex the SystemException to map.
     * @return the mapped exception.
     */
    public RemoteException mapSystemException(SystemException ex) {
        return proxied.mapSystemException(ex);
    }

    /**
     * Reads a java.lang.Object as a CORBA any.
     * @param in the stream from which to read the any.
     * @return the object read from the stream.
     */
    public Object readAny(InputStream in) {
        return proxied.readAny(in);
    }

    /**
     * Registers a target for a tie. Adds the tie to an internal table and calls
     * {@link Tie#setTarget} on the tie object.
     * @param tie the tie to register.
     * @param target the target for the tie.
     */
    public void registerTarget(Tie tie, Remote target) {
        proxied.registerTarget(tie, target);
    }

    /**
     * Removes the associated tie from an internal table and calls {@link
     * Tie#deactivate} to deactivate the object.
     * @param target the object to unexport.
     * @throws NoSuchObjectException if the object cannot be unexported
     */
    public void unexportObject(Remote target) throws NoSuchObjectException {
        proxied.unexportObject(target);
    }

    /**
     * Wraps an exception thrown by an implementation method. It returns the
     * corresponding client-side exception.
     * @param orig the exception to wrap.
     * @return the wrapped exception.
     */
    public RemoteException wrapException(Throwable orig) {
        return proxied.wrapException(orig);
    }

    /**
     * Writes a java.lang.Object as either a value or a CORBA Object. If
     * <code>obj</code> is a value object or a stub object, it is written to
     * <code>out.write_abstract_interface(java.lang.Object)</code>. If
     * <code>obj</code> is an exported RMI-IIOP server object, the tie is
     * found and wired to <code>obj</code>, then written to
     * <code>out.write_abstract_interface(java.lang.Object)</code>.
     * @param out the stream in which to write the object.
     * @param obj the object to write.
     */
    public void writeAbstractObject(OutputStream out, Object obj) {
        proxied.writeAbstractObject(out, obj);
    }

    /**
     * Writes any java.lang.Object as a CORBA any.
     * @param out the stream in which to write the any.
     * @param obj the object to write as an any.
     */
    public void writeAny(OutputStream out, Object obj) {
        proxied.writeAny(out, obj);
    }

    /**
     * Writes a java.lang.Object as a CORBA Object. If <code>obj</code> is an
     * exported RMI-IIOP server object, the tie is found and wired to
     * <code>obj</code>, then written to
     * <code>out.write_Object(org.omg.CORBA.Object)</code>. If
     * <code>obj</code> is a CORBA Object, it is written to
     * <code>out.write_Object(org.omg.CORBA.Object)</code>.
     * @param out the stream in which to write the object.
     * @param obj the object to write.
     */
    public void writeRemoteObject(OutputStream out, Object obj) {
        proxied.writeRemoteObject(out, obj);
    }
}
