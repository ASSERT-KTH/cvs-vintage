/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
/***
 * Jonathan: an Open Distributed Processing Environment 
 * Copyright (C) 1999 France Telecom R&D
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Release: 2.0
 *
 * Contact: jonathan@objectweb.org
 *
 * Author: Kathleen Milsted
 *
 * with contributions from:
 *   Francois Horn
 *   Bruno Dumant
 *   Vincent Sheffer 
 * 
 */
package org.objectweb.carol.cmi.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is a utility class containing useful static methods for the stub
 * compiler.
 */
public class Utils {

    static void trace(String str, CompilerContext cCtx) {
        if (cCtx.verbose) {
            System.out.println("* " + str);
        }
    }

    static void trace_header(CompilerContext cCtx) {
        if (cCtx.verbose) {
            System.out.println();
            System.out.println(
                "Cluster stub compiler, version "
                    + CompilerContext.version
                    + " (verbose mode)");
        }
    }

    static String typeName(Class cl) {
        if (cl.isArray()) {
            return typeName(cl.getComponentType()) + "[]";
        } else {
            return cl.getName();
        }
    }

    static String prim2refTypeName(Class cl) {
        if (cl.isArray())
            return typeName(cl.getComponentType()) + "[]";
        if (cl == Boolean.TYPE)
            return "java.lang.Boolean";
        else if (cl == Byte.TYPE)
            return "java.lang.Byte";
        else if (cl == Character.TYPE)
            return "java.lang.Character";
        else if (cl == Short.TYPE)
            return "java.lang.Short";
        else if (cl == Integer.TYPE)
            return "java.lang.Integer";
        else if (cl == Long.TYPE)
            return "java.lang.Long";
        else if (cl == Float.TYPE)
            return "java.lang.Float";
        else if (cl == Double.TYPE)
            return "java.lang.Double";
        else
            return cl.getName();
    }

    static String ref2primConversion(Class cl) {
        if (!cl.isPrimitive())
            return "";
        if (cl == Boolean.TYPE)
            return ".booleanValue()";
        else if (cl == Byte.TYPE)
            return ".byteValue()";
        else if (cl == Character.TYPE)
            return ".charValue()";
        else if (cl == Short.TYPE)
            return ".shortValue()";
        else if (cl == Integer.TYPE)
            return ".intValue()";
        else if (cl == Long.TYPE)
            return ".longValue()";
        else if (cl == Float.TYPE)
            return ".floatValue()";
        else if (cl == Double.TYPE)
            return ".doubleValue()";
        else
            return "";
    }

    static String typeUnMarshaller(Class cl) {
        if (!cl.isPrimitive())
            return "readValue";
        if (cl == Boolean.TYPE)
            return "readBoolean";
        else if (cl == Byte.TYPE)
            return "readByte";
        else if (cl == Character.TYPE)
            return "readChar16";
        else if (cl == Short.TYPE)
            return "readShort";
        else if (cl == Integer.TYPE)
            return "readInt";
        else if (cl == Long.TYPE)
            return "readLong";
        else if (cl == Float.TYPE)
            return "readFloat";
        else if (cl == Double.TYPE)
            return "readDouble";
        else
            return "";
    }

    static String typeMarshaller(Class cl) {
        if (!cl.isPrimitive())
            return "writeValue";
        if (cl == Boolean.TYPE)
            return "writeBoolean";
        else if (cl == Byte.TYPE)
            return "writeByte";
        else if (cl == Character.TYPE)
            return "writeChar16";
        else if (cl == Short.TYPE)
            return "writeShort";
        else if (cl == Integer.TYPE)
            return "writeInt";
        else if (cl == Long.TYPE)
            return "writeLong";
        else if (cl == Float.TYPE)
            return "writeFloat";
        else if (cl == Double.TYPE)
            return "writeDouble";
        else
            return "";
    }

    static Class java_rmi_Remote, java_rmi_RemoteException, java_lang_Exception;
    static {
        try {
            java_rmi_Remote = Class.forName("java.rmi.Remote");
        } catch (Exception e) {
            throw new Error(e.toString());
        }
    }
    static {
        try {
            java_rmi_RemoteException =
                Class.forName("java.rmi.RemoteException");
        } catch (Exception e) {
            throw new Error(e.toString());
        }
    }
    static {
        try {
            java_lang_Exception = Class.forName("java.lang.Exception");
        } catch (Exception e) {
            throw new Error(e.toString());
        }
    }

