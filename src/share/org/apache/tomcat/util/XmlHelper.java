package org.apache.tomcat.util;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.StringTokenizer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/*
   1. <foo a="1" b="2" /> will:
   1.1 construct a new object ( using mappings like foo->org.apache.Foo, or
       a default package )
   1.2 Call setters for a and b. If no setA() is found - call setProperty("a", "1");
       It will also take care of converting from string to int ( for setA( int ) )

   2. <bar><foo /></bar>
   2.1. Will construct Bar, Foo
   2.2. Will call Foo.setBar( Bar ). If no such property is found,
       it will call Foo.setAttribute( "bar", Bar );
   2.3  Will call Bar.addFoo( Foo ). If not found will try
        addFooInterface( Foo ) for all interfaces implemented by Foo.
	( for example addServerAdapter() will be called for HttpAdapter,
	  and addRequestInterceptor() will be called for SimpleMapper )
	  If none is found it will call Bar.addAttribute( "foo", Foo );

   This allows us to construct a hierarchy of objects, and it seems
   to be enough to run Ant and Tomcat.

   More features ( hacks ? ) will be added if needed.

   The Xml parsing is done using SAX - not only it's faster, but it
   is much easier ( and no memory overhead, no extra duplicated tree).

 */

/**
 * Read an XML file and construct Java objects for each element.
 * Use only SAX - instead of SAX + DOM.
 *
 * @param mapper convert from XML tag names to Java objects
 *
 * @author costin@dnt.ro
 * @author duncan@x180.com
 */
public class XmlHelper {
    String defaultPackage;
    Hashtable maps=new Hashtable();
    Hashtable propertyTags=new Hashtable();
    Hashtable attribMap=new Hashtable();
    Hashtable execTags=new Hashtable();
    int debug=0;
    
    public XmlHelper() {
    }

    /** All <tags> that are not explicitely mapped will construct
     * objects in defaultPackage.Tags
     *  That means <contextManager> will be mapped to
     *              org.apache.core.tomcat.ContextManager
     */
    public void setDefaultPackage(String defP) {
	defaultPackage=defP + ".";
    }

    /** Add a mapping for <tname> to a class jname
     */
    public void addMap( String tagName, String javaClassName ) {
	maps.put( tagName, javaClassName );
    }

    /** Add a mapping for <tname> to a class jname, and mark it as
	"execute at parsing time" ( to define new mappings, etc).
     */
    public void addMap( String tagName, String javaClassName, boolean execute ) {
	maps.put( tagName, javaClassName );
	execTags.put( tagName, javaClassName);
    }

    /** Add a mapping for <tagName type="typeProperty"> to a class jname
     *  Used to define something like <adapter type="http" />
     *  ( to reduce name-space polution)
     */
    public void addMap( String tagName, String typeProperty, String javaClassName ) {
	maps.put( tagName + "." + typeProperty , javaClassName );
    }

    /** Return the name of the setter that will be used to set tag into parent.
	Used for setTag() and addTag().
     */
    public String mapAttribute( String parentType, String tag) {
	String newT=(String)attribMap.get( parentType + "/" + tag );
	if ( newT != null ) return newT;
	return tag;
    }

    public void addAttributeMap( String parentType, String tag, String attrib) {
	attribMap.put( parentType + "/" + tag, attrib);
    }
    
    /**
     * special treatement - use name attribute as a property name in parent
     */
    public void addPropertyTag( String name ) {
	propertyTags.put( name, name );
    }

    public boolean isPropertyTag( String name ) {
	return propertyTags.get(name) != null;
    }

    /** Construct a new object for the tag <tname type="cName">
     */
    public Object getObjectForClassName( String cName ) {
	return InvocationHelper.getInstance( cName );
    }

    /** Construct a new object for the tag <tname>
     */
    public Object getObjectForTag( String tname ) {
	String cName=(String)maps.get(tname);
	if(cName!=null) return InvocationHelper.getInstance( cName );

	// default
	if(defaultPackage!=null)
	    return InvocationHelper.getInstance(defaultPackage +
						InvocationHelper.capitalize( tname ));
	System.out.println("Error: can't find object for tag " + tname);
	return  new Object(); 
    }

    /** Called when all Xml-related processing are done with this tag
     */
    void endTag( String tagName, Object o) {
	if( null == execTags.get(tagName) )
	    return;
	// TODO
    }
    
    /** Read an XML file, construct and return the object hierarchy
     */
    public Object readXml(File xmlFile)
	throws Exception
    {
	InputSource input;

	Parser parser=null;
	try {
	    if(System.getProperty("org.xml.sax.parser") != null )
		parser=ParserFactory.makeParser();
	    else
		parser=ParserFactory.makeParser("com.sun.xml.parser.Parser");

	    input = new InputSource( new FileReader(xmlFile));

	    XmlHelperHandler dh=new XmlHelperHandler(this);
	    dh.setDebug( debug );
	    parser.setDocumentHandler( dh );
	    parser.parse( input );
	    return dh.getRootElement();
	} catch( IllegalAccessException ex1 ) {
	    ex1.printStackTrace();
	    throw new Exception( "Error creating sax parser" );
	} catch(ClassNotFoundException  ex2 ) {
	    ex2.printStackTrace();
	    throw new Exception( "Error creating sax parser" );
	} catch( InstantiationException ex3)  {
	    ex3.printStackTrace();
	    throw new Exception( "Error creating sax parser" );
   	} catch (IOException ioe) {
	    String msg = "Can't open config file: " + xmlFile +
		" due to: " + ioe;
	    throw new Exception(msg);
	} catch (SAXException se) {
	    se.printStackTrace();
	    String msg = "Can't open config file: " + xmlFile +
		" due to: " + se;
	    throw new Exception(msg);
	}
    }

