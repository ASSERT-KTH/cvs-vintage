/*
 * @(#) JVMConfiguration.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
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
 *
 */
package org.objectweb.carol.util.bootstrap;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Class <code>JVMConfiguration</code> Is a Data structure representing a Java command 
 * This class is serializable and can be pass througth a RMI call (for the Java Deamon for example) 
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 *
 */

public class JVMConfiguration implements Serializable {
  
    /**
     * Boolean for jvm -server option
     */
    public boolean server = false;
 
    /**
     * Boolean for jvm -debug option
     */
    public boolean debug = false;
    
    /**
     * Properties for system properties (-D...) options
     */
    public Properties properties = new Properties();    

    /**
     * String Vector for classpath (path and jar file)
     * Be carful, this is for the moment
     * RJVM server System dependant 
     */
    public Vector classpath = new Vector();

    /**
     * String for -jar option 
     */
    public  String jarName = null;

    /**
     * String for classname
     */
    public  String className = null;

     /**
     * String for class main args [] option
     */
    public  String mainArgs = "";

    /**
     * Boolean -verbose option 
     */
    public boolean verbose = false;
 
    /**
     * Boolean -verbose:class option 
     */
    public boolean verboseClass = false;

    /**
     * Boolean -verbose:gc option 
     */
    public boolean verboseGC = false;

    /**
     * Boolean -verbose:jni option 
     */
    public boolean verboseJNI = false;

    /**
     * String Vector for non Standard options (-X...) 
     */
    public Vector nonStandard = new Vector();


    /**
     * empty constructor 
     * start with default
     */
    public JVMConfiguration() {
    }

    /**
     * Constuctor with the jvm string
     * This constructor is use "like a java ... command"
     * and parse the string
     *
     */
    public JVMConfiguration(String commandLine) throws ProcessException {
	StringTokenizer st = new StringTokenizer(commandLine);
	while (st.hasMoreTokens()) {
	    String opt= (String)st.nextToken();
	    if (opt.equals("-server")) {
		setServer(); 
	    } else if (opt.equals("-debug")) {
		setDebug();
	    } else if (opt.equals("-verbose")) {
		setVerbose();
	    } else if (opt.equals("-verbose:class")) {
		setVerboseClass();
	    } else if (opt.equals("-verbose:gc")) {
		setVerboseGC();
	    } else if (opt.equals("-verbose:jni")) {
		setVerboseJNI();
	    } else if (opt.startsWith("-D")) {
		StringTokenizer prop = new StringTokenizer(opt.substring(2), "=");
		if (prop.countTokens()==2) {		    
		    addProperty(prop.nextToken(), prop.nextToken());
		} else {
		    throw  new ProcessException("-D option is not valid");
		}
	    } else if (opt.startsWith("-X")) {
		addNonStandard(opt.substring(2));
	    } else if ((opt.startsWith("-classpath")) || (opt.startsWith("-cp"))) {
		StringTokenizer classP = new StringTokenizer(st.nextToken(), System.getProperty("path.separator"));
		if (classP.countTokens()!=0) {
		    while (classP.hasMoreTokens()) {
			addPath(classP.nextToken());
		    }
		} else {
		    throw  new ProcessException("-classpath or -cp option is not a valid path option"); 

		}
	    } else if (opt.equals("-jar")) {
		setJar(st.nextToken());
		if (st.hasMoreTokens()) {
		    mainArgs=" ";
		    while (st.hasMoreTokens()) {
		        mainArgs+=" "+st.nextToken();
		    }
		}
		break;
	    } else {
		// this is the class name
		setClass(opt);
		if (st.hasMoreTokens()) {
		    mainArgs=" ";
		    while (st.hasMoreTokens()) {
		        mainArgs+=" "+st.nextToken();
		    }
		}
		break;
	    }	
	}


    }
    

    /**
     * set jvm -server option
     */
    public void setServer() {
	server = true;
    }

    /**
     * set jvm -debug option
     */
    public void setDebug() {
	debug = true;
    } 

    /**
     * add jvm -D property
     */
    public void addProperty(String key, String value) {
	properties.put(key, value);
    } 

    /**
     * add path for the jvm
     */
    public void addPath(String path) {
	classpath.add(path);
    } 
   
    /**
     * set -verbose option 
     */
    public void setVerbose() {
	verbose = true;
    }  

    /**
     * set -verbose:class option 
     */
    public void setVerboseClass() {
	verboseClass = true;
    }  

    /**
     * set -verbose:gc option 
     */
    public void setVerboseGC() {
	verboseGC = true;
    }  

    /**
     * set -verbose:jni option 
     */
    public void setVerboseJNI() {
	verboseJNI = true;
    }  

    /**
     * ste non standard option 
     * (for example bootclasspath:/usr/local/lib/foo/foo.jar  pass
     * the -Xbootclasspath:/usr/local/lib/foo/foo.jar to the jvm)
     */
    public void addNonStandard(String option) {
	nonStandard.add(option);
    }

    /**
     * set the main class namle for the jvm    
     * @throws ProcessException if the jar option is set
     */
    public void setClass(String cName) throws  ProcessException {
	if (jarName != null) {
	    throw  new ProcessException("Can not set className when there is a jar name with (-jar option)");
	} else {
	    className = cName;
	}
    }

    /**
     * set -jar ... option
     *
     * @throws ProcessException if the class option is set
     */
    public void setJar(String jName) throws  ProcessException {
	if (className != null) {
	    throw  new ProcessException("Can not set jar Name when there is a main class define");
	} else {
	    jarName = jName;
	}
    }   

    /**
     * add mains args ... option
     *
     */
    public void addArgs(String args) throws  ProcessException {
	mainArgs+=" "+args;
    }  

    /**
     * Get the command string
     * @throws ProcessException if the class or jar option is not set
     */
    public String getCommandString() throws  ProcessException {
	String command = " ";

	// standard jvm option 
	if (server) command+="-server ";
	if (debug) command+="-debug "; 
	if (verbose) command+="-verbose ";
	if (verboseClass) command+="-verbose:class ";
	if (verboseGC) command+="-verbose:gc ";
	if (verboseJNI) command+="-verbose:jni ";
	for (Enumeration e = properties.propertyNames() ; e.hasMoreElements() ;) {
	    String propertyKey = (String)e.nextElement();
	    command+="-D" + propertyKey + "=" + properties.getProperty(propertyKey) + " ";
	}
 
	if (classpath.size() != 0) {
	    String pathSeparator = System.getProperty("path.separator");
	    command+="-classpath ";
	    for (Enumeration e = classpath.elements() ; e.hasMoreElements() ;) {
		command+=e.nextElement();
		if (e.hasMoreElements()) {
		    command+=pathSeparator;
		}
	    }
	    command+=" ";
	}

	// non standard option
 	if (nonStandard.size() != 0) {
	    for (Enumeration e = nonStandard.elements() ; e.hasMoreElements() ;) {
		command+="-X"+ e.nextElement() + " ";
	    }
	}
	
	// jar or class name
	if (jarName!=null) {
	    command+="-jar " + jarName;
	} else if(className != null) {
	    command+=className;
	} else {
	    throw new ProcessException("Class or jar name missing");
	} 

	// java main args option 
	command+=" " + mainArgs;
	
	return command;
    }
}
