/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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

package org.apache.tomcat.modules.config;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.compat.Jdk11Compat;
import org.apache.tomcat.util.compat.SimpleClassLoader;

/**
 * Set class loader based on WEB-INF/classes, lib.
 * Compatible with JDK1.1, but takes advantage of URLClassLoader if
 * java2 is detected.
 *
 * Note. LoaderInterceptor must be the first in the reload and contextInit
 * chains.
 *
 * @author costin@dnt.ro
 */
public class LoaderInterceptor11 extends BaseInterceptor {
    boolean useAppsL=true;
    boolean useParentL=false;
    boolean useCommonL=false;
    boolean useContainerL=false;
    boolean useNoParent=false;
    boolean use11Loader=false;

    boolean addJaxp=true;
    
    private int attributeInfo;
    String loader=null;
    Vector jaxpJars=new Vector();
    String jaxpJarsSDefault="jaxp.jar:crimson.jar:xalan.jar:xerces.jar";
    String jaxpJarsS=null;
    String jaxpDir=null;
    Vector additionalJars=new Vector();
    String additionalJarsS=null;
    String jarSeparator=":";
    static final String clAttribute = "org.apache.tomcat.classloader";
    
    public LoaderInterceptor11() {
    }

    /** Use ContextManager.getParentLoader() - typlically the class loader
     *  that is set by the application embedding tomcat.
     */
    public void setUseApplicationLoader( boolean b ) {
	useAppsL=b;
    }

    /** Use no parent loader. The contexts will be completely isolated.
     */
    public void setUseNoParent( boolean b ) {
	useNoParent=b;
    }

    /** Use the 1.1 loader even if running under Java2.
     *  This allows for a work-around to the currently broken URLClassLoader
     *  which can't reload classes from changed jar files.
     */
    public void setUse11Loader( boolean b ) {
	use11Loader = b;
    }

    /** Directory where jaxp jars are installed. Defaults to
	tomcat_install/lib/container, where the parser used by
	tomcat internally is located.
    */
    public void setJaxpDir(String dir ) {
	jaxpDir=dir;
    }

    /** Name of the jars that compose jaxp.
	Defaults to jaxp.jar:crimson.jar:xalan.jar:xerces.jar,
	it'll match either crimson or xerces.
     */
    public void setJaxpJars(String jars ) {
	jaxpJarsS=jars;
    }

    /** List of additional jars to add to each web application.
     */
    public void setAdditionalJars(String jars ) {
	additionalJarsS=jars;
    }

    /** Character to use to separate jars in the jaxpJars list.
        It also applies to the additionalJars context property
        list.
     */
    public void setJarSeparator(String sep) {
        if( sep != null && sep.length() > 0 ) {
            if( sep.length() > 1 )
                sep = sep.substring(0,1);

            char oldSep[]=new char[1];
            char newSep[]=new char[1];
            jarSeparator.getChars(0,1,oldSep,0 );
            sep.getChars(0,1,newSep,0);
            jaxpJarsSDefault=jaxpJarsSDefault.replace(oldSep[0],newSep[0]);

            jarSeparator=sep;
        }
    }

    /** Check if the webapp contains jaxp , and add one if not.
	This allow apps to include their own parser if they want,
	while using the normal delegation model.

	A future module will extend this and implement a more 
	advanced and generic mechanism
    */
    public void setJaxp( boolean b ) {
	addJaxp=b;
    }