    public void setDebug( int level) {
	this.debug=level;
    }

    public void log( String s ) {
	System.out.println("XmlHelper: " + s );
    }

}

/** SAX Handler - it will read the XML and construct java objects
 */
class XmlHelperHandler implements DocumentHandler {
    XmlHelper helper;
    Locator locator;

    // Stack of elements
    Object elemStack[];
    String tagStack[];
    int sp;
    
    int debug=0;
    
    public XmlHelperHandler(XmlHelper helper) {
	this.helper=helper;
        elemStack = new Object[100]; // depth of the xml doc
	tagStack = new String[100]; 
    }
    
    public void setDocumentLocator (Locator locator)
    {
	this.locator = locator;
    }

    public void startDocument () throws SAXException
    {
        sp = 0;
    }

    public void endDocument () throws SAXException
    {
        if (sp != 0) {
	    System.out.println("The XML document is probably broken. " + sp);
	}
    }

    public void startElement (String tag, AttributeList attributes)
	throws SAXException
    {
	try {
	    if( debug>0) log("Start " + tag + " " + attributes + " " + sp);

	    // Special case: <foo><property name="a" value="b" /></foo>
	    // Will call foo.setA( "b" ) ( or foo.setProperty("a", "b") if setA not found )
	    if( helper.isPropertyTag( tag ) ) {
		String n=attributes.getValue("name");
		String v=attributes.getValue("value");
		if(n==null || v==null)
		    System.out.println("Error: property with null name/value");
		Object elem=elemStack[sp-1];
		if(debug>1) log("Setting " + elem.getClass()+ " "  + n + "=" + v );
		
		InvocationHelper.setProperty( elem, n, v );
		return;
	    }

	    Object elem=null;
	    // <tag type="subtype" >
	    if( attributes.getValue("type") != null )
		elem=helper.getObjectForTag( tag + "." + attributes.getValue("type"));

	    // Normal tag <foo> - mapped to org.bar.Foo, will construct a new Foo.
	    if( elem==null)
		elem=helper.getObjectForTag( tag );

	    // Another attempt - if className exists, use it as java type
	    if( attributes.getValue("className") != null )
		elem=helper.getObjectForClassName( attributes.getValue( "className" ));
	    
	    if( elem ==null ) {
		System.out.println("Can't map " + tag );
		return;
	    }
	    
	    // 	    // For the root element, add the properties passed to XmlHelper
	    // 	    // ( from the command line )
	    // 	    if(sp==0) {
	    // 		// set all "default" properties
	    // 		Enumeration e = props.keys();
	    // 		while (e.hasMoreElements()) {
	    // 		    String arg = (String)e.nextElement();
	    // 		    String value = (String)props.get(arg);
	    // 		    InvocationHelper.setProperty(elem, arg, value);
	    // 		}
	    // 	    }

	    /* Set parent in child:
	       For <foo><bar/></foo>,
	       will try: bar.setFoo( Foo )
	       then: foo.setAttribute( "foo", Foo);
	    */
	    if( sp > 0 ) {
		String parentType=elem.getClass().getName();
		String attribName=helper.mapAttribute( parentType,  tagStack[sp-1]);
		// we're not the root
		// tell our parent about us
		InvocationHelper.setAttribute( elem, attribName, elemStack[sp-1] );
	    }

	    /* <foo a="b" />
	       Normal attribute setting, with setA( "b" ), or setProperty("a", "b") if not
	       setA found
	     */
	    if(attributes!=null) {
		for (int i = 0; i < attributes.getLength (); i++) {
		    String type = attributes.getType (i);
		    String name=attributes.getName(i);
		    String value=attributes.getValue(i);
		    //System.out.println("Attribute " + i + " type= " + type + " name=" + name + " value=" + value);
		    InvocationHelper.setProperty( elem, name, value );
		}
	    }

	    /*
	      Add child to parent, will try:
	      foo.setBar( Bar )
	      foo.addBar( Bar )
	      foo.setAttribute( "bar", Bar)
	    */
	    if( sp > 0 ) {
		String parentType=elemStack[sp-1].getClass().getName();
		String attribName=helper.mapAttribute( parentType,  tag);
		InvocationHelper.addAttribute( elemStack[sp-1], attribName, elem );
	    }

	    helper.endTag( tag, elem );
	    
	    elemStack[sp]=elem;
	    tagStack[sp]=tag;
	    sp++;
	    //	System.out.println("Start " + tag + " " + attributes + " " + sp);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void endElement (String tag) throws SAXException
    {
	if( helper.isPropertyTag( tag ) ) {
	    return;
	}
	if( sp > 1 ) {
	    tagStack[sp] = null;
	    elemStack[sp] = null;
	}
	sp--;
    }

    public void characters (char buf [], int offset, int len)
	throws SAXException
    {
	String value=new String(buf, offset, len );
	if( (sp > 0) && elemStack[sp]!=null ) {
	    InvocationHelper.addAttribute( elemStack[sp], "body", value );
	}
	
    }

    public void ignorableWhitespace (char buf [], int offset, int len)
	throws SAXException
    {
    }
    
    public void processingInstruction (String name, String instruction) 
	throws SAXException
    {
    }

    public Object getRootElement() {
	return elemStack[0];
    }

    // Debug ( to be replaced with the real thing )
    public void setDebug( int level ) {
	debug=level;
    }

    void log( String msg ) {
	System.out.println("SaxHelper: " + msg );
    }

}

