/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.service.prefs.BackingStoreException;

/**
 *
 */
public class UserLibraryManager {
	
	public final static String CP_USERLIBRARY_PREFERENCES_PREFIX = JavaCore.PLUGIN_ID+".userLibrary."; //$NON-NLS-1$
	public final static String CP_ENTRY_IGNORE = "##<cp entry ignore>##"; //$NON-NLS-1$

	private static Map userLibraries;
	private static final boolean logProblems= false;
	private static IEclipsePreferences.IPreferenceChangeListener listener= new IEclipsePreferences.IPreferenceChangeListener() {

		public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
			String key= event.getKey();
			if (key.startsWith(CP_USERLIBRARY_PREFERENCES_PREFIX)) {
				try {
					recreatePersistedUserLibraryEntry(key, (String) event.getNewValue(), false, true);
				} catch (JavaModelException e) {
					if (logProblems) {
						Util.log(e, "Exception while rebinding user library '"+ key.substring(CP_USERLIBRARY_PREFERENCES_PREFIX.length()) +"'."); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
				}
			}
		}
	};
	
	private UserLibraryManager() {
		// do not instantiate
	}
		
	/**
	 * Returns the names of all defined user libraries. The corresponding classpath container path
	 * is the name appended to the CONTAINER_ID.  
	 * @return Return an array containing the names of all known user defined.
	 */
	public static String[] getUserLibraryNames() {
		Set set= getLibraryMap().keySet();
		return (String[]) set.toArray(new String[set.size()]);
	}
	
	/**
	 * Gets the library for a given name or <code>null</code> if no such library exists.
	 * @param name The name of the library
	 * @return The library registered for the given name or <code>null</code>.
	 */
	public static UserLibrary getUserLibrary(String name) {
		return (UserLibrary) getLibraryMap().get(name);
	}

