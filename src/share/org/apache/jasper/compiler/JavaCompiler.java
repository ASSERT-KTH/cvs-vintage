/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/JavaCompiler.java,v 1.4 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.4 $
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

/**
 * If you want to plugin your own Java compiler, you probably want to
 * write a class that implements this interface. 
 *
 * @author Anil K. Vijendran
 * @author Sam Ruby
 */
public interface JavaCompiler {

    /**
     * Specify where the compiler can be found
     */ 
    void setCompilerPath(String compilerPath);

    /**
     * Set the encoding (character set) of the source
     */ 
    void setEncoding(String encoding);

    /**
     * Set the class path for the compiler
     */ 
    void setClasspath(String classpath);

    /**
     * Set the output directory
     */ 
    void setOutputDir(String outdir);

    /**
     * Set where you want the compiler output (messages) to go 
     */ 
    void setMsgOutput(OutputStream out);

    /**
     * Set if you want debugging information in the class file 
     */ 
    void setClassDebugInfo(boolean classDebugInfo);

    /**
     * Execute the compiler
     * @param source - file name of the source to be compiled
     */ 
    boolean compile(String source);

}