    static boolean isRemoteInterface(Class cl) {
        if (!java_rmi_Remote.isAssignableFrom(cl))
            return false;
        Method[] mths = cl.getMethods();
        for (int i = 0; i < mths.length; i++) {
            if (!throwsRemoteException(mths[i])) {
                throw new Error(
                    cl.getName()
                        + " is not a valid remote interface: "
                        + "method "
                        + mths[i]
                        + " must throw java.rmi.RemoteException "
                        + "or one of its superclasses");
            }
        }
        return true;
    }

    static boolean throwsRemoteException(Method mth) {
        Class[] excTypes = mth.getExceptionTypes();
        for (int i = 0; i < excTypes.length; i++) {
            if (excTypes[i].isAssignableFrom(java_rmi_RemoteException)) {
                return true;
            }
        }
        return false;
    }

    static Vector getRemoteInterfaces(Class cl, boolean testDirect) {
        // Get all the remote interfaces, both direct and those inherited
        // from superclasses, that the class implements.
        // Check that the class directly implements (declared in its
        // implements clause) a remote interface.
        // Note that java.lang.Class.getInterfaces() returns interfaces
        // that are explicitly declared plus their super-interfaces
        // but it does not return interfaces of super-classes.
        Class[] directItfs = cl.getInterfaces();
        Vector remItfs = new Vector(directItfs.length);
        // BD May 22 2000: added direct handling of an interface
        if (cl.isInterface() && isRemoteInterface(cl)) {
            remItfs.addElement(cl);
        }
        // BD May 22 2000: end
        for (int i = 0; i < directItfs.length; i++) {
            Class itf = directItfs[i];
            if (isRemoteInterface(itf) && (!remItfs.contains(itf))) {
                remItfs.addElement(itf);
            }
        }
        if (testDirect && remItfs.isEmpty()) {
            throw new Error(
                cl.getName()
                    + " does not directly implement a remote interface "
                    + "(an interface that extends or is java.rmi.Remote)");
        }
        Class sc = cl.getSuperclass();
        if (sc != null) {
            Vector sc_remItfs = getRemoteInterfaces(sc, false);
            for (int i = 0; i < sc_remItfs.size(); i++) {
                Class sc_itf = (Class) sc_remItfs.elementAt(i);
                if (!(remItfs.contains(sc_itf))) {
                    remItfs.addElement(sc_itf);
                }
            }
        }
        return remItfs;
    }

