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
 * $Id: CarolJRMPPerformanceHelper.java,v 1.3 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.perfs;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.rmi.server.RMIClassLoaderSpi;

import sun.rmi.server.MarshalOutputStream;

/**
 * Class <code>CarolJRMPPerformanceHelper</code> Provide an Helper for perfs
 * mesures
 * @author Simon Nieuviarts
 * @version 1.0, 15/03/2003
 */
public class CarolJRMPPerformanceHelper {

    private static RMIClassLoaderSpi defRMISpi;

    static {
        try {
            Class cl = Class.forName("java.rmi.server.RMIClassLoader");
            Method meth = cl.getMethod("getDefaultProviderInstance", new Class[0]);
            defRMISpi = (RMIClassLoaderSpi) meth.invoke(cl, new Object[0]);
        } catch (Exception e) {
            //TraceCarol.error("RemoteClassLoaderSpi error", e);
        }
    }

    /**
     * See a marshalled object
     * @param obj the object to marchal
     * @return a visible format of the marshalled object
     */
    public static String getMarshalBytes(Object obj) {
        try {
            String result = getClassString(obj.getClass()) + "<serialization>\n";
            // Print the Context value and size
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            MarshalOutputStream p = new MarshalOutputStream(ostream);
            p.writeObject(obj);
            p.flush();
            byte[] b = ostream.toByteArray();
            for (int i = 0; i < b.length; i++) {
                if ((b[i] >= 0) && (b[i] < 32)) {
                    result += "<" + b[i] + ">";
                } else
                    result += (char) b[i];
            }
            result += "</serialization>\n";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * See the size of a Marchalled object
     * @param obj the Object to marchal
     * @return the size of this Marchalled object
     */
    public static int getMarshalSize(Object obj) {
        try {
            // Print the Context value and size
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            MarshalOutputStream p = new MarshalOutputStream(ostream);
            p.writeObject(obj);
            p.flush();
            return ostream.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getClassString(Class cl) {
        ClassLoader loader = cl.getClassLoader();
        String result = "<class>\n";
        result = result + "<classloader>" + loader.getClass().getName() + "</classloader>\n";
        result = result + "<annotations>" + defRMISpi.getClassAnnotation(cl) + "</annotaions>\n";
        result += "</class>";
        return result;
    }

    public static String getClassAnnotation(Class cl) {
        return defRMISpi.getClassAnnotation(cl);
    }
}