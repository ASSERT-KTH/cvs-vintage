/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author nieuviar
 *
 */
public class Compiler {
    private boolean keep = false;
    private boolean noc = false;
    private String compiler = "javac";
    private String classPath = null;
    private String genConf = null;
    private ArrayList conf = new ArrayList();
    private ArrayList classes = new ArrayList();
    private String srcDir = null;
    private String destDir = ".";
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public void configure(String[] args) throws CompilerException {
        int i = 0;
        while (i < args.length) {
            String arg = args[i++];
            if (arg.equals("-keep") || arg.equals("-keepgenerated")) {
                keep = true;
            } else if (arg.equals("-noc")) {
                noc = true;
            } else if (arg.equals("-c")) {
                if (i == args.length) {
                    throw new CompilerException("-c : missing argument");
                }
                compiler = args[i++];
            } else if (arg.equals("-classpath")) {
                if (i == args.length) {
                    throw new CompilerException("-classpath : missing argument");
                }
                classPath = args[i++];
            } else if (arg.equals("-genconf")) {
                if (i == args.length) {
                    throw new CompilerException("-genconf : missing argument");
                }
                genConf = args[i++];
            } else if (arg.equals("-conf")) {
                if (i == args.length) {
                    throw new CompilerException("-conf : missing argument");
                }
                conf.add(args[i++]);
            } else if (arg.equals("-d")) {
                if (i == args.length) {
                    throw new CompilerException("-d : missing argument");
                }
                destDir = args[i++];
            } else if (arg.equals("-s")) {
                if (i == args.length) {
                    throw new CompilerException("-s : missing argument");
                }
                srcDir = args[i++];
            } else if (arg.startsWith("-")) {
                throw new CompilerException(arg + ": unknown option");
            } else {
                classes.add(arg);
            }
        }
        if (srcDir == null) {
            srcDir = destDir;
        }
        if (noc) {
            keep = true;
        }
        if (classPath != null) {
            classLoader = buildClassLoader(classPath);
        }
    }

    public void run() throws CompilerException {
        if (genConf != null) {
            if (conf.size() != 0) {
                System.err.println("options -conf and -genconf are not compatible");
                System.exit(1);
            }
            generateClusterConfExample();
        } else {
            Conf cconf = new Conf(classLoader, classes);
            Iterator i = conf.iterator();
            while (i.hasNext()) {
                cconf.loadConfig((String) i.next());
            }
            i = classes.iterator();
            while (i.hasNext()) {
                String className = (String) i.next();
                if (className == null || className == "") {
                    throw new CompilerException("Empty class name to compile");
                }
                TemplateCompiler tc =
                    new TemplateCompiler(this, cconf.getClassConf(className));
                compileAndRemove(tc.genConfig());
                compileAndRemove(tc.genStub());

            }
        }
    }