    static boolean equalRemoteMethods(Method m1, Method m2) {
        // Test if two remote methods are equal.
        // Standard java.lang.reflect.Method.equals can not be used because
        // the declaring interface of the two methods are allowed to be
        // different under RMI semantics. The equality defined here just
        // tests if the method names and parameter types are equal.
        if (m1.getName().equals(m2.getName())) {
            Class[] params1 = m1.getParameterTypes();
            Class[] params2 = m2.getParameterTypes();
            if (params1.length == params2.length) {
                for (int i = 0; i < params1.length; i++) {
                    if (params1[i] != params2[i])
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    static MethodContext[] getRemoteMethodContexts(Class cl, Vector remItfs) {
        // Don't need to test if the class implements all the methods of
        // the remote interfaces; this will have been checked by previous
        // compilation of the class. Just extract those methods of the
        // class that implement one of the remote interfaces.
        // The result is sorted by lexicographic order of method signatures.
        // Note that we assume here that the interfaces are really remote;
        // no tests are done here for this.
        Method[] mths = cl.getMethods();
        int nbMths = mths.length;
        Vector unsortedMths = new Vector(nbMths);
        Hashtable saveItfs = new Hashtable(nbMths);
        int nbRemItfs = remItfs.size();
        Method[][] allItfMths = new Method[nbRemItfs][];
        int nbRemMths = 0;
        for (int i = 0; i < nbMths; i++) {
            Method mth = mths[i];
            boolean mthOK = false;
            for (int j = 0;(j < nbRemItfs && !mthOK); j++) {
                // check all the methods of the jth remote interface to find
                // a remote method that is "equal" to the current (ith) method
                // of the class. The first time around that the jth remote
                // interface is checked, store its methods for later use.
                Class remItf = (Class) remItfs.elementAt(j);
                if (allItfMths[j] == null) {
                    allItfMths[j] = remItf.getMethods();
                }
                Method[] itfMths = allItfMths[j];
                for (int k = 0;(k < itfMths.length && !mthOK); k++) {
                    Method itfMth = itfMths[k];
                    if (equalRemoteMethods(mth, itfMth)) {
                        mthOK = true;
                        nbRemMths++;
                        unsortedMths.addElement(mth);
                        saveItfs.put(mth, remItf);
                    }
                }
            }
        }
        Method[] sortedMths = sortMethodsBySignature(unsortedMths);
        MethodContext[] result = new MethodContext[nbRemMths];
        for (int i = 0; i < nbRemMths; i++) {
            Method mth = sortedMths[i];
            result[i] = new MethodContext(mth, i, (Class) saveItfs.get(mth));
        }
        return result;
    }

    static String signature(Method mth) {
        // return input signature of method
        String result;
        result = mth.getName() + "(";
        Class[] paramTypes = mth.getParameterTypes();
        int len = paramTypes.length;
        for (int i = 0; i < len; i++) {
            result = result + paramTypes[i].getName();
            if (i != len - 1)
                result = result + ", ";
        }
        result = result + ")";
        return result;
    }

    static Method[] sortMethodsBySignature(Vector mths) {
        // sorted by increasing lexicographical order of input signatures
        int nbMths = mths.size();
        Method[] result = new Method[nbMths];
        Method current, other;
        for (int i = 0; i < nbMths; i++) {
            result[i] = (Method) mths.elementAt(i);
        }
        String currentsig;
        for (int i = 0; i < nbMths; i++) {
            current = result[i];
            currentsig = signature(current);
            for (int j = i + 1; j < nbMths; j++) {
                other = result[j];
                if (currentsig.compareTo(signature(other)) > 0) {
                    result[j] = current;
                    current = other;
                    currentsig = signature(current);
                }
            }
            result[i] = current;
        }
        return result;
    }

    static Class[] getSortedExceptions(MethodContext mthctx) {
        // Sorted by increasing inheritance (superclass) order.
        // During the process, we note in the method context
        // if java.rmi.RemoteException or a superclass, or
        // java.lang.Exception were declared exceptions.
        Class[] excTypes = mthctx.mth.getExceptionTypes();
        int nbTypes = excTypes.length;
        Class[] result = new Class[nbTypes];
        Class current, other;
        for (int i = 0; i < nbTypes; i++) {
            current = excTypes[i];
            if (current.equals(java_lang_Exception)) {
                mthctx.declaresJavaLangException = true;
            }
            if (current.isAssignableFrom(java_rmi_RemoteException)) {
                mthctx.declaresJavaRmiRemoteException = true;
            }
            for (int j = i + 1; j < nbTypes; j++) {
                other = excTypes[j];
                if (current.isAssignableFrom(other)) {
                    excTypes[j] = current;
                    current = other;
                }
            }
            result[i] = current;
        }
        return result;
    }

    static void compileFile(CompilerContext cCtx, String fullFileName)
        throws Exception {
        String command =
            cCtx.javaCompiler
                + " -d "
                + cCtx.classDir
                + " "
                + fullFileName;

        String classpath = "";
        if (cCtx.classPath != null) {
            classpath =
                cCtx.classPath
                    + System.getProperty("path.separator", "")
                    + System.getProperty("java.class.path", "");
        } else {
            classpath = System.getProperty("java.class.path", "");
        }

        String[] env = new String[] { "CLASSPATH=" + classpath };
        Utils.trace("compiling source with command: " + command, cCtx);
        Utils.trace(" and classpath " + classpath, cCtx);

        Process proc = Runtime.getRuntime().exec(command, env);

        Thread stdoutThread =
            new Thread(
                new RunnableStreamListener(
                    new BufferedReader(
                        new InputStreamReader(proc.getInputStream())),
                    System.out),
                "stdout listener for " + cCtx.javaCompiler);
        stdoutThread.start();

        Thread stderrThread =
            new Thread(
                new RunnableStreamListener(
                    new BufferedReader(
                        new InputStreamReader(proc.getErrorStream())),
                    System.err),
                "stderr listener for " + cCtx.javaCompiler);
        stderrThread.start();
        int n = proc.waitFor();
        if (n == 0) {
            Utils.trace("successfully compiled source", cCtx);
        } else {
            Utils.trace(
                cCtx.javaCompiler
                    + " compilation ended abnormally with code "
                    + n, cCtx);
            throw new Exception("compilation ended abnormally with code " + n);
        }
    }

    static void deleteFile(CompilerContext cmpCtx, String fileName) {
        if (fileName == null || fileName == "")
            return;
        File f = new File(fileName);
        if (f.exists()) {
            Utils.trace("deleting file " + fileName, cmpCtx);
            f.delete();
        }
    }

}

class RunnableStreamListener implements Runnable {

    BufferedReader is;
    PrintStream ps;

    RunnableStreamListener(BufferedReader istream, PrintStream pstream) {
        is = istream;
        ps = pstream;
    }

    public void run() {
        String line;
        try {
            while ((line = is.readLine()) != null)
                ps.println(line);
        } catch (IOException e) {
            ps.println(e.toString());
        }
    }
}
