/*
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jasper.runtime;

import java.io.IOException;
import java.util.Vector;

import org.apache.jasper.Constants;
import org.apache.jasper.Options;
import org.apache.tomcat.util.log.Log;

/**
 * Jsp compiler and runtime depends on special features from the
 * ClassLoader.
 *
 *  It has to allow run-time addition of class paths and provide
 *  access to the class path.
 *
 *  The loader is also responsible for detecting changes and reloading.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public abstract class JspLoader extends ClassLoader {
    protected ClassLoader parent;
    protected Options options;
    //    Object pd;

    /*
     * This should be factoried out
     */
    protected JspLoader() {
	super();
    }

    public void setParentClassLoader( ClassLoader cl) 
    {
	this.parent = cl;
    }

    // The only thing we use is getScratchDir !
    public void setOptions( Options options) {
	this.options = options;
    }

    protected Vector jars = new Vector();
    
    public void addJar(String jarFileName) throws IOException {
        if (!jars.contains(jarFileName)) {
            Constants.message("jsp.message.adding_jar",
                              new Object[] { jarFileName },
                              Log.DEBUG);
            
            jars.addElement(jarFileName);
        }
    }
    
    public String getClassPath() {
        StringBuffer cpath = new StringBuffer();
        String sep = System.getProperty("path.separator");

        for(int i = 0; i < jars.size(); i++) {
            cpath.append((String)jars.elementAt(i)+sep);
        }
        
        return cpath.toString();
    }
}
