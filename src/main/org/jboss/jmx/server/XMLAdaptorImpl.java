/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.server;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
// import javax.management.AttributeNotFoundException;
// import javax.management.InstanceNotFoundException;
// import javax.management.InvalidAttributeValueException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
// import javax.management.ReflectionException;
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
* @version $Revision: 1.2 $
*/
public class XMLAdaptorImpl
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------
  MBeanServer mServer;


  // Static --------------------------------------------------------

  /** Primitive type name -> class map. */
  private static Hashtable mPrimitives = new Hashtable();
  
  /** Setup the primitives map. */
  static {
    mPrimitives.put( "boolean", Boolean.TYPE );
    mPrimitives.put( "byte", Byte.TYPE );
    mPrimitives.put( "short", Short.TYPE );
    mPrimitives.put( "int", Integer.TYPE );
    mPrimitives.put( "long", Long.TYPE );
    mPrimitives.put( "float", Float.TYPE );
    mPrimitives.put( "double", Double.TYPE );
    mPrimitives.put( "char", Character.TYPE );
  }

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
    NodeList lRoot = pJmxOperations.getChildNodes();
    if( lRoot.getLength() > 0 ) {
      Element lRootElement = (Element) lRoot.item( 0 );
      System.out.println( "XMLAdaptorImpl.invokeXML(), root: " + lRootElement );
      NodeList lOperations = lRootElement.getChildNodes();
      for( int i = 0; i < lOperations.getLength(); i++ ) {
        Element lChildElement = (Element) lOperations.item( i );
        lReturns.add( invokeXML( lChildElement ) );
      }
    }
    return (Object[]) lReturns.toArray( new Object[ 0 ] );
  }

  public Object invokeXML( Element pJmxOperation ) {
    if( pJmxOperation == null ) {
      return null;
    }
    // Get the requested operation
    String lTag = pJmxOperation.getTagName();
    System.out.println( "XMLAdaptorImpl.invokeXML(), Tag: " + lTag );
    if( "invoke".equals( lTag ) ) {
      // Get the operation, Object Name and attributes and invoke it
      String lOperation = pJmxOperation.getAttribute( "operation" );
      return invoke(
        lOperation,
        pJmxOperation.getElementsByTagName( "object-name" ),
        pJmxOperation.getElementsByTagName( "attribute" )
      );
    }
    else if( !"create-mbean".equals( lTag ) ) {
        NodeList lList = pJmxOperation.getElementsByTagName( "object-name" );
      // Get the operation, Object Name and attributes and invoke it
      String lCodebase = pJmxOperation.getAttribute( "code" );
      String lName = pJmxOperation.getAttribute( "name" );
      return createMBean(
        lCodebase,
        lName,
        pJmxOperation.getElementsByTagName( "object-name" ),
        pJmxOperation.getElementsByTagName( "constructor" ),
        pJmxOperation.getElementsByTagName( "attribute" )
      );
    }
    return null;
  }

  public ObjectName createMBean(
    String pCodebase,
    String pName,
    NodeList pObjectName,
    NodeList pConstructor,
    NodeList pAttributes
  ) {
    ObjectName lReturn = null;
    // Check Codebase
    if( pCodebase != null && !pCodebase.equals( "" ) ) {
      try {
        // Create ObjectName
        ObjectName lName = null;
        if( pName != null && !pName.equals( "" ) ) {
          lName = createObjectName( pName );
        }
        else if( pObjectName != null && pObjectName.getLength() > 0 ) {
          lName = createObjectName( (Element) pObjectName.item( 0 ) );
        }
        if( lName != null ) {
          ObjectInstance lNew = null;
          if( pConstructor.getLength() == 0 ) {
            lNew = mServer.createMBean( pCodebase, lName );
          }
          else {
            // Get the Constructor Values
            Object[][] lAttributes = getAttributes(
              ( (Element) pConstructor.item( 0 ) ).getElementsByTagName( "argument" )
            );
            lNew = mServer.createMBean(
              pCodebase,
              lName,
              lAttributes[ 0 ],
              (String[]) lAttributes[ 1 ]
            );
          }
          // Now loop over the attributes and set them
          Object[][] lAttributes = getAttributes(
            lNew.getObjectName(),
            pAttributes
          );
          applyAttributes(
            lNew.getObjectName(),
            (String[]) lAttributes[ 1 ],
            (Object[]) lAttributes[ 0 ]
          );
          
          lReturn = lNew.getObjectName();
        }
      }
      catch( Exception e ) {
        e.printStackTrace();
      }
    }
    return lReturn;
  }

  public Object invoke( String pOperation, NodeList pObjectName, NodeList pAttributes ) {
    Object lReturn = null;
    System.out.println( "XMLAdaptorImpl.invoke(), Operation: " + pOperation );
    if( pOperation != null && !pOperation.equals( "" ) &&
        pObjectName != null && pObjectName.getLength() > 0 )
    {
      try {
        ObjectName lName = createObjectName( (Element) pObjectName.item( 0 ) );
        if( pAttributes != null && pAttributes.getLength() > 0 ) {
          Object[][] lAttributes = getAttributes(
            pAttributes
          );
          // Invoke the method and return the value
          lReturn = mServer.invoke(
            lName,
            pOperation,
            lAttributes[ 0 ],
            (String[]) lAttributes[ 1 ]
          );
        }
        else {
          // Invoke the method and return the value
          lReturn = mServer.invoke(
            lName,
            pOperation,
            new Object[] {},
            new String[] {}
          );
        }
      }
      catch( Exception e ) {
        e.printStackTrace();
      }
    }
    return lReturn;
  }
  // Protected -----------------------------------------------------
  
  protected ObjectName createObjectName( String pName )
    throws
      MalformedObjectNameException
  {
    return new ObjectName( pName );
  }
  
  protected ObjectName createObjectName( Element pObjectName )
    throws
      MalformedObjectNameException
  {
    if( pObjectName.hasAttribute( "name" ) ) {
      return new ObjectName( pObjectName.getAttribute( "name" ) );
    }
    else {
      String lDomain = null;
      if( pObjectName.hasAttribute( "domain" ) ) {
        lDomain = pObjectName.getAttribute( "domain" );
      }
      Hashtable lProperties = new Hashtable();
      NodeList lPropertyList = pObjectName.getElementsByTagName( "property" );
      for( int i = 0; i < lPropertyList.getLength(); i++ ) {
        Element lProperty = (Element) lPropertyList.item( i );
        if( lProperty.hasAttribute( "key" ) && lProperty.hasAttribute( "value" ) ) {
          lProperties.put( lProperty.getAttribute( "key" ), lProperty.getAttribute( "value" ) );
        }
      }
      return new ObjectName( lDomain, lProperties );
    }
  }

  protected Object[][] getAttributes( NodeList pAttributes ) {
    Object[] lReturn = new Object[ 2 ];
    Object[] lValues = new Object[ pAttributes.getLength() ];
    String[] lTypes = new String[ pAttributes.getLength() ];
    // Loop through argument list and create type and values
    for( int i = 0; i < pAttributes.getLength(); i++ ) {
      try {
        Element lArgument = (Element) pAttributes.item( 0 );
        String lTypeString = lArgument.getAttribute( "type" );
        String lValueString = lArgument.getAttribute( "value" );
        Class lClass = null;
        if( mPrimitives.containsKey( lTypeString ) ) {
          lClass = (Class) mPrimitives.get( lTypeString );
        }
        else {
         lClass = Thread.currentThread().getContextClassLoader().loadClass( lTypeString );
        }
        PropertyEditor lEditor = PropertyEditorManager.findEditor( lClass );
        lEditor.setAsText( lValueString );
        lValues[ i ] = lEditor.getValue();
        lTypes[ i ] = lClass.getName();
      }
      catch( Exception e ) {
        e.printStackTrace();
      }
    }
    lReturn[ 0 ] = lValues;
    lReturn[ 1 ] = lTypes;
    return (Object[][]) lReturn;
  }
  protected Object[][] getAttributes( ObjectName pName, NodeList pAttributes ) {
    Object[] lReturn = new Object[ 2 ];
    Object[] lValues = new Object[ pAttributes.getLength() ];
    String[] lTypes = new String[ pAttributes.getLength() ];

    try {
      MBeanAttributeInfo[] attributes = mServer.getMBeanInfo( pName ).getAttributes();
      // Loop through argument list and create type and values
      for( int i = 0; i < pAttributes.getLength(); i++ ) {
        Element lArgument = (Element) pAttributes.item( 0 );
        String lNameString = lArgument.getAttribute( "name" );
        String lValueString = lArgument.getAttribute( "value" );
        for( int k = 0; k < attributes.length; k++ ) {
          if( attributes[ k ].getName().equals( lNameString ) ) { 
            String lTypeString = attributes[ k ].getType();
            Class lClass;
            if( mPrimitives.containsKey( lTypeString ) ) {
               lClass = (Class) mPrimitives.get( lTypeString );
            }
            else {
              lClass = Thread.currentThread().getContextClassLoader().loadClass( lTypeString );
            }
            PropertyEditor lEditor = PropertyEditorManager.findEditor( lClass );
            lEditor.setAsText( lValueString );
            lValues[ i ] = lEditor.getValue();
            lTypes[ i ] = lClass.getName();
          }
        }
      }
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
    lReturn[ 0 ] = lValues;
    lReturn[ 1 ] = lTypes;
    return (Object[][]) lReturn;
  }
  
  protected void applyAttributes(
    ObjectName pName,
    String[] pNames,
    Object[] pValues
  ) {
    try {
      if( pName != null && pNames != null && pValues != null ) {
        if( pNames.length == pValues.length ) {
          AttributeList lList = new AttributeList();
          for( int i = 0; i < pNames.length; i++ ) {
            String lName = pNames[ i ];
            if( lName != null && !lName.equals( "" ) ) {
              // Create Value from attribute type and given string representation
              lList.add( new Attribute( lName, pValues[ i ] ) );
            }
          }
          mServer.setAttributes( pName, lList );
        }
      }
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }
}
