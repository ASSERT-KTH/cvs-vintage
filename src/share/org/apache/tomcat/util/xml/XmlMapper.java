package org.apache.tomcat.util.xml;

import org.apache.tomcat.util.*;
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

/** SAX Handler - it will read the XML and construct java objects
 * @author costin@dnt.ro
 */
public class XmlMapper implements DocumentHandler, SaxContext, EntityResolver, DTDHandler  {
    Locator locator;

    // Stack of elements
    Stack oStack=new Stack();
    Object root;
    Object attributeStack[];
    String tagStack[];
    int oSp;
    int sp;

    String body;
    
    int debug=0;
    
    public XmlMapper() {
	attributeStack = new Object[100]; // depth of the xml doc
	tagStack = new String[100]; 
    }
    
    public void setDocumentLocator (Locator locator)
    {
	if( debug>0 ) log("Set locator : " + locator);
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
	    //	    if( debug>0) log(sp + "<" + tag + " " + attributes + ">");
	    attributeStack[sp]=attributes;
	    tagStack[sp]=tag;
	    sp++;
	    matchStart( this);
	    body="";
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void endElement (String tag) throws SAXException
    {
	// Find a match for the current tag in the context
	matchEnd( this);
	    
	if( sp > 1 ) {
	    tagStack[sp] = null;
	    attributeStack[sp]=null;
	}
	sp--;
    }

    public void characters (char buf [], int offset, int len)
	throws SAXException
    {
	body=body+ new String(buf, offset, len );
    }

    public void ignorableWhitespace (char buf [], int offset, int len)
	throws SAXException
    {
    }
    
    public void processingInstruction (String name, String instruction) 
	throws SAXException
    {
    }

    // -------------------- Context --------------------
    // provide access to the current stack and XML elements.
    // -------------------- Context --------------------

    public AttributeList getAttributeList( int pos ) {
	return (AttributeList)attributeStack[pos];
    }

    public int getTagCount() {
	return sp;
    }

    public String getTag( int pos ) {
	return tagStack[pos];
    }

    public String getBody() {
	return body;
    }

    public Stack getObjectStack() {
	return oStack;
    }

    public Object getRoot() {
	return root;
    }

    public void setRoot(Object o) {
	root=o;
    }
    
    // -------------------- Utils --------------------
    // Debug ( to be replaced with the real thing )
    public void setDebug( int level ) {
	debug=level;
    }

    public int getDebug() {
	return debug;
    }
    
    public void log( String msg ) {
	System.out.println("SH: " + msg );
    }

    /** read an XML file, construct and return the object hierarchy
     */
    public Object readXml(File xmlFile, Object root)
	throws Exception
    {
	if(root!=null) {
	    Stack st=this.getObjectStack();
	    this.root=root;
	    st.push( root );
	}
	InputSource input;
	Parser parser=null;
	try {
	    if(System.getProperty("org.xml.sax.parser") != null )
		parser=ParserFactory.makeParser();
	    else
		parser=ParserFactory.makeParser("com.sun.xml.parser.Parser");
	    
	    input = new InputSource( new FileReader(xmlFile));

	    parser.setDocumentHandler( this);
	    parser.setEntityResolver( this);
	    parser.setDTDHandler( this);
	    parser.parse( input );
	    return root;
	    // assume the stack is in the right position.
	    // or throw an exception is more than one element is on the stack
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

    class Rule {
	XmlMatch match;
	XmlAction action;
	Rule( XmlMatch match, XmlAction action ) {
	    this.match=match;
	    this.action=action;
	}
    }
    Rule rules[]=new Rule[100];
    Rule matching[]=new Rule[100];
    int ruleCount=0;
    
    public void addRule( String path, XmlAction action ) {
	rules[ruleCount]=new Rule( new PathMatch( path ) , action);
	ruleCount++;
    }

    private int match( SaxContext ctx, Rule matching[] ) {
	int matchCount=0;
	for( int i=0; i< ruleCount; i++ ) {
	    if( rules[i].match.match( ctx ) &&
		rules[i].action != null ) {
		matching[matchCount]=rules[i];
		matchCount++;
	    }
	}
	return matchCount;
    }

    void matchStart(SaxContext ctx ) {
	try {
	    int matchCount=match( ctx, matching );
	    for ( int i=0; i< matchCount; i++ ) {
		matching[i].action.start( ctx );
	    }
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
    }

    void matchEnd(SaxContext ctx ) {
	try {
	    int matchCount=match( ctx, matching );
	    for ( int i=0; i< matchCount; i++ ) 
		matching[i].action.end( ctx );
	    for ( int i=0; i< matchCount; i++ ) 
		matching[i].action.cleanup( ctx );
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
    }

    /** Trick for off-line usage
     */
    public InputSource resolveEntity(String publicId, String systemId) {
	if( debug>0 ) log("Entity: " + publicId + " " + systemId);
	// We need to return a valid IS - or the default will be used,
	// and that means reading from net.
	InputSource is=new InputSource();
	is.setByteStream( new StringBufferInputStream(""));
	return is;
    }

    public void notationDecl (String name,
			      String publicId,
			      String systemId)
	throws SAXException
    {
	System.out.println("Notation: " + name + " " + publicId + " " + systemId);
    }

    public  void unparsedEntityDecl (String name,
				     String publicId,
				     String systemId,
				     String notationName)
	throws SAXException
    {
	System.out.println("Unparsed: " + name + " " + publicId + " " + systemId + " " + notationName);
    }


    // -------------------- Factories for "common" actions --------------------

    /** Create an object using for a matching tag with the given class name
     */
    public XmlAction objectCreate( String classN ) {
	return new ObjectCreate( classN);
    }

    /** Create an object using an attribute value as the class name
	If no attribute use classN as a default.
     */
    public XmlAction objectCreate( String classN, String attrib ) {
	return new ObjectCreate( classN, attrib);
    }

    /** Set object properties using XML attributes
     */
    public XmlAction setProperties(  ) {
	return new SetProperties();
    }

    /** For the last 2 objects in stack, create a parent-child
     *	and child.childM with parente as parameter
     */
    public XmlAction setParent( String childM ) {
	return new SetParent( childM );
    }

    /** For the last 2 objects in stack, create a parent-child
     *  relation by invokeing parent.parentM with the child as parameter
     *  ArgType is the parameter expected by addParent ( null use the current object
     *  type)
     */
    public XmlAction addChild( String parentM, String argType ) {
	return new AddChild( parentM, argType );
    }

    /** If a tag matches, invoke a method on the current object.
	Parameters can be extracted from sub-elements of the current
	tag.
    */
    public XmlAction methodSetter(String method, int paramC) {
	return new MethodSetter(method, paramC);
    }

    /** If a tag matches, invoke a method on the current object.
	Parameters can be extracted from sub-elements of the current
	tag.
    */
    public XmlAction methodSetter(String method, int paramC, String paramTypes[]) {
	return new MethodSetter(method, paramC, paramTypes);
    }

    /** Extract the method param from the body of the tag
     */
    public XmlAction methodParam(int ord) {
	return new MethodParam(ord, null); // use body as value
    }
    
    /** Extract the method param from a tag attribute
     */
    public XmlAction methodParam(int ord, String attrib) {
	return new MethodParam(ord, attrib);
    }
    
}

//-------------------- "Core" actions --------------------

/**
 * Create a new object.
 */
class ObjectCreate extends XmlAction {
    String className;
    String attribute;
    
    /**
     * Create a new object of the specified class only.
     *
     * @param className Class name of the object to be created
     */
    public ObjectCreate(String className) {
	this.className = className;
    }

    /**
     * Create an object of a class whose name is stored in the specified
     * attribute (if present), or defaults to the specified class name.
     *
     * @param className Default class name to use
     * @param attribute Name of the attribute containing the override class
     */
    public ObjectCreate(String className, String attribute) {
	this.className=className;
	this.attribute=attribute;
    }
    
    /**
     * Create an object of the specified (or override) class.
     *
     * @param ctx Context in which this action takes place
     */
    public void start( SaxContext ctx) throws Exception {

	// Initialize our local environment
	Stack st = ctx.getObjectStack();
	int top = ctx.getTagCount()-1;
	String tag = ctx.getTag(top);
	String className = this.className;

	// Override the default class name if specified
	if (attribute != null) {
	    AttributeList attributes = ctx.getAttributeList(top);
	    String value = attributes.getValue(attribute);
	    if (value != null)
		className = value;
	}

	// Create a new instance and push it on the stack
	Class c = Class.forName( className );
	Object o = c.newInstance();
	st.push(o);
	if (ctx.getDebug() > 0)
	    ctx.log("new "  + attribute + " " + className + " "  + tag  +
		    " " + o);
    }
    
    /**
     * Clean up by popping the object we created off of the stack.
     *
     * @param ctx Context in which this action takes place
     */
    public void cleanup( SaxContext ctx) {
	Stack st = ctx.getObjectStack();
	String tag = ctx.getTag(ctx.getTagCount()-1);
	Object o = st.pop();
	if (ctx.getDebug() > 0)
	    ctx.log("pop " + tag + " " + o.getClass().getName() + ": " + o);
    }

}


/** Set object properties using XML attribute list
 */
class SetProperties extends XmlAction {
    //    static Class paramT[]=new Class[] { "String".getClass() };
    
    public SetProperties() {
    }

    public void start( SaxContext ctx) {
	Stack st=ctx.getObjectStack();
	Object elem=st.peek();
	int top=ctx.getTagCount()-1;
	AttributeList attributes = ctx.getAttributeList( top );

	for (int i = 0; i < attributes.getLength (); i++) {
	    String type = attributes.getType (i);
	    String name=attributes.getName(i);
	    String value=attributes.getValue(i);

	    InvocationHelper.setProperty( elem, name, value );
	    if( ctx.getDebug() > 0 ) ctx.log("setProperty(" + name + " "  + value  +")" );
	}

    }
}


/** Set parent
 */
class SetParent extends XmlAction {
    String childM;
    public SetParent(String c) {
	childM=c;
    }
    
    public void end( SaxContext ctx) throws Exception {
	Stack st=ctx.getObjectStack();

	Object obj=st.pop();
	Object parent=st.peek();
	st.push( obj ); // put it back

	String parentC=parent.getClass().getName();
	if( ctx.getDebug() > 0 ) ctx.log("Calling " + obj.getClass().getName() + "." + childM +
					 " " + parentC);

	Class params[]=new Class[1];
	params[0]=parent.getClass();
	Method m=obj.getClass().getMethod( childM, params );
	m.invoke(obj, new Object[] { parent } );
    }
}

/** Set parent
 */
class AddChild extends XmlAction {
    String parentM;
    String paramT;
    
    public AddChild(String p, String c) {
	parentM=p;
	paramT=c;
    }
    
    public void end( SaxContext ctx) throws Exception {
	Stack st=ctx.getObjectStack();

	Object obj=st.pop();
	Object parent=st.peek();
	st.push( obj ); // put it back

	String parentC=parent.getClass().getName();
	if( ctx.getDebug() > 0 ) ctx.log("Calling " + parentC + "." + parentM  +" " + obj  );

	Class params[]=new Class[1];
	if( paramT==null) {
	    params[0]=obj.getClass();
	} else {
	    params[0]=Class.forName( paramT );
	}
	Method m=parent.getClass().getMethod( parentM, params );
	m.invoke(parent, new Object[] { obj } );
    }
}

/**
 */
class  MethodSetter extends 	    XmlAction {
    String mName;
    int paramC;
    String paramTypes[];
    
    public MethodSetter( String mName, int paramC) {
	this.mName=mName;
	this.paramC=paramC;
    }
    
    public MethodSetter( String mName, int paramC, String paramTypes[]) {
	this.mName=mName;
	this.paramC=paramC;
	this.paramTypes=paramTypes;
    }
    
    public void start( SaxContext ctx) {
	Stack st=ctx.getObjectStack();
	if(paramC==0) return;
	String params[]=new String[paramC];
	st.push( params );
    }

    static final Class STRING_CLASS="String".getClass(); // XXX is String.CLASS valid in 1.1 ?
    
    public void end( SaxContext ctx) throws Exception {
	Stack st=ctx.getObjectStack();
	String params[]=null;
	if( paramC >0 ) params=(String []) st.pop();
	Object parent=st.peek();

	if( paramC == 0 ) {
	    params=new String[1];
	    params[0]= ctx.getBody().trim();
	    if( ctx.getDebug() > 0 ) ctx.log("" + parent.getClass().getName() + "." + mName + "( " + params[0] + ")");
	}

	Class paramT[]=new Class[params.length];
	Object realParam[]=new Object[params.length];
	for (int i=0; i< params.length; i++ ) {
	    if( paramTypes==null) {
		realParam[i]=params[i];
		paramT[i]=STRING_CLASS;
	    } else {
		// XXX Add more types
		if( "int".equals( paramTypes[i] ) ) {
		    realParam[i]=new Integer( params[i] );
		    paramT[i]=realParam[i].getClass();
		} else {
		    realParam[i]=params[i];
		    paramT[i]=STRING_CLASS;
		}
	    }
	}

	//	System.out.println(" XXX  " + parent.getClass().getName() + " " + mName + " " + paramT[0]);
	Method m=parent.getClass().getMethod( mName, paramT );
	m.invoke( parent, realParam );
	    
	if(ctx.getDebug() > 0 ) {
	    // debug
	    StringBuffer sb=new StringBuffer();
	    sb.append("" + parent.getClass().getName() + "." + mName + "( " );
	    for(int i=0; i<paramC; i++ ) {
		if(i>0) sb.append( ", ");
		sb.append(params[i]);
	    }
	    sb.append(")");
	    if( ctx.getDebug() > 0 ) ctx.log(sb.toString());
	}
    }
}

/**
 */
class  MethodParam extends XmlAction {
    int paramId;
    String attrib;
    
    public MethodParam( int paramId, String attrib) {
	this.paramId=paramId;
	this.attrib=attrib;
    }

    // If param is an attrib, set it
    public void start( SaxContext ctx) {
	if( attrib==null) return;
	
	Stack st=ctx.getObjectStack();
	String h[]=(String[])st.peek();

	int top=ctx.getTagCount()-1;
	AttributeList attributes = ctx.getAttributeList( top );
	h[paramId]= attributes.getValue(attrib);
    }

    // If param is the body, set it
    public void end( SaxContext ctx) {
	if( attrib!=null) return;
	Stack st=ctx.getObjectStack();
	String h[]=(String[])st.peek();
	h[paramId]= ctx.getBody().trim();
    }
}
