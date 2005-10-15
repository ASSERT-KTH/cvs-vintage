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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.columba.core.scripting.config.BeanshellConfig;
import org.columba.core.scripting.model.ColumbaScript;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * @author Celso Pinto <cpinto@yimports.com>
 */
public class BshInterpreter
    implements ScriptInterpreter
{
  private final static String[] EXTENSIONS = new String[]{"bsh","beanshell"};
  
  
  public String getName()
  {
    return "Beanshell Interpreter";
  }

  public String[] getSupportedExtensions()
  {
    return EXTENSIONS;
  }

  public void execute(ColumbaScript script)
  {
    /* TODO execute through the Beanshell engine */
    /*
     * it's the script responsability to define the "metadata" by invoking
     * .setName(), .setAuthor() and .setDescription()
     * 
     * 
     */
    System.out.println("Executing bsh: " + script.getPath());
    Interpreter bsh = new Interpreter();
    try {
      bsh.set("COLUMBA_SCRIPT_PATH", BeanshellConfig.getInstance().getPath().getPath());
      bsh.set("cScript", script);
      bsh.source(script.getPath());

    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (EvalError ex) {
      ex.printStackTrace();
    }
  }

}
