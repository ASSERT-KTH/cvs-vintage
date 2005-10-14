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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.columba.core.gui.scripting.ScriptManagerDocument;
import org.columba.core.scripting.config.BeanshellConfig;
import org.columba.core.scripting.config.OptionsObserver;
import org.columba.core.scripting.model.ColumbaScript;

/**
 * The FileObserverThread is a timer that polls the file system to check if any
 * new .bsh scripts were created or existing scripts modified, since the last
 * time it was run.<br>
 * <br>
 * The polling behaviour can be enabled or disabled in the &lt;columba
 * dir&gt;/scripts/config.xml, as well as the polling interval. <br>
 * <br>
 * The default polling interval is of 5 seconds.
 * 
 * @author Celso Pinto (cpinto@yimports.com)
 */
public class FileObserverThread extends Thread implements OptionsObserver,
		ScriptManagerDocument {

	private static final Logger LOG = Logger.getLogger(FileObserverThread.class
			.getName());

	/*
	 * TODO at some point in time, move this pattern to the config.xml file but
	 * not everything, just the extension (i.e. bsh) part. Then, it'll enable
	 * the use of this kind of stuff in the config.xml: <filter>bsh</filter>
	 * <filter>groovy</filter> <filter>whatever</filter>
	 * 
	 * which will be passed on in the format bsh|groovy|whatever to the pattern
	 * builder. the resulting pattern will be something like:
	 * ".*\\.(bsh|groovy|whatever)$"
	 */
	private static final String SCRIPT_EXTENSION_PATTERN = ".*\\.(bsh)$";

	private final BeanshellConfig config = BeanshellConfig.getInstance();

	private final BeanshellFileFilter beanshellFilter = new BeanshellFileFilter();

	private long lastExecution = System.currentTimeMillis();

	private boolean finish = false;

	private Map scriptList = new HashMap();

	private int pollingInterval = -1;

	private static FileObserverThread instance = null;

	private List observers = new ArrayList();

	private FileObserverThread() {
		pollingInterval = config.getOptions().getInternalPollingInterval();
	}

	public void setScriptList(Map scripts) {
		scriptList = scripts;
	}

	public void finish() {
		finish = true;
	}

	private synchronized void executeRefresh(boolean force) {

		List changedFiles = checkFiles();

		if (changedFiles.size() > 0)
			execChangedFiles(changedFiles);

		lastExecution = System.currentTimeMillis();

	}

	public void run() {
		config.getOptions().addObserver(this);

		while (!finish) {

			executeRefresh(false);

			try {
				sleep(pollingInterval);
			} catch (InterruptedException ex) {
			}

		}

		config.getOptions().removeObserver(this);
		instance = null;
	}

	private List checkFiles() {

		List changedFiles = new ArrayList(), removedScripts = new ArrayList(), addedScripts = new ArrayList();

		// check current file list for changes
		ColumbaScript script = null;
		Map.Entry entry = null;

		synchronized (scriptList) {

			for (Iterator itCurrent = scriptList.entrySet().iterator(); itCurrent
					.hasNext();) {
				entry = (Map.Entry) itCurrent.next();
				script = (ColumbaScript) entry.getValue();
				if (!script.exists()) {
					// it isn't possible to undo whatever the script did
					removedScripts.add(script);
					itCurrent.remove();
					continue;
				}

				if (script.getLastModified() > lastExecution)
					changedFiles.add(script);

			}

			/* check for new files in the scripts directory */
			File[] scripts = getNewScripts();
			for (int i = 0; i < scripts.length; i++) {
				if (!scriptList.containsKey(scripts[i].getPath())) {
					script = new ColumbaScript(scripts[i]);
					changedFiles.add(script);
					scriptList.put(scripts[i].getPath(), script);
					addedScripts.add(script);
				}
			}

		}

		for (Iterator it = observers.iterator(); it.hasNext();) {

			IScriptsObserver obs = (IScriptsObserver) it.next();
			if (removedScripts.size() > 0)
				obs.scriptsRemoved(removedScripts);

			if (addedScripts.size() > 0)
				obs.scriptsAdded(addedScripts);

			if (changedFiles.size() > 0)
				obs.scriptsChanged(changedFiles);

		}

		return changedFiles;

	}

	private File[] getNewScripts() {
		/*
		 * I specifically want this here to ensure that the directory exists and
		 * this method never returns null.
		 * 
		 * Any files that were in the observation list have already been
		 * previously removed by checkFiles().
		 */
		File configPath = config.getPath();
		if (!configPath.exists() || !configPath.isDirectory()) {
			LOG.warning("Scripts directory doesn't exist:"
					+ configPath.getPath());
			return new File[] {};
		}

		return configPath.listFiles(beanshellFilter);
	}

	private void execChangedFiles(List files) {
		for (Iterator it = files.iterator(); it.hasNext();)
			((ColumbaScript) it.next()).execute();
	}

	private class BeanshellFileFilter implements FileFilter {

		private Pattern extensionPattern = Pattern
				.compile(SCRIPT_EXTENSION_PATTERN);

		public boolean accept(File aPathname) {
			return extensionPattern.matcher(aPathname.getPath()).matches();
		}

	}

	public void pollingIntervalChanged(int interval) {
		pollingInterval = interval;
	}

	public void pollingStateChanged(boolean enabled) {
	}

	public static FileObserverThread getInstance() {
		if (instance == null)
			instance = new FileObserverThread();

		return instance;
	}

	public void addObserver(IScriptsObserver observer) {
		if (!observers.contains(observer))
			observers.add(observer);
	}

	public void removeObserver(IScriptsObserver observer) {
		observers.remove(observer);
	}

	public List getScripts() {
		return new ArrayList(scriptList.values());
	}

	public void refreshScriptList() {
		executeRefresh(true);
	}

	public ColumbaScript getScript(String path) {
		return (ColumbaScript) scriptList.get(path);
	}

	public void removeScript(ColumbaScript[] scripts) {

		synchronized (scriptList) {

			for (int i = 0; i < scripts.length; i++) {

				/* really delete file */
				LOG.fine("Removing script: " + scripts[i].getPath());

				if (scripts[i].exists())
					scripts[i].deleteFromDisk();

				/* remove from script list */
				scriptList.remove(scripts[i].getPath());

			}

		}

		List removed = Arrays.asList(scripts);
		for (Iterator it = observers.iterator(); it.hasNext();)
			((IScriptsObserver) it.next()).scriptsRemoved(removed);

	}

}
