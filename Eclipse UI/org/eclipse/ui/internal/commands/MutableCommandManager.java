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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.CategoryEvent;
import org.eclipse.ui.commands.CommandEvent;
import org.eclipse.ui.commands.CommandManagerEvent;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.IMutableCommandManager;
import org.eclipse.ui.commands.KeyConfigurationEvent;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public final class MutableCommandManager implements IMutableCommandManager {

	public final static String SEPARATOR = "_"; //$NON-NLS-1$

	static String[] extend(String[] strings) {
		String[] strings2 = new String[strings.length + 1];
		System.arraycopy(strings, 0, strings2, 0, strings.length);
		return strings2;
	}

	static String[] getPath(String string, String separator) {
		if (string == null || separator == null)
			return new String[0];

		List strings = new ArrayList();
		StringBuffer stringBuffer = new StringBuffer();
		string = string.trim();

		if (string.length() > 0) {
			StringTokenizer stringTokenizer =
				new StringTokenizer(string, separator);

			while (stringTokenizer.hasMoreElements()) {
				if (stringBuffer.length() > 0)
					stringBuffer.append(separator);

				stringBuffer.append(
					((String) stringTokenizer.nextElement()).trim());
				strings.add(stringBuffer.toString());
			}
		}

		Collections.reverse(strings);
		strings.add(Util.ZERO_LENGTH_STRING);
		return (String[]) strings.toArray(new String[strings.size()]);
	}

	static boolean isKeyConfigurationDefinitionChildOf(
		String ancestor,
		String id,
		Map keyConfigurationDefinitionsById) {
		Collection visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			KeyConfigurationDefinition keyConfigurationDefinition =
				(
					KeyConfigurationDefinition) keyConfigurationDefinitionsById
						.get(
					id);
			visited.add(id);

			if (keyConfigurationDefinition != null
				&& Util.equals(
					id = keyConfigurationDefinition.getParentId(),
					ancestor))
				return true;
		}

		return false;
	}

	static void validateContextBindingDefinitions(Collection contextBindingDefinitions) {
		Iterator iterator = contextBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			ContextBindingDefinition contextBindingDefinition =
				(ContextBindingDefinition) iterator.next();

			if (contextBindingDefinition.getCommandId() == null)
				iterator.remove();
		}
	}

	static void validateImageBindingDefinitions(Collection imageBindingDefinitions) {
		Iterator iterator = imageBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			ImageBindingDefinition imageBindingDefinition =
				(ImageBindingDefinition) iterator.next();

			if (imageBindingDefinition.getCommandId() == null
				|| imageBindingDefinition.getImageUri() == null)
				iterator.remove();
		}
	}

	static boolean validateKeySequence(KeySequence keySequence) {
		if (keySequence == null)
			return false;

		List keyStrokes = keySequence.getKeyStrokes();
		int size = keyStrokes.size();

		if (size == 0 || size > 4 || !keySequence.isComplete())
			return false;

		return true;
	}

	static void validateKeySequenceBindingDefinitions(Collection keySequenceBindingDefinitions) {
		Iterator iterator = keySequenceBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			KeySequenceBindingDefinition keySequenceBindingDefinition =
				(KeySequenceBindingDefinition) iterator.next();
			String keyConfigurationId =
				keySequenceBindingDefinition.getKeyConfigurationId();
			KeySequence keySequence =
				keySequenceBindingDefinition.getKeySequence();

			if (keyConfigurationId == null
				|| keySequence == null
				|| !validateKeySequence(keySequence))
				iterator.remove();
		}
	}

	// TODO review begin
	private Map handlersByCommandId = new HashMap();
	private Set activeContextIds = new HashSet();
	// TODO does this have any use anymore?
	private String activeKeyConfigurationId = null;
	private String activeLocale = null;
	private String activePlatform = null;
	private Map contextBindingsByCommandId = new HashMap();
	private Map categoriesById = new WeakHashMap();
	private Set categoriesWithListeners = new HashSet();
	private Map categoryDefinitionsById = new HashMap();
	private Map commandDefinitionsById = new HashMap();
	private List commandManagerListeners;
	private ICommandRegistry commandRegistry;
	private Map commandsById = new WeakHashMap();
	private Set commandsWithListeners = new HashSet();
	private Set definedCategoryIds = new HashSet();
	private Set definedCommandIds = new HashSet();
	private Set definedHandlers = new HashSet();
	private Set definedKeyConfigurationIds = new HashSet();
	private Map imageBindingsByCommandId = new HashMap();
	private Map keyConfigurationDefinitionsById = new HashMap();
	private Map keyConfigurationsById = new WeakHashMap();
	private Set keyConfigurationsWithListeners = new HashSet();
	private KeySequenceBindingMachine keySequenceBindingMachine =
		new KeySequenceBindingMachine();
	private Map keySequenceBindingsByCommandId = new HashMap();
	private IMutableCommandRegistry mutableCommandRegistry;
	// TODO review end

	public MutableCommandManager() {
		this(
			new ExtensionCommandRegistry(Platform.getExtensionRegistry()),
			new PreferenceCommandRegistry(
				WorkbenchPlugin.getDefault().getPreferenceStore()));
	}

	public MutableCommandManager(
		ICommandRegistry commandRegistry,
		IMutableCommandRegistry mutableCommandRegistry) {
		if (commandRegistry == null || mutableCommandRegistry == null)
			throw new NullPointerException();

		this.commandRegistry = commandRegistry;
		this.mutableCommandRegistry = mutableCommandRegistry;
		String systemLocale = Locale.getDefault().toString();
		activeLocale =
			systemLocale != null ? systemLocale : Util.ZERO_LENGTH_STRING;
		String systemPlatform = SWT.getPlatform();
		activePlatform =
			systemPlatform != null ? systemPlatform : Util.ZERO_LENGTH_STRING;

		this
			.commandRegistry
			.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(CommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});

		this
			.mutableCommandRegistry
			.addCommandRegistryListener(new ICommandRegistryListener() {
			public void commandRegistryChanged(CommandRegistryEvent commandRegistryEvent) {
				readRegistry();
			}
		});

		readRegistry();
	}

	public void addCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();

		if (commandManagerListeners == null)
			commandManagerListeners = new ArrayList();

		if (!commandManagerListeners.contains(commandManagerListener))
			commandManagerListeners.add(commandManagerListener);
	}

	private void calculateImageBindings() {
		String[] activeLocales = extend(getPath(activeLocale, SEPARATOR));
		String[] activePlatforms = extend(getPath(activePlatform, SEPARATOR));
		// TODO
	}

	private void calculateKeySequenceBindings() {
		List list = new ArrayList(this.activeContextIds);

		// TODO high priority. temporary fix for M3 for automatic inheritance
		// of activities for the specific case of the java editor scope.
		if (list.contains("org.eclipse.jdt.ui.javaEditorScope") //$NON-NLS-1$
			&& !list.contains("org.eclipse.ui.textEditorScope")) //$NON-NLS-1$
			list.add("org.eclipse.ui.textEditorScope"); //$NON-NLS-1$

		// TODO Remove -- debuggging println
		String[] activeContextIds =
			extend((String[]) list.toArray(new String[list.size()]));
		
		String[] activeKeyConfigurationIds =
			extend(getKeyConfigurationIds(activeKeyConfigurationId));
		String[] activeLocales = extend(getPath(activeLocale, SEPARATOR));
		String[] activePlatforms = extend(getPath(activePlatform, SEPARATOR));
		keySequenceBindingMachine.setActiveContextIds(activeContextIds);
		keySequenceBindingMachine.setActiveKeyConfigurationIds(
			activeKeyConfigurationIds);
		keySequenceBindingMachine.setActiveLocales(activeLocales);
		keySequenceBindingMachine.setActivePlatforms(activePlatforms);
		keySequenceBindingsByCommandId =
			keySequenceBindingMachine.getKeySequenceBindingsByCommandId();
	}

	private void fireCommandManagerChanged(CommandManagerEvent commandManagerEvent) {
		if (commandManagerEvent == null)
			throw new NullPointerException();

		if (commandManagerListeners != null)
			for (int i = 0; i < commandManagerListeners.size(); i++)
				(
					(ICommandManagerListener) commandManagerListeners.get(
						i)).commandManagerChanged(
					commandManagerEvent);
	}

	public Map getHandlersByCommandId() {
		return Collections.unmodifiableMap(handlersByCommandId);
	}

	public Set getActiveContextIds() {
		return Collections.unmodifiableSet(activeContextIds);
	}

	public String getActiveKeyConfigurationId() {
		return activeKeyConfigurationId;
	}

	public String getActiveLocale() {
		return activeLocale;
	}

	public String getActivePlatform() {
		return activePlatform;
	}

	public ICategory getCategory(String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();

		Category category = (Category) categoriesById.get(categoryId);

		if (category == null) {
			category = new Category(categoriesWithListeners, categoryId);
			updateCategory(category);
			categoriesById.put(categoryId, category);
		}

		return category;
	}

	public ICommand getCommand(String commandId) {
		if (commandId == null)
			throw new NullPointerException();

		Command command = (Command) commandsById.get(commandId);

		if (command == null) {
			command = new Command(commandsWithListeners, commandId);
			updateCommand(command);
			commandsById.put(commandId, command);
		}

		return command;
	}

	// TODO public only for test cases. remove?
	public ICommandRegistry getCommandRegistry() {
		return commandRegistry;
	}

	public Set getDefinedCategoryIds() {
		return Collections.unmodifiableSet(definedCategoryIds);
	}

	public Set getDefinedCommandIds() {
		return Collections.unmodifiableSet(definedCommandIds);
	}
	
	/**
	 * An accessor for those handlers that have been defined in XML.
	 * @return The handlers defined in XML; never <code>null</code>, but may be
	 * empty.
	 */
	public Set getDefinedHandlers() {
	    return Collections.unmodifiableSet(definedHandlers);
	}

	public Set getDefinedKeyConfigurationIds() {
		return Collections.unmodifiableSet(definedKeyConfigurationIds);
	}

	public IKeyConfiguration getKeyConfiguration(String keyConfigurationId) {
		if (keyConfigurationId == null)
			throw new NullPointerException();

		KeyConfiguration keyConfiguration =
			(KeyConfiguration) keyConfigurationsById.get(keyConfigurationId);

		if (keyConfiguration == null) {
			keyConfiguration =
				new KeyConfiguration(
					keyConfigurationsWithListeners,
					keyConfigurationId);
			updateKeyConfiguration(keyConfiguration);
			keyConfigurationsById.put(keyConfigurationId, keyConfiguration);
		}

		return keyConfiguration;
	}

	String[] getKeyConfigurationIds(String keyConfigurationId) {
		List strings = new ArrayList();

		while (keyConfigurationId != null) {
			strings.add(keyConfigurationId);

			try {
				keyConfigurationId =
					getKeyConfiguration(keyConfigurationId).getParentId();
			} catch (NotDefinedException eNotDefined) {
				return new String[0];
			}
		}

		return (String[]) strings.toArray(new String[strings.size()]);
	}

	IMutableCommandRegistry getMutableCommandRegistry() {
		return mutableCommandRegistry;
	}

	public Map getPartialMatches(KeySequence keySequence) {
		Map map = new HashMap();

		for (Iterator iterator =
			keySequenceBindingMachine
				.getMatchesByKeySequence()
				.entrySet()
				.iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence2 = (KeySequence) entry.getKey();
			Match match = (Match) entry.getValue();

			if (keySequence2.startsWith(keySequence, false))
				map.put(keySequence2, match.getCommandId());
		}

		return Collections.unmodifiableMap(map);
	}

	public String getPerfectMatch(KeySequence keySequence) {
		Match match =
			(Match) keySequenceBindingMachine.getMatchesByKeySequence().get(
				keySequence);
		return match != null ? match.getCommandId() : null;
	}

	private boolean isInContext(Collection contextBindings) {
		if (contextBindings.isEmpty())
			return true;

		Iterator iterator = activeContextIds.iterator();

		while (iterator.hasNext()) {
			String activeContextId = (String) iterator.next();

			if (contextBindings
				.contains(new ContextBinding(activeContextId)));
			return true;
		}

		return false;
	}

	public boolean isPartialMatch(KeySequence keySequence) {
		for (Iterator iterator =
			keySequenceBindingMachine
				.getMatchesByKeySequence()
				.entrySet()
				.iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence2 = (KeySequence) entry.getKey();

			if (keySequence2.startsWith(keySequence, false))
				return true;
		}
		
		return false;
	}

	public boolean isPerfectMatch(KeySequence keySequence) {
		return getPerfectMatch(keySequence) != null;
	}

	private void notifyCategories(Map categoryEventsByCategoryId) {
		for (Iterator iterator =
			categoryEventsByCategoryId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String categoryId = (String) entry.getKey();
			CategoryEvent categoryEvent = (CategoryEvent) entry.getValue();
			Category category = (Category) categoriesById.get(categoryId);

			if (category != null)
				category.fireCategoryChanged(categoryEvent);
		}
	}

	private void notifyCommands(Map commandEventsByCommandId) {
		for (Iterator iterator = commandEventsByCommandId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String commandId = (String) entry.getKey();
			CommandEvent commandEvent = (CommandEvent) entry.getValue();
			Command command = (Command) commandsById.get(commandId);

			if (command != null)
				command.fireCommandChanged(commandEvent);
		}
	}

	private void notifyKeyConfigurations(Map keyConfigurationEventsByKeyConfigurationId) {
		for (Iterator iterator =
			keyConfigurationEventsByKeyConfigurationId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String keyConfigurationId = (String) entry.getKey();
			KeyConfigurationEvent keyConfigurationEvent =
				(KeyConfigurationEvent) entry.getValue();
			KeyConfiguration keyConfiguration =
				(KeyConfiguration) keyConfigurationsById.get(
					keyConfigurationId);

			if (keyConfiguration != null)
				keyConfiguration.fireKeyConfigurationChanged(
					keyConfigurationEvent);
		}
	}

	private void readRegistry() {
		Collection categoryDefinitions = new ArrayList();
		categoryDefinitions.addAll(commandRegistry.getCategoryDefinitions());
		categoryDefinitions.addAll(
			mutableCommandRegistry.getCategoryDefinitions());
		Map categoryDefinitionsById =
			new HashMap(
				CategoryDefinition.categoryDefinitionsById(
					categoryDefinitions,
					false));
		
		definedHandlers.addAll(commandRegistry.getHandlers());

		for (Iterator iterator = categoryDefinitionsById.values().iterator();
			iterator.hasNext();
			) {
			CategoryDefinition categoryDefinition =
				(CategoryDefinition) iterator.next();
			String name = categoryDefinition.getName();

			if (name == null || name.length() == 0)
				iterator.remove();
		}

		Collection commandDefinitions = new ArrayList();
		commandDefinitions.addAll(commandRegistry.getCommandDefinitions());
		commandDefinitions.addAll(
			mutableCommandRegistry.getCommandDefinitions());
		Map commandDefinitionsById =
			new HashMap(
				CommandDefinition.commandDefinitionsById(
					commandDefinitions,
					false));

		for (Iterator iterator = commandDefinitionsById.values().iterator();
			iterator.hasNext();
			) {
			CommandDefinition commandDefinition =
				(CommandDefinition) iterator.next();
			String name = commandDefinition.getName();

			if (name == null || name.length() == 0)
				iterator.remove();
		}

		Collection keyConfigurationDefinitions = new ArrayList();
		keyConfigurationDefinitions.addAll(
			commandRegistry.getKeyConfigurationDefinitions());
		keyConfigurationDefinitions.addAll(
			mutableCommandRegistry.getKeyConfigurationDefinitions());
		Map keyConfigurationDefinitionsById =
			new HashMap(
				KeyConfigurationDefinition.keyConfigurationDefinitionsById(
					keyConfigurationDefinitions,
					false));

		for (Iterator iterator =
			keyConfigurationDefinitionsById.values().iterator();
			iterator.hasNext();
			) {
			KeyConfigurationDefinition keyConfigurationDefinition =
				(KeyConfigurationDefinition) iterator.next();
			String name = keyConfigurationDefinition.getName();

			if (name == null || name.length() == 0)
				iterator.remove();
		}

		// TODO should we really filter these out?
		for (Iterator iterator = commandDefinitionsById.values().iterator();
			iterator.hasNext();
			) {
			CommandDefinition commandDefinition =
				(CommandDefinition) iterator.next();
			String categoryId = commandDefinition.getCategoryId();

			if (categoryId != null
				&& !categoryDefinitionsById.containsKey(categoryId))
				iterator.remove();
		}

		for (Iterator iterator =
			keyConfigurationDefinitionsById.keySet().iterator();
			iterator.hasNext();
			)
			if (!isKeyConfigurationDefinitionChildOf(null,
				(String) iterator.next(),
				keyConfigurationDefinitionsById))
				iterator.remove();

		// TODO should the active key configuration change if a call to
		// setContextKeyConfigurationId was explicitly made already?
		List activeKeyConfigurationDefinitions = new ArrayList();
		activeKeyConfigurationDefinitions.addAll(
			commandRegistry.getActiveKeyConfigurationDefinitions());
		activeKeyConfigurationDefinitions.addAll(
			mutableCommandRegistry.getActiveKeyConfigurationDefinitions());
		String activeKeyConfigurationId = null;

		if (!activeKeyConfigurationDefinitions.isEmpty()) {
			ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition =
				(
					ActiveKeyConfigurationDefinition) activeKeyConfigurationDefinitions
						.get(
					activeKeyConfigurationDefinitions.size() - 1);
			activeKeyConfigurationId =
				activeKeyConfigurationDefinition.getKeyConfigurationId();

			if (activeKeyConfigurationId != null
				&& !keyConfigurationDefinitionsById.containsKey(
					activeKeyConfigurationId))
				activeKeyConfigurationId = null;
		}

		// TODO - if null, pick the first key configuration in sorted order by
		// id?
		if (activeKeyConfigurationId == null
			&& !keyConfigurationDefinitionsById.isEmpty()) {
			SortedSet sortedSet =
				new TreeSet(keyConfigurationDefinitionsById.keySet());
			activeKeyConfigurationId = (String) sortedSet.first();
		}

		this.categoryDefinitionsById = categoryDefinitionsById;
		this.commandDefinitionsById = commandDefinitionsById;
		this.keyConfigurationDefinitionsById = keyConfigurationDefinitionsById;
		boolean activeKeyConfigurationIdChanged = false;

		if (!Util
			.equals(this.activeKeyConfigurationId, activeKeyConfigurationId)) {
			this.activeKeyConfigurationId = activeKeyConfigurationId;
			activeKeyConfigurationIdChanged = true;
		}

		boolean definedCategoryIdsChanged = false;
		Set definedCategoryIds = new HashSet(categoryDefinitionsById.keySet());

		if (!definedCategoryIds.equals(this.definedCategoryIds)) {
			this.definedCategoryIds = definedCategoryIds;
			definedCategoryIdsChanged = true;
		}

		boolean definedCommandIdsChanged = false;
		Set definedCommandIds = new HashSet(commandDefinitionsById.keySet());

		if (!definedCommandIds.equals(this.definedCommandIds)) {
			this.definedCommandIds = definedCommandIds;
			definedCommandIdsChanged = true;
		}

		boolean definedKeyConfigurationIdsChanged = false;
		Set definedKeyConfigurationIds =
			new HashSet(keyConfigurationDefinitionsById.keySet());

		if (!definedKeyConfigurationIds
			.equals(this.definedKeyConfigurationIds)) {
			this.definedKeyConfigurationIds = definedKeyConfigurationIds;
			definedKeyConfigurationIdsChanged = true;
		}

		List contextBindingDefinitions = new ArrayList();
		contextBindingDefinitions.addAll(
			commandRegistry.getContextBindingDefinitions());
		contextBindingDefinitions.addAll(
			mutableCommandRegistry.getContextBindingDefinitions());
		validateContextBindingDefinitions(contextBindingDefinitions);
		Map contextBindingsByCommandId = new HashMap();

		for (Iterator iterator = contextBindingDefinitions.iterator();
			iterator.hasNext();
			) {
			ContextBindingDefinition contextBindingDefinition =
				(ContextBindingDefinition) iterator.next();
			String contextId = contextBindingDefinition.getContextId();
			String commandId = contextBindingDefinition.getCommandId();
			SortedSet sortedSet =
				(SortedSet) contextBindingsByCommandId.get(commandId);

			if (sortedSet == null) {
				sortedSet = new TreeSet();
				contextBindingsByCommandId.put(commandId, sortedSet);
			}

			sortedSet.add(new ContextBinding(contextId));
		}

		this.contextBindingsByCommandId = contextBindingsByCommandId;
		List commandRegistryImageBindingDefinitions =
			new ArrayList(commandRegistry.getImageBindingDefinitions());
		validateImageBindingDefinitions(commandRegistryImageBindingDefinitions);
		List mutableCommandRegistryImageBindingDefinitions =
			new ArrayList(mutableCommandRegistry.getImageBindingDefinitions());
		validateImageBindingDefinitions(mutableCommandRegistryImageBindingDefinitions);
		// TODO prepare imageBindingMachine with ImageBindingDefinitions
		calculateImageBindings();
		List commandRegistryKeySequenceBindingDefinitions =
			new ArrayList(commandRegistry.getKeySequenceBindingDefinitions());
		validateKeySequenceBindingDefinitions(commandRegistryKeySequenceBindingDefinitions);
		List mutableCommandRegistryKeySequenceBindingDefinitions =
			new ArrayList(
				mutableCommandRegistry.getKeySequenceBindingDefinitions());
		validateKeySequenceBindingDefinitions(mutableCommandRegistryKeySequenceBindingDefinitions);
		keySequenceBindingMachine.setKeySequenceBindings0(
			mutableCommandRegistryKeySequenceBindingDefinitions);
		keySequenceBindingMachine.setKeySequenceBindings1(
			commandRegistryKeySequenceBindingDefinitions);
		calculateKeySequenceBindings();
		Map categoryEventsByCategoryId =
			updateCategories(categoriesById.keySet());
		Map commandEventsByCommandId = updateCommands(commandsById.keySet());
		Map keyConfigurationEventsByKeyConfigurationId =
			updateKeyConfigurations(keyConfigurationsById.keySet());

		if (activeKeyConfigurationIdChanged
			|| definedCategoryIdsChanged
			|| definedCommandIdsChanged
			|| definedKeyConfigurationIdsChanged)
			fireCommandManagerChanged(
				new CommandManagerEvent(
					this,
					false,
					activeKeyConfigurationIdChanged,
					false,
					false,
					definedCategoryIdsChanged,
					definedCommandIdsChanged,
					definedKeyConfigurationIdsChanged,
					false));

		if (categoryEventsByCategoryId != null)
			notifyCategories(categoryEventsByCategoryId);

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);

		if (keyConfigurationEventsByKeyConfigurationId != null)
			notifyKeyConfigurations(keyConfigurationEventsByKeyConfigurationId);
	}

	public void removeCommandManagerListener(ICommandManagerListener commandManagerListener) {
		if (commandManagerListener == null)
			throw new NullPointerException();

		if (commandManagerListeners != null)
			commandManagerListeners.remove(commandManagerListener);
	}

	public void setHandlersByCommandId(Map handlersByCommandId) {
		handlersByCommandId = Util.safeCopy(handlersByCommandId, String.class, IHandler.class, false, true);
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!Util.equals(handlersByCommandId, this.handlersByCommandId)) {
			this.handlersByCommandId = handlersByCommandId;
			commandManagerChanged = true;
			commandEventsByCommandId = updateCommands(commandsById.keySet());
		}

		if (commandManagerChanged)
			fireCommandManagerChanged(
				new CommandManagerEvent(
					this,
					false,
					false,
					false,
					false,
					false,
					false,
					false,
					true));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);
	}

	public void setActiveContextIds(Set activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.activeContextIds.equals(activeContextIds)) {
			this.activeContextIds = activeContextIds;
			commandManagerChanged = true;
			calculateKeySequenceBindings();
			commandEventsByCommandId = updateCommands(commandsById.keySet());
		}

		if (commandManagerChanged)
			fireCommandManagerChanged(
				new CommandManagerEvent(
					this,
					true,
					false,
					false,
					false,
					false,
					false,
					false,
					false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);
	}

	public void setActiveKeyConfigurationId(String activeKeyConfigurationId) {
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;
		Map keyConfigurationEventsByKeyConfigurationId = null;

		if (!Util
			.equals(this.activeKeyConfigurationId, activeKeyConfigurationId)) {
			this.activeKeyConfigurationId = activeKeyConfigurationId;
			commandManagerChanged = true;
			calculateKeySequenceBindings();
			commandEventsByCommandId = updateCommands(commandsById.keySet());
			keyConfigurationEventsByKeyConfigurationId =
				updateKeyConfigurations(keyConfigurationsById.keySet());
		}

		if (commandManagerChanged)
			fireCommandManagerChanged(
				new CommandManagerEvent(
					this,
					false,
					true,
					false,
					false,
					false,
					false,
					false,
					false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);

		if (keyConfigurationEventsByKeyConfigurationId != null)
			notifyKeyConfigurations(keyConfigurationEventsByKeyConfigurationId);
	}

	public void setActiveLocale(String activeLocale) {
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!Util.equals(this.activeLocale, activeLocale)) {
			this.activeLocale = activeLocale;
			commandManagerChanged = true;
			calculateImageBindings();
			calculateKeySequenceBindings();
			commandEventsByCommandId = updateCommands(commandsById.keySet());
		}

		if (commandManagerChanged)
			fireCommandManagerChanged(
				new CommandManagerEvent(
					this,
					false,
					false,
					true,
					false,
					false,
					false,
					false,
					false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);
	}

	public void setActivePlatform(String activePlatform) {
		boolean commandManagerChanged = false;
		Map commandEventsByCommandId = null;

		if (!Util.equals(this.activePlatform, activePlatform)) {
			this.activePlatform = activePlatform;
			commandManagerChanged = true;
			calculateImageBindings();
			calculateKeySequenceBindings();
			commandEventsByCommandId = updateCommands(commandsById.keySet());
		}

		if (commandManagerChanged)
			fireCommandManagerChanged(
				new CommandManagerEvent(
					this,
					false,
					false,
					false,
					true,
					false,
					false,
					false,
					false));

		if (commandEventsByCommandId != null)
			notifyCommands(commandEventsByCommandId);
	}

	private Map updateCategories(Collection categoryIds) {
		Map categoryEventsByCategoryId = new TreeMap();

		for (Iterator iterator = categoryIds.iterator(); iterator.hasNext();) {
			String categoryId = (String) iterator.next();
			Category category = (Category) categoriesById.get(categoryId);

			if (category != null) {
				CategoryEvent categoryEvent = updateCategory(category);

				if (categoryEvent != null)
					categoryEventsByCategoryId.put(categoryId, categoryEvent);
			}
		}

		return categoryEventsByCategoryId;
	}

	private CategoryEvent updateCategory(Category category) {
		CategoryDefinition categoryDefinition =
			(CategoryDefinition) categoryDefinitionsById.get(category.getId());
		boolean definedChanged =
			category.setDefined(categoryDefinition != null);
		boolean descriptionChanged =
			category.setDescription(
				categoryDefinition != null
					? categoryDefinition.getDescription()
					: null);
		boolean nameChanged =
			category.setName(
				categoryDefinition != null
					? categoryDefinition.getName()
					: null);

		if (definedChanged || descriptionChanged || nameChanged)
			return new CategoryEvent(
				category,
				definedChanged,
				descriptionChanged,
				nameChanged);
		else
			return null;
	}

	private CommandEvent updateCommand(Command command) {
		SortedSet contextBindings =
			(SortedSet) contextBindingsByCommandId.get(command.getId());
		// TODO list to sortedset in api?
		boolean contextBindingsChanged =
			command.setContextBindings(
				contextBindings != null
					? new ArrayList(contextBindings)
					: Collections.EMPTY_LIST);
		CommandDefinition commandDefinition =
			(CommandDefinition) commandDefinitionsById.get(command.getId());
		boolean categoryIdChanged =
			command.setCategoryId(
				commandDefinition != null
					? commandDefinition.getCategoryId()
					: null);
		boolean definedChanged = command.setDefined(commandDefinition != null);
		boolean descriptionChanged =
			command.setDescription(
				commandDefinition != null
					? commandDefinition.getDescription()
					: null);
		SortedSet imageBindings =
			(SortedSet) imageBindingsByCommandId.get(command.getId());				
		IHandler handler = (IHandler) handlersByCommandId.get(command.getId());
		boolean handlerChanged = command.setHandler(handler);
		// TODO list to sortedset in api?
		boolean imageBindingsChanged =
			command.setImageBindings(
				imageBindings != null
					? new ArrayList(imageBindings)
					: Collections.EMPTY_LIST);
		SortedSet keySequenceBindings =
			(SortedSet) keySequenceBindingsByCommandId.get(command.getId());
		// TODO list to sortedset in api?
		boolean keySequenceBindingsChanged =
			command.setKeySequenceBindings(
				keySequenceBindings != null
					? new ArrayList(keySequenceBindings)
					: Collections.EMPTY_LIST);
		boolean nameChanged =
			command.setName(
				commandDefinition != null ? commandDefinition.getName() : null);

		if (contextBindingsChanged
			|| categoryIdChanged
			|| definedChanged
			|| descriptionChanged
			|| imageBindingsChanged
			|| keySequenceBindingsChanged
			|| nameChanged)
			return new CommandEvent(
				command,
				Collections.EMPTY_SET, // TODO
				contextBindingsChanged,
				categoryIdChanged,
				false, // TODO 
				definedChanged,
				descriptionChanged,
				imageBindingsChanged,
				keySequenceBindingsChanged,
				nameChanged);
		else
			return null;
	}

	private Map updateCommands(Collection commandIds) {
		Map commandEventsByCommandId = new TreeMap();

		for (Iterator iterator = commandIds.iterator(); iterator.hasNext();) {
			String commandId = (String) iterator.next();
			Command command = (Command) commandsById.get(commandId);

			if (command != null) {
				CommandEvent commandEvent = updateCommand(command);

				if (commandEvent != null)
					commandEventsByCommandId.put(commandId, commandEvent);
			}
		}

		return commandEventsByCommandId;
	}

	private KeyConfigurationEvent updateKeyConfiguration(KeyConfiguration keyConfiguration) {
		boolean activeChanged =
			keyConfiguration.setActive(
				Util.equals(
					activeKeyConfigurationId,
					keyConfiguration.getId()));
		KeyConfigurationDefinition keyConfigurationDefinition =
			(KeyConfigurationDefinition) keyConfigurationDefinitionsById.get(
				keyConfiguration.getId());
		boolean definedChanged =
			keyConfiguration.setDefined(keyConfigurationDefinition != null);
		boolean descriptionChanged =
			keyConfiguration.setDescription(
				keyConfigurationDefinition != null
					? keyConfigurationDefinition.getDescription()
					: null);
		boolean nameChanged =
			keyConfiguration.setName(
				keyConfigurationDefinition != null
					? keyConfigurationDefinition.getName()
					: null);
		boolean parentIdChanged =
			keyConfiguration.setParentId(
				keyConfigurationDefinition != null
					? keyConfigurationDefinition.getParentId()
					: null);

		if (activeChanged
			|| definedChanged
			|| descriptionChanged
			|| nameChanged
			|| parentIdChanged)
			return new KeyConfigurationEvent(
				keyConfiguration,
				activeChanged,
				definedChanged,
				descriptionChanged,
				nameChanged,
				parentIdChanged);
		else
			return null;
	}

	private Map updateKeyConfigurations(Collection keyConfigurationIds) {
		Map keyConfigurationEventsByKeyConfigurationId = new TreeMap();

		for (Iterator iterator = keyConfigurationIds.iterator();
			iterator.hasNext();
			) {
			String keyConfigurationId = (String) iterator.next();
			KeyConfiguration keyConfiguration =
				(KeyConfiguration) keyConfigurationsById.get(
					keyConfigurationId);

			if (keyConfiguration != null) {
				KeyConfigurationEvent keyConfigurationEvent =
					updateKeyConfiguration(keyConfiguration);

				if (keyConfigurationEvent != null)
					keyConfigurationEventsByKeyConfigurationId.put(
						keyConfigurationId,
						keyConfigurationEvent);
			}
		}

		return keyConfigurationEventsByKeyConfigurationId;
	}
}
