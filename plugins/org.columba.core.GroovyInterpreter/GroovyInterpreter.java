// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.columba.core.gui.util.ErrorDialog;
import org.columba.core.loader.ClassLoaderHelper;
import org.columba.core.main.Main;
import org.columba.core.scripting.AbstractInterpreter;



/**
 * Groovy interpreter.
 * 
 * @author fdietz
 */
public class GroovyInterpreter extends AbstractInterpreter {

	public GroovyInterpreter() {
	}

	/**
	 * @see org.columba.core.scripting.AbstractInterpreter#instanciate(java.lang.String,
	 *      java.lang.String, java.lang.Object[])
	 */
	public Object instanciate(String fileName, String className, Object[] args) throws Exception {
		GroovyClassLoader gcl = new GroovyClassLoader(getClass().getClassLoader());

		if (!fileName.endsWith(".groovy"))
			fileName = fileName + ".groovy";

		Class clazz = gcl.parseClass(new File(fileName));
		Constructor constr = ClassLoaderHelper.findConstructor(args, clazz);
		Object object = null;
		if (constr != null)
			object = constr.newInstance(args);
		else
			object = clazz.newInstance();
		
		return object;
		/*
		Class clazz = null;
		try {
			clazz = gcl.parseClass(new File(fileName));

		} catch (CompilationFailedException e) {
			new ErrorDialog(e.getMessage(), e);
			
			if ( Main.DEBUG )
				e.printStackTrace();
		} catch (IOException e) {
			new ErrorDialog(e.getMessage(), e);
			if ( Main.DEBUG )
				e.printStackTrace();
		}

		Constructor constr = null;
		try {
			constr = ClassLoaderHelper.findConstructor(args, clazz);
		} catch (NoSuchMethodException e1) {
			new ErrorDialog(e1.getMessage(), e1);
			
			if ( Main.DEBUG )
				e1.printStackTrace();
		}

		Object aScript;
		try {
			if (constr != null)
				aScript = constr.newInstance(args);
			else
				aScript = clazz.newInstance();
			return aScript;
		} catch (InstantiationException e2) {
			new ErrorDialog(e2.getMessage(), e2);
			if ( Main.DEBUG )
				e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			new ErrorDialog(e2.getMessage(), e2);
			if ( Main.DEBUG )
				e2.printStackTrace();
		} catch (InvocationTargetException e2) {
			new ErrorDialog("Groovy plugin invokation error. Failed to instanciate "+className+".", e2.getCause());
			if ( Main.DEBUG )
				e2.printStackTrace();
		}

		return null;
		*/
	}

}
