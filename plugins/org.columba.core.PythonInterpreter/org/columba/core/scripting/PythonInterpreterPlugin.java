/*
 * 
 *
 * This	free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.columba.core.scripting;

import org.columba.core.io.DiskIO;
import org.columba.core.plugin.PluginInterface;
import org.python.core.Py;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.imp;
import org.python.util.PythonInterpreter;

/**
 * 
 * 
 * @author fdietz@users.sourceforge.net
 * 
 * Taken some stuff vom jext (Author: Roman Guy)
 * 
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public final class PythonInterpreterPlugin extends AbstractInterpreter{
	private static PythonInterpreter parser;

	/**
	 * Creates an interpreter.
	 * @param parent The window which executes the script
	 */

	public static PythonInterpreter getPythonInterpreter(Object parent) {
		if (parser == null) {
			//parser =  new PythonInterpreter();
			System.setProperty("python.path", "./plugins/PythonInterpreter/lib/Lib");
			/*
			Properties props = new Properties();
			props.setProperty("python.path", ConfigPath.getConfigDirectory()+ "/ext/Lib");
			PythonInterpreter.initialize(
				System.getProperties(),
				props,
				new String[] { "" });
			*/

			/*
			Properties pyProps = new Properties();
			pyProps.put("python.cachedir", ConfigPath.getConfigDirectory() + "pythoncache" + File.separator);
			PythonInterpreter.initialize(System.getProperties(), pyProps, new String[0]);
			*/

			parser = new PythonInterpreter();

			PyModule mod = imp.addModule("__main__");
			//imp.addModule("/home/frd/apps/eclipse/workspace/TheColumbaProject/org/columba/modules/mail/config/copy.py");

			parser.setLocals(mod.__dict__);

			PySystemState sys = Py.getSystemState();
			//sys.add_package("org.columba.modules.mail.config");

			/*
			BufferedReader in = new BufferedReader(new InputStreamReader(
			                                   JFrame.class.getResourceAsStream("packages")));
			String line;
			try
			{
			while ((line = in.readLine()) != null)
			sys.add_package(line);
			in.close(); 
			} catch (IOException ioe) { }
			*/

		}

		parser.set("__columba__", parent);
		if (parent != null) {
			parser.setErr(System.err);
			parser.setOut(System.out);
			/*
			parser.setErr(parent.getPythonLogWindow().getStdErr());
			parser.setOut(parent.getPythonLogWindow().getStdOut());
			*/

		} else {
			parser.setOut(System.out);
			parser.setErr(System.err);
		}

		return parser;
	}

	/**
	 * Evaluates some Python code.
	 * @param code The script code to be evaluated
	 * @param map A map of properties to add to interpreter
	 * @param parent The window which executes the script
	 * @return The result of the evaluation
	 */

	public static PyObject eval(
		String code,
		String mapName,
		Object[] map,
		Object parent) {
		try {
			PythonInterpreter parser = getPythonInterpreter(parent);

			if (map != null && mapName != null)
				parser.set(mapName, map);

			return parser.eval(code);
		} catch (Exception pe) {
			/*
			JOptionPane.showMessageDialog(
				parent,
				"python.script.errMessage",
				"python.script.error",
				JOptionPane.ERROR_MESSAGE);
			*/
			/*
			if (Jext.getBooleanProperty("dawn.scripting.debug"))
			  System.err.println(pe.toString());
			 */
			pe.printStackTrace();
			// security ?
			parser = null;
		}

		return null;
	}

	/**
	 * Executes some Python code.
	 * @param code The script code to be interpreted
	 * @param parent The window which executes the script
	 */

	public static void execute(String code, Object parent) {
		try {
			PythonInterpreter parser = getPythonInterpreter(parent);
			parser.exec(code);
		} catch (Exception pe) {
			/*
			JOptionPane.showMessageDialog(
				parent,
				"python.script.errMessage",
				"python.script.error",
				JOptionPane.ERROR_MESSAGE);
			*/
			pe.printStackTrace();
			/*
			if (Jext.getBooleanProperty("dawn.scripting.debug"))
			  parent.getPythonLogWindow().logln(pe.toString());
			*/
			// security ?
			parser = null;
		}
	}

	public static void runResource(String resource, Object parent) {
		try {
			String str = DiskIO.readStringFromResource(resource);

			//System.out.println("script-source:\n"+str);

			execute(str, parent);
		} catch (Exception ex) {
		}
	}

	/**
	 * Runs a Jext script from a file.
	 * @param fileName Path to the script
	 * @param parent The Jext window which have to execute the script
	 */

	/* don't use this method - it doesn't work correctly with jar-files
	 * and java webstart
	 */
	public static void runScript(String fileName, Object parent) {
		try {
			PythonInterpreter parser = getPythonInterpreter(parent);
			parser.execfile(fileName);
		} catch (Exception pe) {
			/*
			JOptionPane.showMessageDialog(
				parent,
				"python.script.errMessage",
				"python.script.error",
				JOptionPane.ERROR_MESSAGE);
			*/
			pe.printStackTrace();
			/*
			if (Jext.getBooleanProperty("dawn.scripting.debug"))
			  parent.getPythonLogWindow().logln(pe.toString());
			*/
			// security ?
			parser = null;
		}
	}

	public Object instanciate(
		String fileName,
		String className,
		Object[] args,
		Object parent) {

		try {
			PythonInterpreter parser = getPythonInterpreter(parent);
			parser.execfile(fileName);
			/*
			PyObject pyObject = parser.eval(object+"()");
			
			Object javaObject = pyObject.__tojava__(c);
			*/

			System.out.println("constructor=" + className);

			/*
			PyObject pyObject = parser.eval(className+"()");
			System.out.println("get instance of ="+fileName);
			System.out.println("pyObject="+pyObject);
			
			Object javaObject = pyObject.__tojava__( DefaultPlugin.class);
			*/

			PyObject pyObject = parser.get(className);
			if ( args==null || args.length == 0) {
				pyObject = pyObject.__call__();
			} else {
				PyObject[] pyArgs = new PyObject[args.length];
				for (int i = 0; i < args.length; i++) {
					pyArgs[i] = Py.java2py(args[i]);
				}

				pyObject = pyObject.__call__(pyArgs);
			}
			System.out.println("pyObject=" + pyObject);

			Object javaObject = pyObject.__tojava__(PluginInterface.class);
			System.out.println("javaObject=" + javaObject);
			//( (DefaultPlugin) javaObject).run("asdfljasf");

			return javaObject;

		} catch (Exception pe) {
			pe.printStackTrace();
		}

		return null;
	}
}

// End of Run.java
