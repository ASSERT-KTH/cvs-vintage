/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/tiles/xmlDefinition/XmlParser.java,v 1.1 2002/06/25 03:15:43 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2002/06/25 03:15:43 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


package org.apache.struts.tiles.xmlDefinition;

import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.*;
import java.net.URL;

import org.apache.commons.digester.Digester;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import org.xml.sax.SAXException;

import org.apache.struts.tiles.NoSuchDefinitionException;

import java.io.Reader;

/**
 *Parse an XML definitions file.
 */
public class XmlParser
{

    /** Associated digester */
  protected Digester digester;
    /**
     * Should we use a validating XML parser to read the configuration file.
     * Default is false.
     */
    protected boolean validating = false;
    /**
     * Digester debug level. DEfault = 0.
     */
    protected int digesterDebugLevel = 0;

    /**
     * The set of public identifiers, and corresponding resource names, for
     * the versions of the configuration file DTDs that we know about.  There
     * <strong>MUST</strong> be an even number of Strings in this list!
     */
    protected String registrations[] = {
        "-//Apache Software Foundation//DTD Tiles Configuration//EN",
        "/org/apache/struts/tiles/resources/tiles-config.dtd",
        "-//Apache Software Foundation//DTD Components Configuration//EN",
        "/org/apache/struts/tiles/resources/tiles-config.dtd",
    };

     /**
      * Constructor.
      * Create a digester parser, and initialize syntax rules.
      */
  public XmlParser()
  {
	digester = new Digester();
	digester.setDebug(digesterDebugLevel);
	digester.setValidating(validating);
	digester.setNamespaceAware(true);
  //digester.setUseContextClassLoader(true);
	// Register our local copy of the DTDs that we can find
  for (int i = 0; i < registrations.length; i += 2) {
      URL url = this.getClass().getResource(registrations[i+1]);
      if (url != null)
          {
          digester.register(registrations[i], url.toString());
          }
  }
    // Init syntax rules
  initDigester( digester );
  }

    /**
     * Set digester validating flag.
     */
  public void setValidating( boolean validating )
    {
    digester.setValidating( validating);
    }

    /**
     * Set digester detail level.
     */
  public void setDetailLevel( int detailLevel )
    {
    digester.setDebug( detailLevel);
    }

   /**
    * Init digester for components syntax.
    * This is an old set of rules, left for backward compatibilities.
    *
    */
  private void initDigesterForComponentsDefinitionsSyntax( Digester digester )
  {
	 // Common constants
  String PACKAGE_NAME = "org.apache.struts.tiles.xmlDefinition";
  String DEFINITION_TAG = "component-definitions/definition";
  String definitionHandlerClass = PACKAGE_NAME + ".XmlDefinition";

  String PUT_TAG  = DEFINITION_TAG + "/put";
  String putAttributeHandlerClass = PACKAGE_NAME + ".XmlAttribute";

  String LIST_TAG = DEFINITION_TAG + "/putList";
  String listHandlerClass     = PACKAGE_NAME + ".XmlListAttribute";

  String ADD_LIST_ELE_TAG = LIST_TAG + "/add";

    // syntax rules
	digester.addObjectCreate(  DEFINITION_TAG, definitionHandlerClass );
	digester.addSetProperties( DEFINITION_TAG);
	digester.addSetNext(       DEFINITION_TAG, "putDefinition", definitionHandlerClass);
    // put / putAttribute rules
	digester.addObjectCreate(  PUT_TAG, putAttributeHandlerClass);
	digester.addSetNext(       PUT_TAG, "addAttribute", putAttributeHandlerClass);
	digester.addSetProperties( PUT_TAG);
	digester.addCallMethod(    PUT_TAG, "setBody", 0);
    // list rules
	digester.addObjectCreate(  LIST_TAG, listHandlerClass);
	digester.addSetProperties( LIST_TAG);
	digester.addSetNext(       LIST_TAG, "addAttribute", putAttributeHandlerClass);
    // list elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
	digester.addObjectCreate(  ADD_LIST_ELE_TAG, putAttributeHandlerClass);
	digester.addSetNext(       ADD_LIST_ELE_TAG, "add", putAttributeHandlerClass);
	digester.addSetProperties( ADD_LIST_ELE_TAG);
	digester.addCallMethod(    ADD_LIST_ELE_TAG, "setBody", 0);
  }

