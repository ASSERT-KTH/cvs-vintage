package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.turbine.TurbineConfig;

/**
 * This is a class to help with initialization of Turbine in various
 * packages such as the testing suite as well as in the XML Import/Export.
 * It will initialize Log4J with a properties file as well.
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: TurbineInitialization.java,v 1.5 2003/03/28 00:02:23 jon Exp $
 */
public class TurbineInitialization
{
    public static String tr_props = "/WEB-INF/conf/TurbineResources.properties";

    protected static void initTurbine (String configDir)
        throws Exception
    {
        TurbineConfig tc = new TurbineConfig(configDir, tr_props);
        tc.init();
    }

    public static void setTurbineResources(String trprops)
    {
        tr_props = trprops;
    }
    
    public static void setUp(String configDir, String configFile)
        throws Exception
    {
        if (configDir == null || configFile == null)
        {
            System.err.println("config.dir System property was not defined");
            throw new Exception ("configDir or configFile was null");
        }

        // set this so that the proper substitution will happen in the
        // configFile
        System.getProperties().setProperty("configDir", configDir);

        initTurbine(configDir);
        
        InputStream is = new File(configDir + configFile).toURL()
            .openStream();
        Properties props = new Properties();
        try
        {
            props.load(is);
            // init Log4J
            PropertyConfigurator.configure(props);
        }
        catch (Exception e)
        {
            System.err.println("Can't read the properties file (" + 
                configDir + configFile + "). ");
        }
    }
}
