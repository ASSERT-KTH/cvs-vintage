package org.apache.tomcat.startup;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;

// Used to stop tomcat

/**
 * Simple starter for Tomcat.
 *
 * @author costin@dnt.ro
 */
public class SimpleStartup {

    SimpleStartup() {
    }

    public static void main(String args[] ) {
	try {
	    ContextManager cm=new ContextManager();
	    cm.start();
	} catch(Exception ex ) {
	    ex.printStackTrace();
	}

    }
}