   /**
    * Init digester for tiles syntax.
    * Same as components, but with first element = tiles-definitions
    *
    */
  private void initDigesterForTilesDefinitionsSyntax( Digester digester )
  {
	 // Common constants
  String PACKAGE_NAME = "org.apache.struts.tiles.xmlDefinition";
  String DEFINITION_TAG = "tiles-definitions/definition";
  String definitionHandlerClass = PACKAGE_NAME + ".XmlDefinition";

  String PUT_TAG  = DEFINITION_TAG + "/put";
  String putAttributeHandlerClass = PACKAGE_NAME + ".XmlAttribute";

  //String LIST_TAG = DEFINITION_TAG + "/putList";
    // List tag value
  String LIST_TAG = "putList";
  String DEF_LIST_TAG = DEFINITION_TAG + "/" + LIST_TAG;
  String listHandlerClass     = PACKAGE_NAME + ".XmlListAttribute";
    // Tag value for adding an element in a list
  String ADD_LIST_ELE_TAG = "*/" + LIST_TAG + "/add";

    // syntax rules
	digester.addObjectCreate(  DEFINITION_TAG, definitionHandlerClass );
	digester.addSetProperties( DEFINITION_TAG);
	digester.addSetNext(       DEFINITION_TAG, "putDefinition", definitionHandlerClass);
    // put / putAttribute rules
    // Rules for a same pattern are called in order, but rule.end() are called
    // in reverse order.
    // SetNext and CallMethod use rule.end() method. So, placing SetNext in
    // first position ensure it will be called last (sic).
	digester.addObjectCreate(  PUT_TAG, putAttributeHandlerClass);
	digester.addSetNext(       PUT_TAG, "addAttribute", putAttributeHandlerClass);
	digester.addSetProperties( PUT_TAG);
	digester.addCallMethod(    PUT_TAG, "setBody", 0);
    // Definition level list rules
    // This is rules for lists nested in a definition
	digester.addObjectCreate(  DEF_LIST_TAG, listHandlerClass);
	digester.addSetProperties( DEF_LIST_TAG);
	digester.addSetNext(       DEF_LIST_TAG, "addAttribute", putAttributeHandlerClass);
    // list elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
	digester.addObjectCreate(  ADD_LIST_ELE_TAG, putAttributeHandlerClass);
	digester.addSetNext(       ADD_LIST_ELE_TAG, "add", putAttributeHandlerClass);
	digester.addSetProperties( ADD_LIST_ELE_TAG);
	digester.addCallMethod(    ADD_LIST_ELE_TAG, "setBody", 0);

    // nested list elements rules
    // Create a list handler, and add it to parent list
  String NESTED_LIST = "*/" + LIST_TAG + "/" + LIST_TAG;
	digester.addObjectCreate(  NESTED_LIST, listHandlerClass);
	digester.addSetProperties( NESTED_LIST);
	digester.addSetNext(       NESTED_LIST, "add", putAttributeHandlerClass);

    // bean elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
  //String ADD_WILDCARD = LIST_TAG + "/addItem";
  // non String ADD_WILDCARD = LIST_TAG + "/addx*";
  String ADD_WILDCARD = "*/item";
  String menuItemDefaultClass = "org.apache.struts.tiles.beans.SimpleMenuItem";
	digester.addObjectCreate(  ADD_WILDCARD, menuItemDefaultClass, "classtype");
	digester.addSetNext(       ADD_WILDCARD, "add", "java.lang.Object");
	digester.addSetProperties( ADD_WILDCARD);
  }

