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

// XXX this interface is not final, but a prototype.

/** SAX Context - used to match and perform actions 
 *  provide access to the current stack and XML elements.
 * 
 *  We maintain a stack with all elements and their attributes.
 *  We also support a stack of objects that can be used as in a
 *  stack-based programming language.
 *
 * @author costin@dnt.ro
 */
public interface SaxContext  {

    // -------------------- Access to the element stack

    /** Body of the last tag.
     */
    public String getBody();

    /** Attributes of the current tag
     */
    public AttributeList getCurrentAttributes();

    /** Current element
     */
    public String getCurrentElement();


    /** Depth of the tag stack.
     *  XXX getElementDepth() ?
     */
    public int getTagCount();

    /** Random access to attributes of a particular element.
     */
    public AttributeList getAttributeList( int pos );

    /** Random Access a particular parent element
     *  XXX getElement() is a better name
     */
    public String getTag( int pos );



    // -------------------- Object stack

    public void pushObject(Object o);
    public Object popObject();

    public Object currentObject();
    public Object previousObject();
    
    /**
       The root object is either set by caller before starting the parse
       or can be created using the first tag. It is used to set object in
       the result graph by navigation ( using root and a path). Please
       use the stack, it's much faster and better.
    */
    public Object getRoot();
    
    /** We maintain a stack to keep java objects that are generated
	as result of parsing. You can either use the stack ( which is
	very powerfull construct !), or use the root object
	and navigation in the result tree.
	@deprecated
    */
    public Stack getObjectStack();

    // -------------------- Utilities
    
    public int getDebug();

    public void log( String s );

    public XmlMapper getMapper();

    // -------------------- Variables -------------------- 
    public void setVariable( String s, Object v );

    public Object getVariable( String s );
}
