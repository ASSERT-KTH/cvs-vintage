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
package org.columba.core.scripting.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.columba.core.scripting.config.BeanshellConfig;

import bsh.EvalError;
import bsh.Interpreter;

public class ColumbaScript {

	private final File scriptFile;

	private String name = "", author = "", description = "";

	public ColumbaScript(File file) {
		scriptFile = file;
	}

	public void setMetadata(String name, String author, String desc) {
		this.name = name;
		this.author = author;
		this.description = desc;
	}

	public String getName() {
		if (name.equals(""))
			return scriptFile.getName();
		else
			return name;

	}

	public String getAuthor() {
		return author;
	}

	public String getDescription() {
		return description;
	}

	public void execute() {
		/* TODO execute through the Beanshell engine */
		/*
		 * it's the script responsability to define the "metadata" by invoking
		 * .setName(), .setAuthor() and .setDescription()
		 * 
		 * 
		 */
		System.out.println("Executing bsh: " + getPath());
		Interpreter bsh = new Interpreter();
		try {
			bsh.set("COLUMBA_SCRIPT_PATH", BeanshellConfig.getInstance()
					.getPath().getPath());
			bsh.set("cScript", this);
			bsh.source(getPath());

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (EvalError ex) {
			ex.printStackTrace();
		}
	}

	public long getLastModified() {
		return scriptFile.lastModified();
	}

	public String getPath() {
		return scriptFile.getPath();
	}

	public boolean exists() {
		return scriptFile.exists();
	}

	public boolean deleteFromDisk() {
		return scriptFile.delete();
	}
}