    public void compileAndRemove(String fileName) throws CompilerException {
        try {
            if (!noc) {
                Utils.compileFile(this, fileName);
            }
        } finally {
            if (!keep) {
                File f = new File(fileName);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    private void generateClusterConfExample()
        throws CompilerException {
        try {
            FileWriter f = new FileWriter(genConf);
            f.write("<!DOCTYPE cluster-config PUBLIC\n");
            f.write("\t\"\"\n");
            f.write("\t\"cluster-config-0.2.dtd\">\n\n");
            f.write("<cluster-config>\n\n");
            Iterator i = classes.iterator();
            while (i.hasNext()) {
                String className = (String) i.next();
                Class cl;
                try {
                    cl = loadClass(className);
                } catch (ClassNotFoundException e1) {
                    throw new CompilerException("class not found " + className, e1);
                }
                f.write("<class>\n\t<name>" + className + "</name>\n");
                Method[] remMths = getRemoteMethods(cl);
                for (int j = 0; j < remMths.length; j++) {
                    f.write("\t<method>\n\t\t<signature>");
                    f.write(new MethodProto(remMths[j]).toString());
                    f.write("</signature>\n\t\t<rr/>\n\t</method>\n");
                }
                f.write("</class>\n\n");
            }
            f.write("</cluster-config>\n");
            f.close();
        } catch (IOException e) {
            throw new CompilerException(genConf, e);
        }
    }

    public Class loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    public static boolean isRemoteItf(Class cl) {
        // The interface must extend java.rmi.Remote
        if (!cl.isInterface() || !Remote.class.isAssignableFrom(cl)) {
            return false;
        }

        // Each method of this interface must throw java.rmi.RemoteException
        Method[] m = cl.getMethods();
        for (int i=0; i<m.length; i++) {
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

    public static Set getRemoteItfs(Class cl) {
        Set remItfs = new HashSet();
        while (cl != null) {
            Class[] itfs = cl.getInterfaces();
            if (isRemoteItf(cl)) {
                remItfs.add(cl);
            }
            for (int i=0; i<itfs.length; i++) {
                Class itf = itfs[i];
                if (isRemoteItf(itf)) {
                    remItfs.add(itf);
                }
            }
            cl = cl.getSuperclass();
        }
        return remItfs;
    }

    public static class MethodComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Method m1 = (Method)o1;
            Method m2 = (Method)o2;
            int r = m1.getName().compareTo(m2.getName());
            if (r != 0) return r;
            Class[] p1 = m1.getParameterTypes();
            Class[] p2 = m2.getParameterTypes();
            int i = 0;
            while ((i < p1.length) && (i < p2.length)) {
                r = p1[i].getName().compareTo(p2[i].getName());
                if (r != 0) return r;
                i++;
            }
            if (i == p1.length) {
                if (i == p2.length) {
                    return 0;
                }
                return -1;
            }
            return 1;
        }
    }

    private static MethodComparator methodComparator = new MethodComparator();

    // Return remote methods, sorted lexigraphically
    public static Method[] getRemoteMethods(Class cl) {
        TreeSet remMethods = new TreeSet(methodComparator);
        // Get the methods of the remote interfaces
        Set itfs = getRemoteItfs(cl);
        TreeSet itfMethods = new TreeSet(methodComparator);
        Iterator it = itfs.iterator();
        while (it.hasNext()) {
            Class itf = (Class) it.next();
            Method[] m = itf.getMethods();
            for (int i=0; i<m.length; i++) {
                itfMethods.add(m[i]);
            }
        }

        TreeSet clMethods = new TreeSet(methodComparator);
        Method[] methods = cl.getMethods();
        for (int i=0; i<methods.length; i++) {
            clMethods.add(methods[i]);
        }

        Iterator i1 = clMethods.iterator();
        Iterator i2 = itfMethods.iterator();
        if (i1.hasNext() && i2.hasNext()) {
            Method m1 = (Method) i1.next();
            Method m2 = (Method) i2.next();
            do {
                int d = methodComparator.compare(m1, m2);
                if (d > 0) {
                    m2 = (i2.hasNext()) ? (Method)i2.next() : null;
                } else if (d < 0) {
                    m1 = (i1.hasNext()) ? (Method)i1.next() : null;
                } else {
                    // Give the Method object of the interface
                    remMethods.add(m2);
                    m1 = (i1.hasNext()) ? (Method)i1.next() : null;
                    m2 = (i2.hasNext()) ? (Method)i2.next() : null;
                }
            } while ((m1 != null) && (m2 != null));            
        }

        int i = remMethods.size();
        Method[] m = new Method[i];
        it = remMethods.iterator();
        i = 0;
        while (it.hasNext()) {
            m[i++] = (Method) it.next();
        }
        return m;
    }

    public static ClassLoader buildClassLoader(String classPath) {
        String classpath = classPath + System.getProperty("path.separator", "") + System.getProperty("java.class.path", "");
        Vector nurls = new Vector();
        StringTokenizer st = new StringTokenizer(classpath, System.getProperty("path.separator", ""));
        while (st.hasMoreTokens()) {
            String url = st.nextToken();
            if (!url.equals("")) {
                try {
                    URL u = (new File(url)).toURL();
                    nurls.addElement(u);
                } catch (MalformedURLException e) {
                    // Do not add the URL
                }
            }
        }
        if (nurls.size() == 0) {
            return Thread.currentThread().getContextClassLoader();
        }
        URL urls[] = new URL[nurls.size()];
        nurls.copyInto(urls);
        return new URLClassLoader(urls, null);
    }

    public static void usage() {
        System.out.println(
            "Usage: java "
                + Compiler.class.getName()
                + " [options] [class names]");
        System.out.println();
        System.out.println(
            "Options:\n"
                + "  -keep                do not delete generated source files\n"
                + "  -keepgenerated       same as -keep\n"
                + "  -noc                 do not compile generated source files (implies -keep)\n"
                + "  -c <java compiler>   compile generated source files with this java compiler\n"
                + "                       (defaults to javac)\n"
                + "  -classpath <path>    extra classpath passed to -c compiler\n"
                + "  -d <directory>       root directory for generated class files "
                + "                       (defaults to current directory)\n"
                + "  -s <directory>       root directory for generated source files\n"
                + "                       (defaults to -d directory)\n"
                + "  -conf <xml-file>     specify the XML configuration file to use\n"
                + "  -genconf <xml-file>  generate an XML configuration file example\n");
    }

    public static void generate(String[] args) throws CompilerException {
        Compiler cc = new Compiler();
        cc.configure(args);
        cc.run();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return; 
        }
        try {
            generate(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String getSrcDir() {
        return srcDir;
    }

    public boolean isCompile() {
        return !noc;
    }

    public String getCompiler() {
        return compiler;
    }

    public String getDestDir() {
        return destDir;
    }

    public String getClassPath() {
        return classPath;
    }
}
