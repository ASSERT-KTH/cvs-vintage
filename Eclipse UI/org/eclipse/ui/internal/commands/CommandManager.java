/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICategoryHandle;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandDelegate;
import org.eclipse.ui.commands.ICommandHandle;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.IKeyConfigurationHandle;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

public final class CommandManager implements ICommandManager {

	private static CommandManager instance;

	public static CommandManager getInstance() {
		if (instance == null)
			instance = new CommandManager();
			
		return instance;
	}

	private SortedMap categoriesById = new TreeMap();
	private SortedMap categoryHandlesById = new TreeMap();
	private SortedMap commandDelegatesById = new TreeMap();	
	private SortedMap commandHandlesById = new TreeMap();
	private ICommandManagerEvent commandManagerEvent;
	private List commandManagerListeners;
	private SortedMap commandsById = new TreeMap();
	private SortedMap keyConfigurationHandlesById = new TreeMap();
	private SortedMap keyConfigurationsById = new TreeMap();	
	private IRegistry pluginRegistry;
	private IMutableRegistry preferenceRegistry;

	private CommandManager() {
		super();
		loadPluginRegistry();
		loadPreferenceRegistry();
		updateFromRegistries();
	}

	public void addCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners == null)
			commandManagerListeners = new ArrayList();
		
		if (!commandManagerListeners.contains(commandManagerListener))
			commandManagerListeners.add(commandManagerListener);
	}

	public SortedMap getCategoriesById() {
		return Collections.unmodifiableSortedMap(categoriesById);
	}

	public ICategoryHandle getCategoryHandle(String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();
			
		ICategoryHandle categoryHandle = (ICategoryHandle) categoryHandlesById.get(categoryId);
		
		if (categoryHandle == null) {
			categoryHandle = new CategoryHandle(categoryId);
			categoryHandlesById.put(categoryId, categoryHandle);
		}
		
		return categoryHandle;
	}

	public SortedMap getCommandDelegatesById() {
		return Collections.unmodifiableSortedMap(commandDelegatesById);
	}

	public ICommandHandle getCommandHandle(String commandId) {
		if (commandId == null)
			throw new NullPointerException();
			
		ICommandHandle commandHandle = (ICommandHandle) commandHandlesById.get(commandId);
		
		if (commandHandle == null) {
			commandHandle = new CommandHandle(commandId);
			commandHandlesById.put(commandId, commandHandle);
		}
		
		return commandHandle;
	}
	
	public SortedMap getCommandsById() {
		return Collections.unmodifiableSortedMap(commandsById);
	}	

	public IKeyConfigurationHandle getKeyConfigurationHandle(String keyConfigurationId) {
		if (keyConfigurationId == null)
			throw new NullPointerException();
			
		IKeyConfigurationHandle keyConfigurationHandle = (IKeyConfigurationHandle) keyConfigurationHandlesById.get(keyConfigurationId);
		
		if (keyConfigurationHandle == null) {
			keyConfigurationHandle = new KeyConfigurationHandle(keyConfigurationId);
			keyConfigurationHandlesById.put(keyConfigurationId, keyConfigurationHandle);
		}
		
		return keyConfigurationHandle;
	}

	public SortedMap getKeyConfigurationsById() {
		return Collections.unmodifiableSortedMap(keyConfigurationsById);
	}

	public void removeCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();
			
		if (commandManagerListeners != null) {
			commandManagerListeners.remove(commandManagerListener);
			
			if (commandManagerListeners.isEmpty())
				commandManagerListeners = null;
		}
	}

	public void setCommandDelegatesById(SortedMap commandDelegatesById)
		throws IllegalArgumentException {
		commandDelegatesById = Util.safeCopy(commandDelegatesById, String.class, ICommandDelegate.class);	
	
		if (!Util.equals(commandDelegatesById, this.commandDelegatesById)) {	
			this.commandDelegatesById = commandDelegatesById;	
			fireCommandManagerChanged();
		}
	}

	private void fireCommandManagerChanged() {
		if (commandManagerListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(commandManagerListeners).iterator();	
			
			if (iterator.hasNext()) {
				if (commandManagerEvent == null)
					commandManagerEvent = new CommandManagerEvent(this);
				
				while (iterator.hasNext())	
					((ICommandManagerListener) iterator.next()).commandManagerChanged(commandManagerEvent);
			}							
		}			
	}

	private void loadPluginRegistry() {
		if (pluginRegistry == null)
			pluginRegistry = new PluginRegistry(Platform.getPluginRegistry());
		
		try {
			pluginRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}
	
	private void loadPreferenceRegistry() {
		if (preferenceRegistry == null)
			preferenceRegistry = new PreferenceRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());
		
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}

	private void updateFromRegistries() {	
		List categories = new ArrayList();
		categories.addAll(pluginRegistry.getCategories());
		categories.addAll(preferenceRegistry.getCategories());
		SortedMap categoriesById = Category.sortedMapById(categories);			
		SortedSet categoryChanges = new TreeSet();
		Util.diff(categoriesById, this.categoriesById, categoryChanges, categoryChanges, categoryChanges);
		List commands = new ArrayList();
		commands.addAll(pluginRegistry.getCommands());
		commands.addAll(preferenceRegistry.getCommands());
		SortedMap commandsById = Command.sortedMapById(commands);			
		SortedSet commandChanges = new TreeSet();
		Util.diff(commandsById, this.commandsById, commandChanges, commandChanges, commandChanges);
		List keyConfigurations = new ArrayList();
		keyConfigurations.addAll(pluginRegistry.getKeyConfigurations());
		keyConfigurations.addAll(preferenceRegistry.getKeyConfigurations());
		SortedMap keyConfigurationsById = KeyConfiguration.sortedMapById(keyConfigurations);			
		SortedSet keyConfigurationChanges = new TreeSet();
		Util.diff(keyConfigurationsById, this.keyConfigurationsById, keyConfigurationChanges, keyConfigurationChanges, keyConfigurationChanges);
		boolean commandManagerChanged = false;
				
		if (!categoryChanges.isEmpty()) {
			this.categoriesById = categoriesById;
			commandManagerChanged = true;			
		}

		if (!commandChanges.isEmpty()) {
			this.commandsById = commandsById;		
			commandManagerChanged = true;			
		}

		if (!keyConfigurationChanges.isEmpty()) {
			this.keyConfigurationsById = keyConfigurationsById;		
			commandManagerChanged = true;
		}

		if (commandManagerChanged)
			fireCommandManagerChanged();

		if (!categoryChanges.isEmpty()) {
			Iterator iterator = categoryChanges.iterator();
		
			while (iterator.hasNext()) {
				String categoryId = (String) iterator.next();					
				CategoryHandle categoryHandle = (CategoryHandle) categoryHandlesById.get(categoryId);
			
				if (categoryHandle != null) {			
					if (categoriesById.containsKey(categoryId))
						categoryHandle.define((ICategory) categoriesById.get(categoryId));
					else
						categoryHandle.undefine();
				}
			}			
		}

		if (!commandChanges.isEmpty()) {
			Iterator iterator = commandChanges.iterator();
		
			while (iterator.hasNext()) {
				String commandId = (String) iterator.next();					
				CommandHandle commandHandle = (CommandHandle) commandHandlesById.get(commandId);
			
				if (commandHandle != null) {			
					if (commandsById.containsKey(commandId))
						commandHandle.define((ICommand) commandsById.get(commandId));
					else
						commandHandle.undefine();
				}
			}			
		}
		
		if (!keyConfigurationChanges.isEmpty()) {
			Iterator iterator = keyConfigurationChanges.iterator();
		
			while (iterator.hasNext()) {
				String keyConfigurationId = (String) iterator.next();					
				KeyConfigurationHandle keyConfigurationHandle = (KeyConfigurationHandle) keyConfigurationHandlesById.get(keyConfigurationId);
			
				if (keyConfigurationHandle != null) {			
					if (keyConfigurationsById.containsKey(keyConfigurationId))
						keyConfigurationHandle.define((IKeyConfiguration) keyConfigurationsById.get(keyConfigurationId));
					else
						keyConfigurationHandle.undefine();
				}
			}			
		}		
	}
}