    public void setLoader( String name ) {
	loader=name;
    }
    
    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	attributeInfo=cm.getNoteId(ContextManager.REQUEST_NOTE,
				   "req.attribute");
	initJaxpJars();
        initAdditionalJars();
    }

    
    /** Add all WEB-INF/classes and WEB-INF/lib to the context
     *  path
     */
    public void addContext( ContextManager cm, Context context)
	throws TomcatException
    {
        String base = context.getAbsolutePath();

	// Add "WEB-INF/classes"
	File dir = new File(base + "/WEB-INF/classes");

        // GS, Fix for the jar@lib directory problem.
        // Thanks for Kevin Jones for providing the fix.
	if( dir.exists() ) {
	    try {
		// Note: URLClassLoader in JDK1.2.2 doesn't work with file URLs
		// that contain '\' characters.  Insure only '/' is used.
		URL url=new URL( "file", null,
			dir.getAbsolutePath().replace('\\','/') + "/" );
		context.addClassPath( url );
	    } catch( MalformedURLException ex ) {
	    }
        }

        File f = new File(base + "/WEB-INF/lib");
	Vector jars = new Vector();
	getJars(jars, f);

	for(int i=0; i < jars.size(); ++i) {
	    String jarfile = (String) jars.elementAt(i);
	    File jf=new File(f, jarfile );
	    // Note: URLClassLoader in JDK1.2.2 doesn't work with file URLs
	    // that contain '\' characters.  Insure only '/' is used.
	    String absPath=jf.getAbsolutePath().replace('\\','/');
	    try {
		URL url=new URL( "file", null, absPath );
		context.addClassPath( url );
	    } catch( MalformedURLException ex ) {
	    }
	}

    }

    /** setup the ClassLoader for this context.
     *  If this is a re-loaded context, do nothing.
     */
    public void contextInit( Context ctx )
	throws TomcatException
    {
	// jsp will add it's own stuff
	if(ctx.getAttribute(clAttribute) == null) {
	    prepareClassLoader( ctx );
	}
    }

    public void copyContext(Request req, Context oldC, Context newC)
	throws TomcatException {
	if(debug > 0)
	    log( "Reload event " + newC.getPath() );
	// construct a new loader
	ClassLoader oldLoader=oldC.getClassLoader();

	// will be used by reloader or other modules to try to
	// migrate the data. 
	newC.getContainer().setNote( "oldLoader", oldLoader);
    }

    /** Construct another class loader, when the context is reloaded.
     */
    public void reload( Request req, Context context) throws TomcatException {
	if( debug > 0 )
	    log( "Reload event " + context.getPath() );

	// construct a new loader
	ClassLoader oldLoader=context.getClassLoader();

	// will be used by reloader or other modules to try to
	// migrate the data. 
	context.getContainer().setNote( "oldLoader", oldLoader);
	
	ClassLoader loader=constructLoader( context );
	if( debug>5 ) {
	    URL classP[]=context.getClassPath();
	    log("  Context classpath URLs:");
	    for (int i = 0; i < classP.length; i++)
                log("    " + classP[i].toString() );
        }

	context.setClassLoader( loader );
	context.setAttribute( clAttribute, loader);
    }

    /** Initialize the class loader.
     *  
     */
    public void prepareClassLoader(Context context) throws TomcatException {
        String list = context.getProperty("additionalJars");
        if( list != null ) {
            Vector urls=new Vector();
            getUrls( null, list, urls );
            Enumeration en=urls.elements();
            while( en.hasMoreElements() ) {
                URL url=(URL)en.nextElement();
                if( debug > 0 ) log(context + " adding: " + url);
                context.addClassPath( url );
            }
        }

        Enumeration en=additionalJars.elements();
        while( en.hasMoreElements() ) {
            URL url=(URL)en.nextElement();
            if( debug > 0 ) log(context + " adding: " + url);
            context.addClassPath( url );
        }

	ClassLoader loader=constructLoader( context );
	if( addJaxp ) {
	    boolean hasJaxp=checkJaxp( loader, context );
	    if( ! hasJaxp ) {
		en=jaxpJars.elements();
		while( en.hasMoreElements() ) {
		    URL url=(URL)en.nextElement();
		    if( debug > 0 ) log(context + " adding jaxp: " + url);
		    context.addClassPath( url );
		}
		loader=constructLoader( context );
	    }
	}

	if( debug>5 ) {
	    URL classP[]=context.getClassPath();
	    log("  Context classpath URLs:");
	    for (int i = 0; i < classP.length; i++)
                log("    " + classP[i].toString() );
        }
	
	context.setClassLoader( loader );

	// support for jasper and other applications
	context.setAttribute(clAttribute, loader);
    }
    
    /** Override this method to provide an alternate loader
     *  (or create a new interceptor )
     */
    protected ClassLoader constructLoader(Context context )
	throws TomcatException
    {
	URL classP[]=context.getClassPath();

	ClassLoader parent=null;
	if( useNoParent ) {
	    if( debug > 0 ) log( "Using no parent loader ");
	    parent=null;
	} else if( useAppsL && !context.isTrusted() ) {
	    if( debug > 0 ) log( "Using webapp loader ");
	    parent=cm.getAppsLoader();
	} else {
	    if( debug > 0 ) log( "Using container loader ");
	    parent=this.getClass().getClassLoader();
	}
	
	ClassLoader loader=null;
	if( use11Loader ) {
	    loader = new SimpleClassLoader(classP, parent);
	} else {
	    loader=jdk11Compat.newClassLoaderInstance( classP, parent);
	}
	if( debug > 0 )
	    log("Loader " + loader.getClass().getName() + " " + parent);

	// If another reloading scheme is implemented, you'll
	// have to plug it in here.
	return loader;
    }
    
    private void initJaxpJars() {
        if( jaxpJarsS == null )
            jaxpJarsS=jaxpJarsSDefault;
        getUrls( jaxpDir, jaxpJarsS, jaxpJars );
    }

    private void initAdditionalJars() {
        if( additionalJarsS != null )
            getUrls( null, additionalJarsS, additionalJars );
    }

    private void getUrls( String dir, String jarList, Vector jars ) {
	if( dir == null ) dir=cm.getInstallDir() + "/lib/container";
	File base=new File( dir );
	if( debug > 5 ) log( "Scanning \"" + jarList + "\" with base directory " + base);
	StringTokenizer st=new StringTokenizer( jarList, jarSeparator );
	while( st.hasMoreElements() ) {
	    String s=(String)st.nextElement();
            File f=new File( s );
            if( ! f.isAbsolute() )
                f=new File( base, s);
	    if( ! f.exists() ) continue;
	    try {
		URL url=new URL( "file", null,
				 f.getAbsolutePath().replace('\\','/'));
		jars.addElement( url );
		if( debug > 5 ) log( "Adding " + url );
	    } catch( MalformedURLException ex ) {
	    }
	}
    }

    private boolean checkJaxp(  ClassLoader loader, Context context ) {
	try {
	    loader.loadClass("javax.xml.parsers.SAXParserFactory");
	    return true;
	} catch( Exception ex) {
	    // Add jaxp to classP.
	    if( debug > 0 ) context.log( "Jaxp not detected, adding jaxp ");
	    return false;
	}
    }
	
    // --------------------
    private static final String separator =
	System.getProperty("path.separator", ":");

    public final Object getInfo( Context ctx, Request req,
				 int info, String k )
    {
	if( req!=null )
	    return null;
	if( info== attributeInfo ) {
	    // request for a context attribute, handled by tomcat
	    if( ! k.startsWith( "org.apache.tomcat" ) )
		return null;
	    if (k.equals("org.apache.tomcat.jsp_classpath")) {
		return getClassPath(ctx);
	    }
	    if(k.equals(clAttribute)) {
		return ctx.getClassLoader();
	    }

	}
	return null;
    }

    static Jdk11Compat jdkProxy=Jdk11Compat.getJdkCompat();

    private String getClassPath(Context ctx) {
	StringBuffer cpath=new StringBuffer();
	// local context class path
	URL classPaths[]=ctx.getClassPath();
	convertClassPath( cpath , classPaths );

        ClassLoader Loader=ctx.getClassLoader();
        // apps class loader
	convertClassPath(cpath, jdkProxy.getURLs(Loader,1));
	// common class loader
	convertClassPath(cpath, jdkProxy.getURLs(Loader,2));
	if( debug>9 )
	    log("Getting classpath " + cpath);
	return cpath.toString();
    }

    private void convertClassPath( StringBuffer cpath, URL classPaths[] ) {
	if( classPaths==null ) return;
	for(int i=0; i< classPaths.length ; i++ ) {
	    URL cp = classPaths[i];
	    if (cpath.length()>0) cpath.append( separator );
	    cpath.append(cp.getFile());
	}
    }
    
    static final Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();
    
    private void getJars(Vector v, File f) {
        FilenameFilter jarfilter = new FilenameFilter() {
		public boolean accept(File dir, String fname) {
		    if(fname.endsWith(".jar"))
			return true;

		    return false;
		}
	    };
        FilenameFilter dirfilter = new FilenameFilter() {
		public boolean accept(File dir, String fname) {
		    File f1 = new File(dir, fname);
		    if(f1.isDirectory())
			return true;

		    return false;
		}
	    };

        if(f.exists() && f.isDirectory() && f.isAbsolute()) {
            String[] jarlist = f.list(jarfilter);

            for(int i=0; (jarlist != null) && (i < jarlist.length); ++i) {
                v.addElement(jarlist[i]);
            }

            String[] dirlist = f.list(dirfilter);

            for(int i=0; (dirlist != null) && (i < dirlist.length); ++i) {
                File dir = new File(f, dirlist[i]);
                getJars(v, dir);
            }
        }
    }

}
