/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/SunJavaCompiler.java,v 1.5 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.5 $
 * $Date: 2004/02/23 02:45:12 $
 *
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.jasper.compiler;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The default compiler. This is the javac present in JDK 1.1.x and
 * JDK 1.2.  
 *
 * At some point we need to make sure there is a class like this for
 * JDK 1.3, and other javac-like animals that people want to use. 
 *
 * @author Anil K. Vijendran
 */
public class SunJavaCompiler implements JavaCompiler {

    String encoding;
    String classpath; // ignored
    String compilerPath;
    String outdir;
    OutputStream out;
    boolean classDebugInfo=false;

    /**
     * Specify where the compiler can be found
     */ 
    public void setCompilerPath(String compilerPath) {
        // not used by the SunJavaCompiler
	this.compilerPath = compilerPath;
    }

    /**
     * Set the encoding (character set) of the source
     */ 
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * Set the class path for the compiler
     */ 
    public void setClasspath(String classpath) {
      this.classpath = classpath;
    }

    /**
     * Set the output directory
     */ 
    public void setOutputDir(String outdir) {
      this.outdir = outdir;
    }

    /**
     * Set where you want the compiler output (messages) to go 
     */ 
    public void setMsgOutput(OutputStream out) {
      this.out = out;
    }

    /**
     * Set where you want the compiler output (messages) to go 
     */ 
    public void setOut(OutputStream out) {
        this.out = out;
    }
    
    /**
     * Set if you want debugging information in the class file 
     */ 
    public void setClassDebugInfo(boolean classDebugInfo) {
        this.classDebugInfo = classDebugInfo;
    }

    ClassLoader loader=null;
    public void setLoader( ClassLoader cl  ) {
        loader=cl;
    }

    public boolean compile(String source) {

        try {
            Class c;
            if( loader==null ) 
                c = Class.forName("sun.tools.javac.Main");
            else
                c=loader.loadClass("sun.tools.javac.Main");
            
            Constructor cons =
                c.getConstructor(new Class[] { OutputStream.class,
                                               String.class });
            
            Object compiler = cons.newInstance(new Object[] { out,
                                                              "jsp->javac" });

            // Call the compile() method
            Method compile = c.getMethod("compile",
                                         new Class [] { String[].class });

            String[] args;

            if (classDebugInfo) {
                args = new String[]
                    {
                        "-g",
                        "-encoding", encoding,
                        "-classpath", classpath,
                        "-d", outdir,
                        source
                    };
            } else {
                args = new String[]
                    {
                        "-encoding", encoding,
                        "-classpath", classpath,
                        "-d", outdir,
                        source
                    };
            }
            Boolean ok =
                (Boolean)compile.invoke(compiler,
                                        new Object[] {args});
            return ok.booleanValue();
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
            return false;
        } 
    }
}
