/*
 The contents of this file are subject to the Mozilla Public License Version 1.1
 (the "License"); you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Original Code is "The Columba Project"

 The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
 Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

 All Rights Reserved.
 */
package org.columba.core.scripting.interpreter;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.columba.core.scripting.model.ColumbaScript;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class GroovyInterpreter
    extends ScriptInterpreter
{

    private final static String[] EXTENSIONS = new String[]{"groovy"};

    public String getName()
    {
        return "Goovy Interpreter";
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
        logger.append("Loading groovy script: " + script.getPath());

        GroovyClassLoader gcl = new GroovyClassLoader(getClass().getClassLoader());

        File groovyFile = new File(script.getPath());

        try
        {
            Class clazz = gcl.parseClass(groovyFile);

            /*
            if (!GroovyScriptInterface.class.isAssignableFrom(clazz))
            {
                System.out.printf("Goovy script %s doesn't implement GroovyScriptInterface. " +
                                    "Skipping...",script.getPath());
                return;
            }
            */

            Method mainMethod = null;
            boolean hasMain = true;
            try
            {
                mainMethod = clazz.getDeclaredMethod("main", java.util.Map.class);
            }
            catch (NoSuchMethodException e)
            {
                hasMain = false;
            }

            if (!hasMain ||
                (mainMethod.getModifiers() & Modifier.PUBLIC) == 0 ||
                (mainMethod.getModifiers() & Modifier.STATIC) == 0)
            {
                logger.append(  "Failed to load script",
                                String.format("Groovy script %s doesn't declare a " +
                                                    "'public static main(Map)' method. Skipping....",
                                                script.getPath()));
                return;
            }

            mainMethod.invoke(null, vars);

        }
        catch (CompilationFailedException e)
        {
            logger.append(String.format("Failed compilation of %s", script.getPath()), e);
        }
        catch (IOException e)
        {
            logger.append(String.format("I/O Exception in %s", script.getPath()), e);
        }
        catch (IllegalAccessException e)
        {
            logger.append(String.format("IllegalAccessException when calling main in %s",
                                            script.getPath()),
                                        e);
        }
        catch (InvocationTargetException e)
        {
            logger.append(String.format("InvocationTargetException when calling main in %s",
                                            script.getPath()),
                                        e);
        }

    }

}