	/**
	 * Registers user libraries for given names. If a library for the given name already exists, its value will be updated.
	 * This call will also rebind all related classpath container. 
	 * @param newNames The names to register the libraries for
	 * @param newLibs The libraries to register
	 * @param monitor A progress monitor used when rebinding the classpath containers
	 * @throws JavaModelException
	 */
	public static void setUserLibraries(String[] newNames, UserLibrary[] newLibs, IProgressMonitor monitor) throws JavaModelException {
		Assert.isTrue(newNames.length == newLibs.length, "names and libraries should have the same length"); //$NON-NLS-1$
		
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		
		monitor.beginTask("Configure user libraries...", newNames.length);	//$NON-NLS-1$
		try {
			int last= newNames.length - 1;
			for (int i= 0; i < newLibs.length; i++) {
				internalSetUserLibrary(newNames[i], newLibs[i], i == last, true, new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Registers a user library for a given name. If a library for the given name already exists, its value will be updated.
	 * This call will also rebind all related classpath container. 
	 * @param name The name to register the library for
	 * @param library The library to register
	 * @param monitor A progress monitor used when rebinding the classpath containers
	 * @throws JavaModelException
	 */
	public static void setUserLibrary(String name, UserLibrary library, IProgressMonitor monitor) throws JavaModelException {
		internalSetUserLibrary(name, library, true, true, monitor);
	}
	
	static Map getLibraryMap() {
		if (userLibraries == null) {
			userLibraries= new HashMap();
			// load variables and containers from preferences into cache
			IEclipsePreferences instancePreferences = JavaCore.getInstancePreferences();
			instancePreferences.addPreferenceChangeListener(listener);

			// only get variable from preferences not set to their default
			try {
				String[] propertyNames = instancePreferences.keys();
				for (int i = 0; i < propertyNames.length; i++) {
					String propertyName = propertyNames[i];
					if (propertyName.startsWith(CP_USERLIBRARY_PREFERENCES_PREFIX)) {
						try {
							String propertyValue = instancePreferences.get(propertyName, null);
							if (propertyValue != null)
								recreatePersistedUserLibraryEntry(propertyName,propertyValue, false, false);
						} catch (JavaModelException e) {
							// won't happen: no rebinding
						}
					}
				}
			} catch (BackingStoreException e) {
				// TODO (frederic) see if it's necessary to report this exception
			}
		}
		return userLibraries;
	}
	
	static void recreatePersistedUserLibraryEntry(String propertyName, String savedString, boolean save, boolean rebind) throws JavaModelException {
		String libName= propertyName.substring(CP_USERLIBRARY_PREFERENCES_PREFIX.length());
		if (savedString == null || savedString.equals(CP_ENTRY_IGNORE)) {
			internalSetUserLibrary(libName, null, save, rebind, null);
		} else {
			try {
				StringReader reader = new StringReader(savedString);
				UserLibrary library= UserLibrary.createFromString(reader);
				internalSetUserLibrary(libName, library, save, rebind, null);
			} catch (IOException e) {
				if (logProblems) {
					Util.log(e, "Exception while retrieving user library '"+ propertyName +"', library will be removed."); //$NON-NLS-1$ //$NON-NLS-2$
				}
				internalSetUserLibrary(libName, null, save, rebind, null);
			}
		}
	}



	static void internalSetUserLibrary(String name, UserLibrary library, boolean save, boolean rebind, IProgressMonitor monitor) throws JavaModelException {
		if (library == null) {
			Object previous= getLibraryMap().remove(name);
			if (previous == null) {
				return; // no change
			}
		} else {
			Object previous= getLibraryMap().put(name, library);
			if (library.equals(previous)) {
				return; // no change
			}
		}
		
		IEclipsePreferences instancePreferences = JavaCore.getInstancePreferences();
		String containerKey = CP_USERLIBRARY_PREFERENCES_PREFIX+name;
		String containerString = CP_ENTRY_IGNORE;
		if (library != null) {
			try {
				containerString= library.serialize();
			} catch (IOException e) {
				// could not encode entry: leave it as CP_ENTRY_IGNORE
			}
		}
		instancePreferences.removePreferenceChangeListener(listener);
		try {
			JavaCore.getDefaultPreferences().put(containerKey, CP_ENTRY_IGNORE); // TODO (frederic) verify if this is really necessary...
			instancePreferences.put(containerKey, containerString);
			if (save) {
				try {
					instancePreferences.flush();
				} catch (BackingStoreException e) {
					// TODO (frederic) see if it's necessary to report this exception
				}
			}
			if (rebind) {
				rebindClasspathEntries(name, library==null, monitor);
			}
			
		} finally {
			instancePreferences.addPreferenceChangeListener(listener);
		}
	}

	private static void rebindClasspathEntries(String name, boolean remove, IProgressMonitor monitor) throws JavaModelException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IJavaProject[] projects= JavaCore.create(root).getJavaProjects();
		IPath containerPath= new Path(JavaCore.USER_LIBRARY_CONTAINER_ID).append(name);
		
		ArrayList affectedProjects= new ArrayList();
		
		for (int i= 0; i < projects.length; i++) {
			IJavaProject project= projects[i];
			IClasspathEntry[] entries= project.getRawClasspath();
			for (int k= 0; k < entries.length; k++) {
				IClasspathEntry curr= entries[k];
				if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					if (containerPath.equals(curr.getPath())) {
						affectedProjects.add(project);
						break;
					}				
				}
			}
		}
		if (!affectedProjects.isEmpty()) {
			IJavaProject[] affected= (IJavaProject[]) affectedProjects.toArray(new IJavaProject[affectedProjects.size()]);
			IClasspathContainer[] containers= new IClasspathContainer[affected.length];
			if (!remove) {
				// Previously, containers array only contained a null value. Then, user library classpath entry was first removed
				// and then added a while after when post change delta event on .classpath file was fired...
				// Unfortunately, in some cases, this event was fired a little bit too late and missed the refresh of Package Explorer
				// (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=61872)
				// So now, instanciate a new user library classpath container instead which allow to refresh its content immediately
				// as there's no classpath entry removal...
				// Note that it works because equals(Object) method is not overridden for UserLibraryClasspathContainer.
				// If it was, the update wouldn't happen while setting classpath container
				// @see javaCore.setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
				UserLibraryClasspathContainer container= new UserLibraryClasspathContainer(name);
				containers[0] = container;
			}
			JavaCore.setClasspathContainer(containerPath, affected, containers, monitor);
		} else {
			if (monitor != null) {
				monitor.done();
			}
		}
	}


	
}
