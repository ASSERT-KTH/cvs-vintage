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
 * 
 */
package org.objectweb.carol.cmi.compiler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This is a container class containing information relevant to a remote
 * class being processed by the stub compiler.
 */
public class ClassContext {

    String clFullName, pkgName = "", clName, genDirName;
    boolean inGlobalPkg = true;
	private CompilerContext cCtx;
    private Class cl;

    ClassContext(CompilerContext cctx, String fullName) throws Exception {
        clFullName = fullName;
        cCtx = cctx;
        int n = clFullName.lastIndexOf(".");
        if (n < 0) {
            clName = clFullName;
            genDirName = cctx.srcDir;
        } else {
            inGlobalPkg = false;
            pkgName = clFullName.substring(0, n);
            clName = clFullName.substring(n + 1);
            genDirName =
                cctx.srcDir
                    + File.separator
                    + pkgName.replace('.', File.separatorChar);
        }
        Utils.trace("loading class " + clFullName, cctx);
        // load the class but don't initialize it
        cl = loadClass();
    }

    private Class loadClass() throws ClassNotFoundException, MalformedURLException {
        ClassLoader cl;
        if (cCtx.classPath != null) {
            String classpath = cCtx.classPath + System.getProperty("path.separator", "") + System.getProperty("java.class.path", "");
            Vector nurls = new Vector();
            StringTokenizer st = new StringTokenizer(classpath, System.getProperty("path.separator", ""));
            while (st.hasMoreTokens()) {
                String url = st.nextToken();
                URL u = (new File(url)).toURL();
                nurls.addElement(u);
            }
            URL urls[] = new URL[nurls.size()];
            nurls.copyInto(urls);
            cl = new URLClassLoader(urls, null);
        } else {
            cl = this.getClass().getClassLoader();
        }
        return cl.loadClass(clFullName);
    }

    Vector getRemoteInterfaces() {
        return Utils.getRemoteInterfaces(cl, true);
    }

    MethodContext[] getRemoteMethodContexts() {
        return Utils.getRemoteMethodContexts(cl, getRemoteInterfaces());
    }

    private int chooserCnt = 0;

    String getChooserName() {
        chooserCnt++;
        return "$chooser_" + chooserCnt;
    }
}
