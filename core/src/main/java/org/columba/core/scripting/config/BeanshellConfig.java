/*
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the 
  License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
  
  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
  for the specific language governing rights and
  limitations under the License.

  The Original Code is "The Columba Project"
  
  The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
  Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
  
  All Rights Reserved.
*/
package org.columba.core.scripting.config;

import java.io.File;

import org.columba.core.config.Config;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.io.DiskIO;

/**
 Service configuration class. To access the service options use the getOptions
 accessor.

 @author Celso Pinto (cpinto@yimports.com)
*/
public class BeanshellConfig
{

    private static final String CORE_MODULE = "core";

	private static final String COLUMBA_SCRIPT_DIRECTORY = "scripts", OPTIONS_FILE = "scripting.xml";

    protected Config config;

    protected File path;

    protected File optionsFile;

    private static BeanshellConfig instance;

    private BeanshellConfig(Config config)
    {

        this.config = config;
        
        // scripts should reside in <config-folder>/scripts/ directory
        path = new File(config.getConfigDirectory(), COLUMBA_SCRIPT_DIRECTORY);
        DiskIO.ensureDirectory(path);

        // scripting.xml configuration file should reside in <config-folder>
        optionsFile = new File(config.getConfigDirectory(), OPTIONS_FILE);
        
        registerPlugin(optionsFile.getName(), new ScriptingXmlConfig(optionsFile));

    }

    private String getModuleName()
    {
        return CORE_MODULE;
    }

    public File getPath()
    {
        return path;
    }

    public Options getOptions()
    {
        return ((ScriptingXmlConfig) getPlugin(optionsFile.getName()))
            .getOptions();
    }

    private void registerPlugin(String id, DefaultXmlConfig plugin)
    {
        config.registerPlugin(getModuleName(), id, plugin);
    }

    private DefaultXmlConfig getPlugin(String id)
    {
        return config.getPlugin(getModuleName(), id);
    }

    public static BeanshellConfig getInstance()
    {
        if (instance == null) instance = new BeanshellConfig(Config.getInstance());

        return instance;
    }
}
