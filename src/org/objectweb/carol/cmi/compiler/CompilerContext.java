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
 * with contributions from:
 *   François Horn
 * 
 */

package org.objectweb.carol.cmi.compiler;

/**
 * This is a container class containing information relevant to a run
 * of the stub compiler.
 */
public class CompilerContext {

    public static final String version = "1.0";
    public boolean verbose = false;
    public boolean keep = false;
    public boolean compile = true;
    public String javaCompiler = "javac";
    public String classPath = null;
    public String srcDir = null;
    public String classDir = null;
    public String clusterCfgGen = null;

    public String base_package = "";

    //scope translations (-idl2pkg), used in BE_SimpleNode.getJavaPackName()
    java.util.Hashtable idl_to_package = new java.util.Hashtable();

    //rootDir is the root directory for all generated java classes. This parameter is
    //controlled by the -p option.

    public String rootDir = ".";

    public CompilerContext() {
    }
}
