/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.server;

import java.util.Iterator;
import java.util.Vector;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ObjectInstance;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MBeanServer;
import javax.naming.InitialContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
* XML Adaptor Implementation interpreting the XML wrapped JMX commands.
*
* @author Andreas Schaefer (andreas.schaefer@madplanet.com)
* @created June 22, 2001
* @version $Revision: 1.1 $
*/
public class XMLAdaptorImpl
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------
  MBeanServer mServer;


  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
  *  Constructor for the JMXAdaptorImpl object
  *
  *@param pServer MBeanServer this adaptor executes its calls on
  */
  public XMLAdaptorImpl( MBeanServer pServer ) {
    super();
    mServer = pServer;
  }

  // Public --------------------------------------------------------

  public Object[] invokeXML( Document pJmxOperations ) {
    Vector lReturns = new Vector();
    NodeList lOperations = pJmxOperations.getChildNodes();
    for( int i = 0; i < lOperations.getLength(); i++ ) {
      Element lChildElement = (Element) lOperations.item( i );
      lReturns.add( invokeXML( lChildElement ) );
    }
    return (Object[]) lReturns.toArray( new Object[ 0 ] );
  }

  public Object invokeXML( Element pJmxOperation ) {
    if( pJmxOperation == null ) {
      return null;
    }
    // Get the requested operation
    String lTag = pJmxOperation.getTagName();
    if( "invoke".equals( lTag ) ) {
      // Get the operation, Object Name and attributes and invoke it
      String lOperation = pJmxOperation.getAttribute( "operation" );
      if( !"".equals( lOperation ) ) {
        NodeList lList = pJmxOperation.getElementsByTagName( "object-name" );
        if( lList.getLength() > 0 ) {
          try {
            Node lNodeName = lList.item( 0 );
            ObjectName lName = null;
            switch( lNodeName.getNodeType() ) {
              case Node.ELEMENT_NODE:
                Element lElementName = (Element) lNodeName;
                lName = new ObjectName( lElementName.getAttribute( "name" ) );
                break;
            }
            // Get attribute values and types
            NodeList lAttributeList = pJmxOperation.getElementsByTagName( "attribute" );
            Object[] lObjects = new Object[ lAttributeList.getLength() ];
            String[] lTypes = new String[ lAttributeList.getLength() ];
            for( int i = 0; i < lAttributeList.getLength(); i++ ) {
              Node lAttributeNode = lAttributeList.item( i );
              Element lAttributeElement = (Element) lAttributeNode;
              lTypes[ i ] = lAttributeElement.getAttribute( "type" );
              if( "int".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Integer( lAttributeElement.getAttribute( "value" ) );
              }
              else if( "short".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Short( lAttributeElement.getAttribute( "value" ) );
              }
              else if( "byte".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Byte( lAttributeElement.getAttribute( "value" ) );
              }
              else if( "char".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Character( lAttributeElement.getAttribute( "value" ).charAt( 0 ) );
              }
              else if( "long".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Long( lAttributeElement.getAttribute( "value" ) );
              }
              else if( "float".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Float( lAttributeElement.getAttribute( "value" ) );
              }
              else if( "double".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Double( lAttributeElement.getAttribute( "value" ) );
              }
              else if( "boolean".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new Boolean( lAttributeElement.getAttribute( "value" ) );
              }
              else if( "java.lang.String".equals( lTypes[ i ] ) ) {
                lObjects[ i ] = new String( lAttributeElement.getAttribute( "value" ) );
              }
            }
            // Invoke the method and return the value
            return mServer.invoke(
              lName,
              lOperation,
              lObjects,
              lTypes
            );
          }
          catch( Exception e ) {
            e.printStackTrace();
          }
        }
      }
    }
    return null;
  }

  // Protected -----------------------------------------------------
}

