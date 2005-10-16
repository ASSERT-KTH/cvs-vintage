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
package org.columba.core.scripting.interpreter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.columba.core.scripting.config.BeanshellConfig;
import org.columba.core.scripting.model.ColumbaScript;


/**
 * 
 * This class manages and registers all available script interpreters,
 * which must implement the ScriptInterpreter interface.
 * The registration has to be hardcoded so, if you create an new 
 * ScriptInterpreter class, do not forget to add it here.
 * 
 * @author Celso Pinto <cpinto@yimports.com>
 */
public class InterpreterManager
{

  private static final Logger LOG = 
    Logger.getLogger(InterpreterManager.class.getName());

  /**

    The interpreters Map has the format 
      key(script_extension) => value(ScriptInterpreter instance)

  */
  private final Map interpreters;
  
  public InterpreterManager()
  {
    interpreters = new HashMap();
    
    registerInterpreter(new BshInterpreter());
    registerInterpreter(new JythonInterpreter());
    /*registerInterpreter(new GroovyInterperter());*/
  }
  
  public void registerInterpreter(ScriptInterpreter interpreter)
  {
  
    LOG.entering("registerInterpreter",interpreter.getName());
    
    /* find out if the interpreter is already registered */
    /* if so, remove it */
    /*XXX this is not thread-safe, though luck. It isn't supposed to be. */
    for(Iterator it = interpreters.entrySet().iterator();it.hasNext();)
    {
      Map.Entry entry = (Map.Entry)it.next();
      if (entry.getValue().getClass().isInstance(interpreter))
        it.remove();
    }
    
    String[] extensions = interpreter.getSupportedExtensions();
    for(int i=0;i<extensions.length;i++)
    {
      Object previous = interpreters.put(extensions[i],interpreter); 
      if (previous != null)
        LOG.warning(previous.getClass().getName() +  " doesn't handle " +
                            extensions[i] + " anymore");
    }
    
    LOG.exiting("registerInterpreter",interpreter.getName());
    
  }
  
  public void executeScript(ColumbaScript script)
  {
    LOG.entering("executeScript",script.getPath());
    
    ScriptInterpreter interpreter = 
      (ScriptInterpreter)interpreters.get(script.getExtension());
      
    if (interpreter == null)
    {
      LOG.warning("No interpreter found for " + script.getPath() );
      return;
    }   
    
    Map vars = new HashMap();
    vars.put( ScriptInterpreter.SCRIPT_PATH, 
        BeanshellConfig.getInstance().getPath().getPath());

    vars.put(ScriptInterpreter.SCRIPT_OBJ, script);
    
    interpreter.execute(script,vars);
    
    LOG.exiting("executeScript",script.getPath());
  }
  
  public String[] getSupportedExtensions()
  {
    return (String[])interpreters.keySet().toArray(new String[]{});
  }
}
