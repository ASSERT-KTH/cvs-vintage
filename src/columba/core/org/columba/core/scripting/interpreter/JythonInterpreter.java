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

import java.util.Iterator;
import java.util.Map;

import org.columba.core.scripting.model.ColumbaScript;
import org.python.util.PythonInterpreter;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class JythonInterpreter
    extends ScriptInterpreter
{

    private final static String[] EXTENSIONS = new String[]{"py", "jython"};

    public String getName()
    {
        return "Jython Interpreter";
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
        logger.append("Executing jython: " + script.getPath());

        PythonInterpreter jython = new PythonInterpreter();

        for (Iterator it = vars.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            jython.set(entry.getKey().toString(), entry.getValue());
        }

        jython.execfile(script.getPath());
        jython.cleanup();

    }

}
