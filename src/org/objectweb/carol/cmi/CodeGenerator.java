/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
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
 * $Id: CodeGenerator.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * Used to build the class of a cluster stub.
 *
 * @author Simon Nieuviarts
 */
class CodeGenerator implements Constants {

    /**
     * Remote class
     */
    private final Class remoteObjClass;

    /**
     * Distributor class
     */
    private final Distributor distrib;

    /**
     * Creates a CodeGenerator
     * @param remoteObjClass remote class
     * @param distrib Distributor class
     */
    public CodeGenerator(Class remoteObjClass, Distributor distrib) {
        this.remoteObjClass = remoteObjClass;
        this.distrib = distrib;
    }

    /**
     * Test if is remote interface
     * @param cl tested class
     * @return true if interface, else false
     */
    private static boolean isRemoteItf(Class cl) {
        // The interface must extend java.rmi.Remote
        if (!cl.isInterface() || !Remote.class.isAssignableFrom(cl)) {
            return false;
        }
        if (Remote.class.equals(cl)) {
            return false;
        }

        // Each method of this interface must throw java.rmi.RemoteException
        Method[] m = cl.getMethods();
        for (int i = 0; i < m.length; i++) {
            Class[] ex = m[i].getExceptionTypes();
            int j = 0;
            while ((j < ex.length) && !RemoteException.class.isAssignableFrom(ex[j])) {
                j++;
            }
            if (j == ex.length) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the remote interfaces associated with a class
     * @param cl class to scan
     * @return array of interfaces
     */
    private static ArrayList getRemoteItfs(Class cl) {
        ArrayList remItfs = new ArrayList();
        while (cl != null) {
            Class[] itfs = cl.getInterfaces();
            if (isRemoteItf(cl)) {
                remItfs.add(cl);
            }
            for (int i = 0; i < itfs.length; i++) {
                Class itf = itfs[i];
                if (isRemoteItf(itf)) {
                    remItfs.add(itf);
                }
            }
            cl = cl.getSuperclass();
        }
        return remItfs;
    }

    /**
     * Operations list for ASM
     */
    private static HashMap operations = null;

    /**
     * Operations definition (ASM)
     */
    private static int[] defOps = new int[] {ALOAD, ASTORE, ARETURN, AALOAD, AASTORE};

    /**
     * Get ASM operations for a given class
     * @param type the class
     * @return array of int containing the ASM operations
     */
    private static int[] getOperations(Class type) {
        if (operations == null) {
            HashMap ops = new HashMap();
            int[] iops = new int[] {ILOAD, ISTORE, IRETURN, IALOAD, IASTORE};
            ops.put(boolean.class, iops);
            ops.put(byte.class, iops);
            ops.put(short.class, iops);
            ops.put(char.class, iops);
            ops.put(int.class, iops);
            ops.put(float.class, new int[] {FLOAD, FSTORE, FRETURN, FALOAD, FASTORE});
            ops.put(long.class, new int[] {LLOAD, LSTORE, LRETURN, LALOAD, LASTORE});
            ops.put(double.class, new int[] {DLOAD, DSTORE, DRETURN, DALOAD, DASTORE});
            operations = ops;
        }
        int[] ops = (int[]) operations.get(type);
        return (ops != null) ? ops : defOps;
    }

    /**
     * Code visitor (ASM)
     */
    private CodeVisitor cv;

    /**
     * Visit a class inside Load
     * @param type class to visit
     * @param index index
     */
    private void insLoad(Class type, int index) {
        cv.visitVarInsn(getOperations(type)[0], index);
    }

    /**
     * Visit a class inside Store
     * @param type class to visit
     * @param index index
     */
    private void insStore(Class type, int index) {
        cv.visitVarInsn(getOperations(type)[1], index);
    }

    /**
     * Visit a class inside Return
     * @param type class to visit
     *      */
    private void insReturn(Class type) {
        cv.visitInsn(getOperations(type)[2]);
    }

    /**
     * Visit a class inside Array Load
     * @param type class to visit
     */
    private void insArrLoad(Class type) {
        cv.visitInsn(getOperations(type)[3]);
    }

    /**
     * Visit a class inside Array Store
     * @param type class to visit
     */
    private void insArrStore(Class type) {
        cv.visitInsn(getOperations(type)[4]);
    }

    /**
     * Push Int
     * @param val value
     */
    private void pushInt(int val) {
        switch (val) {
            case -1:
                cv.visitInsn(ICONST_M1);
                break;
            case 0:
                cv.visitInsn(ICONST_0);
                break;
            case 1:
                cv.visitInsn(ICONST_1);
                break;
            case 2:
                cv.visitInsn(ICONST_2);
                break;
            case 3:
                cv.visitInsn(ICONST_3);
                break;
            case 4:
                cv.visitInsn(ICONST_4);
                break;
            case 5:
                cv.visitInsn(ICONST_5);
                break;
            default:
                cv.visitIntInsn(BIPUSH, val);
        }
    }

    /**
     * Hashmap object type
     */
    private static HashMap objectType = null;

    /**
     * Get Object Type by primitive type name
     * @param primitiveTypeName primitive type name
     * @return object type
     */
    private static String getObjectType(String primitiveTypeName) {
        if (objectType == null) {
            HashMap ot = new HashMap();
            ot.put("byte", "java/lang/Byte");
            ot.put("char", "java/lang/Char");
            ot.put("double", "java/lang/Double");
            ot.put("float", "java/lang/Float");
            ot.put("int", "java/lang/Integer");
            ot.put("long", "java/lang/Long");
            ot.put("short", "java/lang/Short");
            ot.put("boolean", "java/lang/Boolean");
            objectType = ot;
        }
        return (String) objectType.get(primitiveTypeName);
    }

    /**
     * Get Object Type by primitive class name
     * @param primitiveType primitive class name
     * @return object type
     */
    private static String getObjectType(Class primitiveType) {
        return getObjectType(primitiveType.getName());
    }

    /**
     * Get FQN by class
     * @param type class
     * @return FQN
     */
    private static String getFQN(Class type) {
        if (type.isPrimitive()) {
            return getObjectType(type);
        } else {
            return type.getName().replace('.', '/');
        }
    }

    /**
     * ClassWriter
     */
    private ClassWriter cw;

    /**
     * index for static class
     */
    private int staticClassIdx = 0;

    /**
     * map for statici class
     */
    private HashMap staticClassMap = new HashMap();


    /**
     * Push static class
     * @param thisClass class to visit
     * @param className classname
     */
    private void staticPushClass(String thisClass, String className) {
        String var = (String) staticClassMap.get(className);
        if (var == null) {
            var = "class$" + staticClassIdx++;
            staticClassMap.put(className, var);
            cw.visitField(ACC_STATIC + ACC_SYNTHETIC, var, "Ljava/lang/Class;", null, null);
        }
        String objType = getObjectType(className);
        if (objType != null) {
            cv.visitFieldInsn(GETSTATIC, objType, "TYPE", "Ljava/lang/Class;");
            return;
        }
        cv.visitFieldInsn(GETSTATIC, thisClass, var, "Ljava/lang/Class;");
        cv.visitInsn(DUP);
        Label l1 = new Label();
        cv.visitJumpInsn(IFNONNULL, l1);
        cv.visitInsn(POP);
        Label l2 = new Label();
        cv.visitLabel(l2);
        cv.visitLdcInsn(className);
        cv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
        Label l3 = new Label();
        cv.visitLabel(l3);
        cv.visitInsn(DUP);
        cv.visitFieldInsn(PUTSTATIC, thisClass, var, "Ljava/lang/Class;");
        cv.visitJumpInsn(GOTO, l1);

        Label l4 = new Label();
        cv.visitLabel(l4);
        cv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError");
        cv.visitInsn(DUP_X1);
        cv.visitInsn(SWAP);
        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "getMessage", "()Ljava/lang/String;");
        cv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V");
        cv.visitInsn(ATHROW);
        cv.visitLabel(l1);

        cv.visitTryCatchBlock(l2, l3, l4, "java/lang/ClassNotFoundException");
    }

    /**
     * Generates the code
     * @return bytes array corresponding to the generated code
     */
    byte[] generate() {
        String className = "org.objectweb.carol.cmi.stub." + distrib.getClass().getName();
        String clName = className.replace('.', '/');

        ArrayList remoteItfs = getRemoteItfs(remoteObjClass);

        Iterator itfIter = remoteItfs.iterator();
        HashMap remMethodsMap = new HashMap();
        while (itfIter.hasNext()) {
            Class itf = (Class) itfIter.next();
            Method[] methods = itf.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                remMethodsMap.put(m.getName() + "." + Type.getMethodDescriptor(m), m);
            }
        }

        Iterator it = remMethodsMap.values().iterator();
        ArrayList remMethods = new ArrayList();
        while (it.hasNext()) {
            remMethods.add(it.next());
        }

        cw = new ClassWriter(true);

        // The cluster stub implements every remote interface of the stubs
        int s = remoteItfs.size();
        String[] itfs = new String[s + 1];
        itfs[0] = "java/io/Serializable";
        for (int i = 0; i < s; i++) {
            itfs[i + 1] = Type.getInternalName((Class) remoteItfs.get(i));
        }

        cw.visit(V1_2, ACC_PUBLIC + ACC_SUPER, clName, "org/objectweb/carol/cmi/ClusterStub", itfs, null);

        // Parameter to pass to the choose method for remote method without
        // parameters
        cw.visitField(ACC_PRIVATE + ACC_STATIC, "noParam", "[Ljava/lang/Object;", null, null);

        cw.visitField(ACC_PRIVATE + ACC_STATIC, "staticInitException", "Ljava/lang/Throwable;", null, null);

        // Static fields that hold the Method object for each method
        for (int methIdx = 0; methIdx < remMethods.size(); methIdx++) {
            cw.visitField(ACC_PRIVATE + ACC_STATIC, "m" + methIdx, "Ljava/lang/reflect/Method;", null, null);
        }

        // Class initialization
        cv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        // noParam
        cv.visitInsn(ICONST_0);
        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        cv.visitFieldInsn(PUTSTATIC, clName, "noParam", "[Ljava/lang/Object;");

        // staticInitException
        cv.visitInsn(ACONST_NULL);
        cv.visitFieldInsn(PUTSTATIC, clName, "staticInitException", "Ljava/lang/Throwable;");

        {
            Label l0 = new Label();
            cv.visitLabel(l0);

            for (int methIdx = 0; methIdx < remMethods.size(); methIdx++) {
                Method method = (Method) remMethods.get(methIdx);
                Class[] paramTypes = method.getParameterTypes();
                int params = paramTypes.length;

                staticPushClass(clName, className);
                cv.visitLdcInsn(method.getName());

                pushInt(params);
                cv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
                for (int i = 0; i < params; i++) {
                    cv.visitInsn(DUP); // array
                    pushInt(i); // index
                    staticPushClass(clName, paramTypes[i].getName());
                    cv.visitInsn(AASTORE);
                }

                cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                        "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
                cv.visitFieldInsn(PUTSTATIC, clName, "m" + methIdx, "Ljava/lang/reflect/Method;");
            }

            Label l9 = new Label();
            cv.visitJumpInsn(GOTO, l9);

            // In case of exception
            Label l10 = new Label();
            cv.visitLabel(l10);
            cv.visitVarInsn(ASTORE, 0);
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(PUTSTATIC, clName, "staticInitException", "Ljava/lang/Throwable;");
            cv.visitLabel(l9);
            cv.visitInsn(RETURN);
            cv.visitTryCatchBlock(l0, l10, l10, "java/lang/NoSuchMethodException");
            cv.visitMaxs(0, 0); // computed
        }

        // Constructor
        cv = cw.visitMethod(ACC_PUBLIC, "<init>",
                "(Lorg/objectweb/carol/cmi/Distributor;Lorg/objectweb/carol/cmi/ServerStubList;)V",
                new String[] {"java/lang/Throwable"}, null);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitVarInsn(ALOAD, 2);
        cv.visitMethodInsn(INVOKESPECIAL, "org/objectweb/carol/cmi/ClusterStub", "<init>",
                "(Lorg/objectweb/carol/cmi/Distributor;Lorg/objectweb/carol/cmi/ServerStubList;)V");
        cv.visitFieldInsn(GETSTATIC, clName, "staticInitException", "Ljava/lang/Throwable;");
        Label l0 = new Label();
        cv.visitJumpInsn(IFNULL, l0);
        cv.visitFieldInsn(GETSTATIC, clName, "staticInitException", "Ljava/lang/Throwable;");
        cv.visitInsn(ATHROW);
        cv.visitLabel(l0);
        cv.visitInsn(RETURN);
        cv.visitMaxs(3, 3);

        // writeExternal
        cv = cw.visitMethod(ACC_PUBLIC, "writeExternal", "(Ljava/io/ObjectOutput;)V",
                new String[] { "java/io/IOException" }, null);
        cv.visitTypeInsn(NEW, "java/io/IOException");
        cv.visitInsn(DUP);
        cv.visitLdcInsn("This class should never be directly serialized");
        cv.visitMethodInsn(INVOKESPECIAL, "java/io/IOException", "<init>", "(Ljava/lang/String;)V");
        cv.visitInsn(ATHROW);
        cv.visitMaxs(3, 2);

        // readExternal
        cv = cw.visitMethod(ACC_PUBLIC, "readExternal", "(Ljava/io/ObjectInput;)V", new String[] {
                "java/io/IOException", "java/lang/ClassNotFoundException"}, null);
        cv.visitTypeInsn(NEW, "java/io/IOException");
        cv.visitInsn(DUP);
        cv.visitLdcInsn("This class should never be directly deserialized");
        cv.visitMethodInsn(INVOKESPECIAL, "java/io/IOException", "<init>", "(Ljava/lang/String;)V");
        cv.visitInsn(ATHROW);
        cv.visitMaxs(3, 2);

        // writeReplace
        cv = cw.visitMethod(ACC_PRIVATE, "writeReplace", "()Ljava/lang/Object;",
                new String[] {"java/io/ObjectStreamException"}, null);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(INVOKESTATIC, "org/objectweb/carol/cmi/StubBuilder", "getReplacement",
                "(Lorg/objectweb/carol/cmi/ClusterStub;)Lorg/objectweb/carol/cmi/StubBuilder;");
        cv.visitInsn(ARETURN);
        cv.visitMaxs(1, 1);

        // generate remote methods
        for (int methIdx = 0; methIdx < remMethods.size(); methIdx++) {
            Method method = (Method) remMethods.get(methIdx);
            String itf = Type.getInternalName(method.getDeclaringClass());

            Class[] exceptions = method.getExceptionTypes();
            String[] excepts = new String[exceptions.length];
            for (int i = 0; i < exceptions.length; i++) {
                excepts[i] = Type.getInternalName(exceptions[i]);
            }
            Class[] paramTypes = method.getParameterTypes();
            int params = paramTypes.length;
            int paramOffset[] = new int[params];
            int lo = 1;
            for (int i = 0; i < params; i++) {
                paramOffset[i] = lo++;
                if (paramTypes[i].equals(long.class) || paramTypes[i].equals(double.class)) {
                    lo++;
                }
            }
            Class retType = method.getReturnType();

            cv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), excepts, null);
            l0 = new Label();
            cv.visitLabel(l0);