   /**
    * Init digester in order to parse instances definition file syntax.
    * Instances is an old name for "definition". This method is left for
    * backward compatibility.
    *
    */
  private void initDigesterForInstancesSyntax( Digester digester )
  {
    	// Build a digester to process our configuration resource
  String PACKAGE_NAME = "org.apache.struts.tiles.xmlDefinition";
  String INSTANCE_TAG = "component-instances/instance";
  String instanceHandlerClass = PACKAGE_NAME + ".XmlDefinition";

  String PUT_TAG = INSTANCE_TAG + "/put";
  String PUTATTRIBUTE_TAG = INSTANCE_TAG + "/putAttribute";
  String putAttributeHandlerClass = PACKAGE_NAME + ".XmlAttribute";

  String LIST_TAG     = INSTANCE_TAG + "/putList";
  String listHandlerClass     = PACKAGE_NAME + ".XmlListAttribute";

  String ADD_LIST_ELE_TAG = LIST_TAG + "/add";

    // component instance rules
	digester.addObjectCreate(  INSTANCE_TAG, instanceHandlerClass );
	digester.addSetProperties( INSTANCE_TAG);
	digester.addSetNext(       INSTANCE_TAG, "putDefinition", instanceHandlerClass);
    // put / putAttribute rules
	digester.addObjectCreate(  PUTATTRIBUTE_TAG, putAttributeHandlerClass);
	digester.addSetProperties( PUTATTRIBUTE_TAG);
	digester.addSetNext(       PUTATTRIBUTE_TAG, "addAttribute", putAttributeHandlerClass);
    // put / putAttribute rules
	digester.addObjectCreate(  PUT_TAG, putAttributeHandlerClass);
	digester.addSetProperties( PUT_TAG);
	digester.addSetNext(       PUT_TAG, "addAttribute", putAttributeHandlerClass);
    // list rules
	digester.addObjectCreate(  LIST_TAG, listHandlerClass);
	digester.addSetProperties( LIST_TAG);
	digester.addSetNext(       LIST_TAG, "addAttribute", putAttributeHandlerClass);
    // list elements rules
    // We use Attribute class to avoid rewriting a new class.
    // Name part can't be used in listElement attribute.
	digester.addObjectCreate(  ADD_LIST_ELE_TAG, putAttributeHandlerClass);
	digester.addSetProperties( ADD_LIST_ELE_TAG);
	digester.addSetNext(       ADD_LIST_ELE_TAG, "add", putAttributeHandlerClass);
  }

   /**
    * Init digester.
    *
    */
  protected void initDigester( Digester digester )
  {
  initDigesterForTilesDefinitionsSyntax( digester );
  initDigesterForComponentsDefinitionsSyntax( digester );
  initDigesterForInstancesSyntax( digester );
  }

  /**
   * Parse input reader and add encounter definitions to definitions set.
   * @param in Input stream
   * @param definitions Xml Definitions set to which encountered definition are added.
   * @throws IOException If an error occur during file parsing.
   * @throws SAXException
   */
  public void parse( InputStream in, XmlDefinitionsSet definitions ) throws IOException, SAXException
  {
	try
    {
      // set first object in stack
    //digester.clear();
    digester.push(definitions);
      // parse
	  digester.parse(in);
	  in.close();
	  }
  catch (SAXException e)
    {
	  //throw new ServletException( "Error while parsing " + mappingConfig, e);
    throw e;
	  }

  }

    /**
     * Main method to check file syntax.
     */
  public static void main(String[] args)
  {
  //String filename = "E:/programs/jakarta-tomcat/webapps/wtiles-struts/WEB-INF/tiles-examples-defs.xml";
  String filename = "E:/programs/jakarta-tomcat-4.0.3/webapps/wtiles-struts/WEB-INF/tiles-examples-defs.xml";
  //String filename = "E:/programs/jakarta-tomcat/webapps/wtiles-struts/WEB-INF/tilesDefinitions.xml";
  //String filename = "E:/programs/jakarta-tomcat/webapps/wtiles-channel/WEB-INF/componentDefinitions.xml";
  //String filename2 = "E:/programs/jakarta-tomcat/webapps/wtiles-tutorial/WEB-INF/componentDefinitions.xml";


    if( args.length > 1 )
      {
      filename = args[1];
      } // end if

  System.out.println( "Read file '" + filename  +"'" );

  InputStream input = null;
  InputStream input2 = null;
    // Open file
    try
      {
	    input = new BufferedInputStream(
                             new FileInputStream( filename) );
	//    input2 = new BufferedInputStream(
          //                   new FileInputStream( filename2) );
      }
     catch( IOException ex )
      {
      System.out.println( "can't open file '" + filename + "' : " + ex.getMessage() );
      }
    // Check file syntax
    try
      {
	    XmlParser parser = new XmlParser();
      parser.setValidating(true);
      parser.setDetailLevel(2);
      XmlDefinitionsSet definitions = new XmlDefinitionsSet();
        System.out.println( "  Parse file" );
      parser.parse( input, definitions);
      //  System.out.println( "  Check file 2" );
      //parser.parse( input2, definitions);
        System.out.println( "  done." );
      System.out.println( "  Result : " + definitions.toString() );
      }
     catch( Exception ex )
      {
      System.out.println( "Error during parsing '" + filename + "' : " + ex.getMessage() );
      ex.printStackTrace();
      }
  }

}
