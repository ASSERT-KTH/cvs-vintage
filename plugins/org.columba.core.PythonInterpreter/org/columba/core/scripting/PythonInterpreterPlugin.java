//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.scripting;

import java.io.File;

import org.columba.core.io.DiskIO;
import org.columba.core.plugin.Plugin;
import org.python.core.Py;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.imp;
import org.python.util.PythonInterpreter;

/**
 * Python interpreter plugin. 
 * <p>
 * some stuff vom jext (Author: Roman Guy)
 * 
 * @author fdietz@users.sourceforge.net 
 */
public final class PythonInterpreterPlugin extends AbstractInterpreter {
	private static PythonInterpreter parser;

	/**
	 * Creates an interpreter.
	 * 
	 * @param parent
	 *            The window which executes the script
	 */

	public static PythonInterpreter getPythonInterpreter() {
		if (parser == null) {

			File file = new File(
					"plugins/org.columba.core.PythonInterpreter/lib/Lib");

			System.setProperty("python.path", file.getAbsolutePath());

			parser = new PythonInterpreter();

			PyModule mod = imp.addModule("__main__");

			parser.setLocals(mod.__dict__);

			PySystemState sys = Py.getSystemState();

			//sys.add_package("org.columba.modules.mail.config");

			/*
			 * BufferedReader in = new BufferedReader(new InputStreamReader(
			 * JFrame.class.getResourceAsStream("packages"))); String line; try {
			 * while ((line = in.readLine()) != null) sys.add_package(line);
			 * in.close(); } catch (IOException ioe) { }
			 */

		}

		parser.set("__columba__", "");

		parser.setOut(System.out);
		parser.setErr(System.err);

		return parser;
	}

	/**
	 * Evaluates some Python code.
	 * 
	 * @param code
	 *            The script code to be evaluated
	 * @param map
	 *            A map of properties to add to interpreter
	 * @param parent
	 *            The window which executes the script
	 * @return The result of the evaluation
	 */

	public static PyObject eval(String code, String mapName, Object[] map) {
		try {
			PythonInterpreter parser = getPythonInterpreter();

			if (map != null && mapName != null)
				parser.set(mapName, map);

			return parser.eval(code);
		} catch (Exception pe) {

			pe.printStackTrace();
			// security ?
			parser = null;
		}

		return null;
	}

	/**
	 * Executes some Python code.
	 * 
	 * @param code
	 *            The script code to be interpreted
	 * @param parent
	 *            The window which executes the script
	 */

	public static void execute(String code) {
		try {
			PythonInterpreter parser = getPythonInterpreter();
			parser.exec(code);
		} catch (Exception pe) {

			pe.printStackTrace();

			// security ?
			parser = null;
		}
	}

	public static void runResource(String resource, Object parent) {
		try {
			String str = DiskIO.readStringFromResource(resource);

			//System.out.println("script-source:\n"+str);

			execute(str);
		} catch (Exception ex) {
		}
	}

	/**
	 * Runs a Jext script from a file.
	 * 
	 * @param fileName
	 *            Path to the script
	 * @param parent
	 *            The Jext window which have to execute the script
	 */

	/*
	 * don't use this method - it doesn't work correctly with jar-files and java
	 * webstart
	 */
	public static void runScript(String fileName) {
		try {
			PythonInterpreter parser = getPythonInterpreter();
			parser.execfile(fileName);
		} catch (Exception pe) {

			pe.printStackTrace();

			// security ?
			parser = null;
		}
	}

	public Object instanciate(String fileName, String className, Object[] args) throws Exception{

		try {
			PythonInterpreter parser = getPythonInterpreter();
			parser.execfile(fileName);
			
			String interpreterClass = className.substring(0, className.length() - 3);

			PyObject pyObject = parser.get(interpreterClass);
			if (args == null || args.length == 0) {
				pyObject = pyObject.__call__();
			} else {
				PyObject[] pyArgs = new PyObject[args.length];
				for (int i = 0; i < args.length; i++) {
					pyArgs[i] = Py.java2py(args[i]);
				}

				pyObject = pyObject.__call__(pyArgs);
			}

			Object javaObject = pyObject.__tojava__(Plugin.class);

			return javaObject;

		} catch (Exception pe) {
			pe.printStackTrace();
		}

		return null;
	}
}

// End of Run.java
