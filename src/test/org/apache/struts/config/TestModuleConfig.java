/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/config/TestModuleConfig.java,v 1.8 2004/03/14 06:23:48 sraeburn Exp $
 * $Revision: 1.8 $
 * $Date: 2004/03/14 06:23:48 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.config;


import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.digester.Digester;
import org.apache.struts.Globals;


/**
 * Unit tests for the <code>org.apache.struts.config</code> package.
 *
 * @version $Revision: 1.8 $ $Date: 2004/03/14 06:23:48 $
 */

public class TestModuleConfig extends TestCase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The ModuleConfig we are testing.
     */
    protected ModuleConfig config = null;


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public TestModuleConfig(String name) {

        super(name);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {

        ModuleConfigFactory factoryObject =
            ModuleConfigFactory.createFactory();
        config = factoryObject.createModuleConfig("");

    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {

        return (new TestSuite(TestModuleConfig.class));

    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {

        config = null;

    }


    // ------------------------------------------------ Individual Test Methods
    private void parseConfig(String publicId, String entityURL,String strutsConfig) {


        // Prepare a Digester for parsing a struts-config.xml file
        Digester digester = new Digester();
        digester.push(config);
        digester.setNamespaceAware(true);
        digester.setValidating(true);
        digester.addRuleSet(new ConfigRuleSet());
        digester.register
            (publicId,
             this.getClass().getResource
             (entityURL).toString());

        // Parse the test struts-config.xml file
        try {
            InputStream input = this.getClass().getResourceAsStream(strutsConfig);
            assertNotNull("Got an input stream for "+strutsConfig, input);
            digester.parse(input);
            input.close();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            fail("Parsing threw exception:  " + t);
        }



    }


    /**
     * Test parsing of a struts-config.xml file.
     */
    public void testParse() {

        testParseBase("-//Apache Software Foundation//DTD Struts Configuration 1.2//EN",
             "/org/apache/struts/resources/struts-config_1_2.dtd",
                    "/org/apache/struts/config/struts-config.xml");

    }

    public void testParse1_1() {

        testParseBase("-//Apache Software Foundation//DTD Struts Configuration 1.1//EN",
             "/org/apache/struts/resources/struts-config_1_1.dtd",
                    "/org/apache/struts/config/struts-config-1.1.xml");

    }

    public void testParseBase(String publicId, String entityURL,String strutsConfig) {

        parseConfig(publicId,entityURL, strutsConfig);

        // Perform assertion tests on the parsed information

        DataSourceConfig dsc =
            config.findDataSourceConfig(Globals.DATA_SOURCE_KEY);
        assertNotNull("Found our data source configuration", dsc);
        assertEquals("Data source driverClass",
                     "org.postgresql.Driver",
                     (String) dsc.getProperties().get("driverClass"));

        assertEquals("Data source description",
                     "Example Data Source Configuration",
                     (String) dsc.getProperties().get("description"));

        FormBeanConfig fbcs[] = config.findFormBeanConfigs();
        assertNotNull("Found our form bean configurations", fbcs);
        assertEquals("Found three form bean configurations",
                     3, fbcs.length);

        ForwardConfig fcs[] = config.findForwardConfigs();
        assertNotNull("Found our forward configurations", fcs);
        assertEquals("Found three forward configurations",
                     3, fcs.length);

        ActionConfig logon = config.findActionConfig("/logon");
        assertNotNull("Found logon action configuration", logon);
        assertEquals("Found correct logon configuration",
                     "logonForm",
                     logon.getName());


    }


   /**
     * Tests a struts-config.xml that contains a custom mapping and property.
     */
    public void testCustomMappingParse() {

       // Prepare a Digester for parsing a struts-config.xml file
       testCustomMappingParseBase
            ("-//Apache Software Foundation//DTD Struts Configuration 1.2//EN",
             "/org/apache/struts/resources/struts-config_1_2.dtd",
                    "/org/apache/struts/config/struts-config-custom-mapping.xml");
    }

    /**
      * Tests a struts-config.xml that contains a custom mapping and property.
      */
     public void testCustomMappingParse1_1() {


         // Prepare a Digester for parsing a struts-config.xml file
        testCustomMappingParseBase
             ("-//Apache Software Foundation//DTD Struts Configuration 1.1//EN",
              "/org/apache/struts/resources/struts-config_1_1.dtd",
                     "/org/apache/struts/config/struts-config-custom-mapping.xml");

     }


    /**
      * Tests a struts-config.xml that contains a custom mapping and property.
      */
     private void testCustomMappingParseBase(String publicId, String entityURL,String strutsConfig) {


         parseConfig(publicId,entityURL, strutsConfig);

         // Perform assertion tests on the parsed information
         CustomMappingTest map = (CustomMappingTest)config.findActionConfig("/editRegistration");
         assertNotNull("Cannot find editRegistration mapping", map);
         assertTrue("The custom mapping attribute has not been set", map.getPublic());

     }

}
