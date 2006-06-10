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
import java.util.Iterator;
import java.util.Map;

import org.columba.core.scripting.model.ColumbaScript;

import bsh.EvalError;
import bsh.Interpreter;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class BshInterpreter
    extends ScriptInterpreter
{
    private final static String[] EXTENSIONS = new String[]{"bsh", "beanshell"};


    public String getName()
    {
        return "Beanshell Interpreter";
    }

    public String[] getSupportedExtensions()
    {
        return EXTENSIONS;
    }

    public void execute(ColumbaScript script, Map vars)
    {
        /*
        * it's the script responsability to define the "metadata" by invoking
        * .setName(), .setAuthor() and .setDescription()
        */

        logger.append("Executing bsh: " + script.getPath());

        Interpreter bsh = new Interpreter();
        bsh.setClassLoader(getClass().getClassLoader());

        try
        {
            for (Iterator it = vars.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry) it.next();
                bsh.set(entry.getKey().toString(), entry.getValue());
            }

            bsh.source(script.getPath());
        }
        catch (FileNotFoundException ex)
        {
            logger.append(String.format("File %s not found", script.getPath()), ex);
        }
        catch (IOException ex)
        {
            logger.append(String.format("IOException in %s", script.getPath()), ex);
        }
        catch (EvalError ex)
        {
            logger.append(String.format("Failed to process script %s", script.getPath()), ex);
        }
    }

}
