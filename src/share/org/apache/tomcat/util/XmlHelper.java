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

/* This file is based on ProjectHelper, with ant-specific classes replaced with generic
   objects.
   The conversion is not perfect yet - but most of the time ant works fine.
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

    public static Object readXml(File xmlFile, Properties initProps, Hashtable mapper, Hashtable valueMapper)
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

	    XmlHelperHandler dh=new XmlHelperHandler(mapper, initProps);
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
	// "project" "org.apache.tools.ant.Project"
	//            "default" "defaultTarget"
	//            "basedir" "baseDirName"
	// "property" "org.apache.tools.ant.Property" --- SPECIAL - setProperty
	//            name, value
	// "taskdef"  "org.apache.tools.ant.TaskDefinition" "TaskDefinition"
	//             "name",
	//             "class"
	// "target"	"...Target" "target"
	//             "name"
	//             "depends"
	// 	
	// *           mapping "addTask"

}


class XmlHelperHandler implements DocumentHandler {
    Locator locator;
    Object elemStack[];
    String tagStack[];
    int sp;
    Properties props;
    int debug=0;
    
    Hashtable tagMapper;
    
    public XmlHelperHandler(Hashtable mapper, Properties props) {
	tagMapper=mapper;
	this.props=props;
        elemStack = new Object[100]; // depth of the xml doc
	tagStack = new String[100]; 
    }
    
    public void setDocumentLocator (Locator locator)
    {
	//	System.out.println("Locator " + locator);
	this.locator = locator;
    }

    public void startDocument () throws SAXException
    {
        sp = 0;
    }

    public void endDocument () throws SAXException
    {
        if (sp != 0) {
	    System.out.println("The XML document is probably broken");
	}
    }

    private String processValue( String v) throws SAXException{
	Object parent=(sp>0) ? elemStack[sp-1] : null;
	//	System.out.println("PV " + v);
	try {
	    return 	v; //// ScriptHelper.replaceProperties( v, parent, elemStack[0]);
	} catch(Exception ex ) {
	    ex.printStackTrace();
	}
	return "";
    }
    
    public void startElement (String tag, AttributeList attributes)
	throws SAXException
    {
	try {
	    if( debug>0) log("Start " + tag + " " + attributes + " " + sp);

	    // Special case: <foo><property name="a" value="b" /></foo>
	    // Will call foo.setA( "b" ) ( or foo.setProperty("a", "b") if setA not found )
	    if( "property".equals( tag ) ) {
		String n=attributes.getValue("name");
		String v=attributes.getValue("value");
		if(n==null || v==null)
		    System.out.println("Error: property with null name/value");
		Object elem=elemStack[sp-1];
		if(debug>1) log("Setting " + elem.getClass()+ " "  + n + "=" + v );
		
		InvocationHelper.setProperty( elem, n, processValue(v) );
		return;
	    }

	    // Normal tag <foo> - mapped to org.bar.Foo, will construct a new Foo.
	    Object elem=tagMapper.get( tag );

	    // For the root element, add the properties passed to XmlHelper
	    // ( from the command line )
	    if(sp==0) {
		// set all "default" properties
		Enumeration e = props.keys();
		while (e.hasMoreElements()) {
		    String arg = (String)e.nextElement();
		    String value = (String)props.get(arg);
		    InvocationHelper.setProperty(elem, arg, value);
		}
	    }

	    /* Set parent in child:
	       For <foo><bar/></foo>,
	       will try: bar.setFoo( Foo )
	       then: foo.setAttribute( "foo", Foo);
	    */
	    if( sp > 0 ) {
		// we're not the root
		// tell our parent about us
		InvocationHelper.setAttribute( elem, tagStack[sp-1], elemStack[sp-1] );
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
		    InvocationHelper.setProperty( elem, name, processValue(value) );
		}
	    }

	    /*
	      Set parent in sun, will try:
	      foo.setBar( Bar )
	      foo.addBar( Bar )
	      foo.setAttribute( "bar", Bar)
	    */
	    if( sp > 0 ) {
		InvocationHelper.addAttribute( elemStack[sp-1], tag, elem );
	    }
	
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
	if( "property".equals( tag ) ) {
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
	System.out.println("XMLH: " + msg );
    }

}

