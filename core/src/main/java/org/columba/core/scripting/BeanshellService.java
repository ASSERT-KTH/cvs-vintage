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
package org.columba.core.scripting;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import org.columba.core.io.DiskIO;
import org.columba.core.scripting.config.BeanshellConfig;
import org.columba.core.scripting.service.api.IColumbaService;

/**
 This class represents the Beanshell Service.<br>
 The Beanshell Service enables the use of scriptable plugins, meaning a
 3rd-party developer can created plugins based on beanshell scripts. <br>
 To create a Beanshell plugin, the 3rd party must create one script with a
 .bsh extension and copy the file to the ~/.columba/scripts directory. <br>
 <br>
 The service will then automaticaly pick up the script and execute it. <br>
 If a plugin depends on more than one script, then only the entry point should
 have the .bsh extension, for example:<br> - my_plugin.bsh<br> -
 my_plugin.file_2<br> - my_plugin.file_3<br> - ...<br>
 <br>
 <br>
 <strong>This is still alpha software so expect things to change.</strong>
 <br>

 @author Celso Pinto (cpinto@yimports.com)
*/
public class BeanshellService
    implements  IColumbaService,
                Observer
{

    private static final Logger LOG = Logger.getLogger(BeanshellService.class.getName());

    private BeanshellConfig config = BeanshellConfig.getInstance();

    private Map beanshellScripts = new HashMap();
    private ScriptLogger logger = ScriptLogger.getInstance();

    public BeanshellService()
    {
        logger.addObserver(this);
    }

    /**
     @see org.columba.core.scripting.service.api.IColumbaService#initService()
     */
    public boolean initService()
    {

        /* check if script directory exists */
        if (!DiskIO.ensureDirectory(config.getPath())) return false;

        /*
           * initialize file observer thread with a reference to our
           * beanshellScripts map
           */
        FileObserverThread.getInstance().setScriptList(beanshellScripts);

        LOG.info("BeanshellService initialized...");
        return true;

    }

    /**
     @see org.columba.core.scripting.service.api.IColumbaService#disposeService()
     */
    public void disposeService()
    {
        /* nothing to dispose, yet... */
    }

    /**
     @see org.columba.core.scripting.service.api.IColumbaService#startService()
     */
    public void startService()
    {
        /* start pooling thread */
        logger.append("Starting " + getClass().getName());
        logger.append("Starting FileObserverThread...");
        logger.addObserver(this); /*in case of a stop-start */
        FileObserverThread.getInstance().start();
    }

    /**
     @see org.columba.core.scripting.service.api.IColumbaService#stopService()
     */
    public void stopService()
    {

        logger.append("Stoping " + getClass().getName());
        logger.append("Stopping FileObserverThread...");

        logger.deleteObserver(this);

        FileObserverThread.getInstance().finish();

    }

    public Map getBeanshellScripts()
    {
        return beanshellScripts;
    }

    public ScriptLogger getLogger()
    {
        return logger;
    }

    public void update(Observable o, Object arg)
    {
        ScriptLogger.LogEntry log = (ScriptLogger.LogEntry) arg;
        LOG.finest(log.getMessage());
        LOG.finest(log.getDetails());
    }
}