            // Create an array with the parameters encapsulated
            if (params > 0) {
                pushInt(params);
                cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                for (int i = 0; i < params; i++) {
                    cv.visitInsn(DUP); // array
                    pushInt(i); // index
                    if (paramTypes[i].isPrimitive()) {
                        String type = getObjectType(paramTypes[i]);
                        cv.visitTypeInsn(NEW, type);
                        cv.visitInsn(DUP);
                        insLoad(paramTypes[i], paramOffset[i]);
                        cv.visitMethodInsn(INVOKESPECIAL, type, "<init>", "(" + Type.getDescriptor(paramTypes[i])
                                + ")V");
                    } else {
                        cv.visitVarInsn(ALOAD, paramOffset[i]);
                    }
                    cv.visitInsn(AASTORE);
                }
            } else {
                cv.visitFieldInsn(GETSTATIC, clName, "noParam", "[Ljava/lang/Object;");
            }
            cv.visitVarInsn(ASTORE, lo + 0); // params

            // Call the choose method
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, clName, "distrib", "Lorg/objectweb/carol/cmi/Distributor;");
            cv.visitFieldInsn(GETSTATIC, clName, "m" + methIdx, "Ljava/lang/reflect/Method;");
            cv.visitVarInsn(ALOAD, lo + 0); // params
            cv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/carol/cmi/Distributor", "choose",
                    "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Lorg/objectweb/carol/cmi/StubData;");
            cv.visitVarInsn(ASTORE, lo + 1); // stubdata

            // Call the getStub method
            cv.visitVarInsn(ALOAD, lo + 1); // stubdata
            cv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/carol/cmi/StubData", "getStub", "()Ljava/rmi/Remote;");
            cv.visitTypeInsn(CHECKCAST, itf);
            cv.visitVarInsn(ASTORE, lo + 2); // stub

            // try {
            Label l1 = new Label();
            cv.visitLabel(l1);

            // Call the remote method
            if (retType.isPrimitive() && (!retType.equals(void.class))) {
                cv.visitTypeInsn(NEW, getObjectType(retType));
                cv.visitInsn(DUP);
            }
            cv.visitVarInsn(ALOAD, lo + 2); // stub
            for (int i = 0; i < params; i++) {
                // push parameters
                insLoad(paramTypes[i], paramOffset[i]);
            }
            cv.visitMethodInsn(INVOKEINTERFACE, itf, method.getName(), Type.getMethodDescriptor(method));
            if (retType.equals(void.class)) {

            } else if (retType.isPrimitive()) {
                cv.visitMethodInsn(INVOKESPECIAL, getObjectType(retType), "<init>", "(" + Type.getDescriptor(retType)
                        + ")V");
                cv.visitVarInsn(ASTORE, lo + 3);
            } else {
                cv.visitVarInsn(ASTORE, lo + 3);
            }

            // call onReturn
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, clName, "distrib", "Lorg/objectweb/carol/cmi/Distributor;");
            cv.visitVarInsn(ALOAD, lo + 1); // stubdata
            if (retType.equals(void.class)) {
                cv.visitInsn(ACONST_NULL);
            } else {
                cv.visitVarInsn(ALOAD, lo + 3);
            }
            cv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/carol/cmi/Distributor", "onReturn",
                    "(Lorg/objectweb/carol/cmi/StubData;Ljava/lang/Object;)Lorg/objectweb/carol/cmi/Decision;");
            cv.visitVarInsn(ASTORE, lo + 4); // decision
            cv.visitVarInsn(ALOAD, lo + 4); // decision
            cv.visitFieldInsn(GETFIELD, "org/objectweb/carol/cmi/Decision", "decision", "I");

            Label l2 = new Label();
            cv.visitJumpInsn(IFNE, l2);

            // return the result
            if (retType.equals(void.class)) {
                cv.visitInsn(RETURN);
            } else {
                cv.visitVarInsn(ALOAD, lo + 4); // decision
                cv.visitFieldInsn(GETFIELD, "org/objectweb/carol/cmi/Decision", "retVal", "Ljava/lang/Object;");
                cv.visitTypeInsn(CHECKCAST, getFQN(retType));
                if (retType.isPrimitive()) {
                    cv.visitMethodInsn(INVOKEVIRTUAL, getObjectType(retType), retType.getName() + "Value", "()"
                            + Type.getDescriptor(retType));
                }
                insReturn(retType);
            }
            cv.visitJumpInsn(GOTO, l2);

            // the end of the try block
            Label l3 = new Label();

            // now catch blocks
            Label l = l3;

            for (int i = 0; i < exceptions.length; i++) {
                if (i != 0) {
                    cv.visitJumpInsn(GOTO, l2);
                }
                cv.visitLabel(l);
                cv.visitTryCatchBlock(l1, l3, l, getFQN(exceptions[i]));
                cv.visitVarInsn(ASTORE, lo + 3); // exception

                // call onException
                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(GETFIELD, clName, "distrib", "Lorg/objectweb/carol/cmi/Distributor;");
                cv.visitVarInsn(ALOAD, lo + 1); // stubdata
                cv.visitVarInsn(ALOAD, lo + 3); // exception
                cv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/carol/cmi/Distributor", "onException",
                        "(Lorg/objectweb/carol/cmi/StubData;Ljava/lang/Exception;)Lorg/objectweb/carol/cmi/Decision;");
                cv.visitVarInsn(ASTORE, lo + 4); // decision
                cv.visitVarInsn(ALOAD, lo + 4); // decision
                cv.visitFieldInsn(GETFIELD, "org/objectweb/carol/cmi/Decision", "decision", "I");

                Label l4 = new Label();
                cv.visitJumpInsn(IFNE, l4);

                // return a result
                if (method.getReturnType().equals(void.class)) {
                    cv.visitInsn(RETURN);
                } else {
                    cv.visitVarInsn(ALOAD, lo + 4); // decision
                    cv.visitFieldInsn(GETFIELD, "org/objectweb/carol/cmi/Decision", "retVal", "Ljava/lang/Object;");
                    cv.visitTypeInsn(CHECKCAST, getFQN(retType));
                    if (retType.isPrimitive()) {
                        cv.visitMethodInsn(INVOKEVIRTUAL, getObjectType(retType), retType.getName() + "Value", "()"
                                + Type.getDescriptor(retType));
                    }
                    insReturn(retType);
                }
                cv.visitLabel(l4);

                // do we have to throw the exception ? if not, restart the loop
                cv.visitVarInsn(ALOAD, lo + 4); // decision
                cv.visitFieldInsn(GETFIELD, "org/objectweb/carol/cmi/Decision", "decision", "I");
                cv.visitInsn(ICONST_2);
                cv.visitJumpInsn(IF_ICMPNE, l2);
                cv.visitVarInsn(ALOAD, lo + 3); // exception
                cv.visitInsn(ATHROW);
                l = new Label();
            }

            // end of catch blocks
            cv.visitLabel(l2);
            cv.visitJumpInsn(GOTO, l0); // restart loop

            cv.visitMaxs(0, 0); // will be computed

        }

        cv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
        cv.visitTypeInsn(NEW, "java/lang/StringBuffer");
        cv.visitInsn(DUP);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;");
        cv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
        cv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, "org/objectweb/carol/cmi/ClusterStub", "distrib",
                "Lorg/objectweb/carol/cmi/Distributor;");
        cv.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/carol/cmi/Distributor", "toContentsString",
                "()Ljava/lang/String;");
        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
        cv.visitInsn(ARETURN);
        cv.visitMaxs(3, 1);

        cw.visitEnd();

        return cw.toByteArray();
    }

}
