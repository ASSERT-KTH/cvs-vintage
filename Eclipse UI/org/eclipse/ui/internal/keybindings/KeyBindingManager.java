/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.Accelerator;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
import org.eclipse.ui.internal.registry.AcceleratorScope;
import org.eclipse.ui.internal.registry.AcceleratorSet;

public final class KeyBindingManager {

	private final static String KEY_SEQUENCE_SEPARATOR = ", "; //$NON-NLS-1$
	private final static String KEY_STROKE_SEPARATOR = " "; //$NON-NLS-1$
	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static String OR_SEPARATOR = "||"; //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	private static KeyBindingManager instance;

	public static KeyBindingManager getInstance() {
		if (instance == null)
			instance = new KeyBindingManager();
			
		return instance;	
	}
	
	private static SortedMap loadConfigurationMap() {
		AcceleratorRegistry acceleratorRegistry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();	
		return buildConfigurationMap(acceleratorRegistry.getAcceleratorConfigurations());	
	}
	
	private static SortedMap buildConfigurationMap(Map acceleratorConfigurations) {
		SortedMap configurations = new TreeMap();
		Iterator iterator = acceleratorConfigurations.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForConfigurationId(id, acceleratorConfigurations);
			
				if (path != null)
					configurations.put(id, path);
			}			
		}

		return configurations;		
	}

	private static Path pathForConfigurationId(String id, Map acceleratorConfigurations) {
		Path path = null;

		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				AcceleratorConfiguration acceleratorConfiguration = (AcceleratorConfiguration) acceleratorConfigurations.get(id);
				
				if (acceleratorConfiguration == null)
					return null;
							
				pathItems.add(0, PathItem.create(id));
				id = acceleratorConfiguration.getParentId();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;			
	}	

	private static SortedMap loadScopeMap() {
		AcceleratorRegistry acceleratorRegistry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();	
		return buildScopeMap(acceleratorRegistry.getAcceleratorScopes());	
	}
	
	private static SortedMap buildScopeMap(Map acceleratorScopes) {
		SortedMap scopes = new TreeMap();
		Iterator iterator = acceleratorScopes.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {
				Path path = pathForScopeId(id, acceleratorScopes);
			
				if (path != null)
					scopes.put(id, path);
			}
		}

		return scopes;		
	}

	private static Path pathForScopeId(String id, Map acceleratorScopes) {
		Path path = null;
		
		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				AcceleratorScope acceleratorScope = (AcceleratorScope) acceleratorScopes.get(id);
				
				if (acceleratorScope == null)
					return null;
							
				pathItems.add(0, PathItem.create(id));
				id = acceleratorScope.getParentId();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;	
	}	

	static Path pathForLocale(String locale) {
		Path path = null;

		if (locale != null) {
			List pathItems = new ArrayList();				
			locale = locale.trim();
			
			if (locale.length() > 0) {
				StringTokenizer st = new StringTokenizer(locale, LOCALE_SEPARATOR);
						
				while (st.hasMoreElements()) {
					String value = ((String) st.nextElement()).trim();
					
					if (value != null)
						pathItems.add(PathItem.create(value));
				}
			}

			path = Path.create(pathItems);
		}
			
		return path;		
	}

	private static Path systemLocale() {
		java.util.Locale locale = java.util.Locale.getDefault();
		return locale != null ? pathForLocale(locale.toString()) : null;
	}

	static Path pathForPlatform(String platform) {
		Path path = null;

		if (platform != null) {
			List pathItems = new ArrayList();				
			platform = platform.trim();
			
			if (platform.length() > 0)
				pathItems.add(PathItem.create(platform));

			path = Path.create(pathItems);
		}
			
		return path;		
	}

	private static Path systemPlatform() {
		return pathForPlatform(SWT.getPlatform());
	}
	
	private static List loadKeyBindings() {
		List keyBindings = new ArrayList();
		AcceleratorRegistry acceleratorRegistry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();	
		List acceleratorSets = acceleratorRegistry.getAcceleratorSets();
		Iterator iterator = acceleratorSets.iterator();
		
		while (iterator.hasNext()) {
			AcceleratorSet acceleratorSet = (AcceleratorSet) iterator.next();
			String configuration = acceleratorSet.getAcceleratorConfigurationId();
			
			if (configuration == null)
				configuration = ZERO_LENGTH_STRING;
			
			String scope = acceleratorSet.getAcceleratorScopeId();

			if (scope == null)
				scope = ZERO_LENGTH_STRING;

			String plugin = acceleratorSet.getPluginId();			
			List accelerators = acceleratorSet.getAccelerators();
			Iterator iterator2 = accelerators.iterator();
			
			while (iterator2.hasNext()) {
				Accelerator accelerator = (Accelerator) iterator2.next();				
				List keySequences = parseKeySequences(accelerator.getKey());
			
				if (keySequences == null)
					continue;

				String action = accelerator.getId();									
				String locale = accelerator.getLocale();
			
				if (locale == null)
					locale = ZERO_LENGTH_STRING;
				
				String platform = accelerator.getPlatform();				

				if (platform == null)
					platform = ZERO_LENGTH_STRING;
				
				Iterator iterator3 = keySequences.iterator();
				
				while (iterator3.hasNext()) {
					KeySequence keySequence = (KeySequence) iterator3.next();
							
					if (keySequence.getKeyStrokes().size() >= 1)						
						keyBindings.add(KeyBinding.create(keySequence, action, configuration, locale, platform, plugin, scope));
				}
			}			
		}
		
		return keyBindings;
	}
	
	private static List parseKeySequences(String keys) {
		List keySequences = null;
		
		if (keys != null) {
			keySequences = new ArrayList();
			StringTokenizer orTokenizer = new StringTokenizer(keys, OR_SEPARATOR); 
			
			while (orTokenizer.hasMoreTokens()) {
				List keyStrokes = new ArrayList();
				StringTokenizer spaceTokenizer = new StringTokenizer(orTokenizer.nextToken());
				
				while (spaceTokenizer.hasMoreTokens()) {
					int accelerator = org.eclipse.jface.action.Action.convertAccelerator(spaceTokenizer.nextToken());
					
					if (accelerator != 0)
						keyStrokes.add(KeyStroke.create(accelerator));
				}
				
				if (keyStrokes.size() >= 1)
					keySequences.add(KeySequence.create(keyStrokes));		
			}
		}

		return keySequences;
	}

	private static List readKeyBindingsFromReader(Reader reader)
		throws IOException {
		try {
			XMLMemento xmlMemento = XMLMemento.createReadRoot(reader);
			return readKeyBindings(xmlMemento);
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();	
		}
	}

	private static void writeKeyBindingsToWriter(Writer writer, String root, List keyBindings)
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(root);
		writeKeyBindings(xmlMemento, keyBindings);
		xmlMemento.save(writer);
	}

	private static List readKeyBindings(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			
		
		IMemento[] mementos = memento.getChildren(KeyBinding.ELEMENT);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List keyBindings = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			keyBindings.add(KeyBinding.read(mementos[i]));
		
		return keyBindings;		
	}

	private static void writeKeyBindings(IMemento memento, List keyBindings)
		throws IllegalArgumentException {
		if (memento == null || keyBindings == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext())
			((KeyBinding) iterator.next()).write(memento.createChild(KeyBinding.ELEMENT)); 
	}

	private static void filterAction(List keyBindings, Set actions, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !actions.contains(keyBinding.getAction()))
				iterator.remove();
		}
	}

	private static void filterConfiguration(List keyBindings, Set configurations, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !configurations.contains(keyBinding.getConfiguration()))
				iterator.remove();
		}
	}

	private static void filterLocale(List keyBindings, Set locales, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !locales.contains(keyBinding.getLocale()))
				iterator.remove();
		}
	}

	private static void filterPlatform(List keyBindings, Set platforms, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !platforms.contains(keyBinding.getPlatform()))
				iterator.remove();
		}
	}

	private static void filterPlugin(List keyBindings, Set plugins, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !plugins.contains(keyBinding.getPlugin()))
				iterator.remove();
		}
	}

	private static void filterScope(List keyBindings, Set scopes, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !scopes.contains(keyBinding.getScope()))
				iterator.remove();
		}
	}

	private static SortedMap buildTree(List keyBindings, SortedMap configurationMap, SortedMap scopeMap) {
		SortedMap tree = new TreeMap();
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext())
			Node.addToTree(tree, (KeyBinding) iterator.next(), configurationMap, scopeMap);

		return tree;
	}			

	private SortedMap configurationMap;
	private SortedMap scopeMap;		
	private List initialKeyBindings;
	//private List customKeyBindings;	
	private SortedMap tree;
		
	private Path configuration = Path.create();
	private Path locale = systemLocale();
	private Path platform = systemPlatform();
	private Path[] scopes = new Path[] { Path.create() };
	
	private KeySequence mode = KeySequence.create();
	private SortedMap actionKeySequenceSetMap;
	private SortedMap keySequenceActionMap;
	private SortedMap actionKeySequenceSetMapForMode;
	private SortedMap keySequenceActionMapForMode;
	private SortedSet keyStrokeSetForMode;

	private KeyBindingManager() {
		super();
		configurationMap = Collections.unmodifiableSortedMap(loadConfigurationMap());
		scopeMap = Collections.unmodifiableSortedMap(loadScopeMap());
		initialKeyBindings = Collections.unmodifiableList(loadKeyBindings());
		
		tree = buildTree(initialKeyBindings, configurationMap, scopeMap);	
		solve();
				
		try {
			IPath path = WorkbenchPlugin.getDefault().getStateLocation();

			IPath path0 = path.append("initialKeyBindings (from list).xml");
			FileWriter fileWriter0 = new FileWriter(path0.toFile());
			writeKeyBindingsToWriter(fileWriter0, "initialkeybindings", initialKeyBindings);
			fileWriter0.close();

			IPath path1 = path.append("initialKeyBindings (from tree).xml");
			FileWriter fileWriter1 = new FileWriter(path1.toFile());
			writeKeyBindingsToWriter(fileWriter1, "initialkeybindings", Node.toBindings(tree));
			fileWriter1.close();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}
	}
	
	public Path getConfigurationForId(String id) {
		return (Path) configurationMap.get(id);	
	}

	public Path getScopeForId(String id) {
		return (Path) scopeMap.get(id);	
	}
	
	public Path getConfiguration() {
		return configuration;	
	}

	public void setConfiguration(Path configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();
			
		if (!this.configuration.equals(configuration)) {
			this.configuration = configuration;
			solve();
		}
	}

	public Path[] getScopes() {
		Path[] scopes = new Path[this.scopes.length];
		System.arraycopy(this.scopes, 0, scopes, 0, this.scopes.length);		
		return scopes;
	}	
	
	public void setScopes(Path[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();

		Path[] scopesCopy = new Path[scopes.length];
		System.arraycopy(scopes, 0, scopesCopy, 0, scopes.length);
		
		if (!Arrays.equals(this.scopes, scopesCopy)) {
			this.scopes = scopesCopy;
			solve();
		}		
	}

	public KeySequence getMode() {
		return mode;	
	}	
	
	public void setMode(KeySequence mode)
		throws IllegalArgumentException {
		if (mode == null)
			throw new IllegalArgumentException();
			
		this.mode = mode;
		invalidateForMode();
	}

	public SortedMap getActionKeySequenceSetMap() {
		if (actionKeySequenceSetMap == null) {				
			actionKeySequenceSetMap = new TreeMap();
			SortedMap keySequenceActionMap = getKeySequenceActionMap();
			Iterator iterator = keySequenceActionMap.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				Action action = (Action) entry.getValue();		
				SortedSet keySequenceSet = 
					(SortedSet) actionKeySequenceSetMap.get(action);
				
				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionKeySequenceSetMap.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(keySequence);
			}
		}
				
		return actionKeySequenceSetMap;		
	}

	public SortedMap getKeySequenceActionMap() {
		if (keySequenceActionMap == null) {
			if (tree != null)
				keySequenceActionMap = Collections.unmodifiableSortedMap(
					Node.toKeySequenceActionMap(tree));
		
			//if (keySequenceActionMapForMode == null)
			//	keySequenceActionMapForMode = new TreeMap();
		}
		
		return keySequenceActionMap;
	}
	
	public SortedMap getActionKeySequenceSetMapForMode() {
		if (actionKeySequenceSetMapForMode == null) {
			actionKeySequenceSetMapForMode = new TreeMap();
			SortedMap keySequenceActionMap = getKeySequenceActionMapForMode();
			Iterator iterator = keySequenceActionMap.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				Action action = (Action) entry.getValue();		
				SortedSet keySequenceSet = 
					(SortedSet) actionKeySequenceSetMapForMode.get(action);
				
				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionKeySequenceSetMapForMode.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(keySequence);
			}
		}
					
		return actionKeySequenceSetMapForMode;			
	}		
	
	public SortedMap getKeySequenceActionMapForMode() {
		if (keySequenceActionMapForMode == null) {
			if (tree != null) {
				SortedMap tree = Node.find(this.tree, mode);
			
				if (tree != null)	
					keySequenceActionMapForMode = Collections.unmodifiableSortedMap(
						Node.toKeySequenceActionMap(tree));
			}
			
			//if (keySequenceActionMapForMode == null)
			//	keySequenceActionMapForMode = new TreeMap();
		}
		
		return keySequenceActionMapForMode;
	}

	public SortedSet getStrokeSetForMode() {
		if (keyStrokeSetForMode == null) {
			keyStrokeSetForMode = new TreeSet();
			SortedMap keySequenceActionMapForMode = 
				getKeySequenceActionMapForMode();
			Iterator iterator = keySequenceActionMapForMode.keySet().iterator();
			
			while (iterator.hasNext()) {
				KeySequence keySequence = (KeySequence) iterator.next();			
				List keyStrokes = keySequence.getKeyStrokes();			
				
				if (keyStrokes.size() >= 1)
					keyStrokeSetForMode.add(keyStrokes.get(0));
			}
		}
		
		return keyStrokeSetForMode;			
	}

	public String getAcceleratorTextForAction(String action)
		throws IllegalArgumentException {
		if (action == null)
			throw new IllegalArgumentException();					

		SortedMap actionSequenceMap = getActionKeySequenceSetMap();		
		SortedSet keySequenceSet = 
			(SortedSet) actionSequenceMap.get(Action.create(action));
		
		if (keySequenceSet == null)
			return null;
		else {
			Iterator iterator = keySequenceSet.iterator();
	    	StringBuffer stringBuffer = new StringBuffer();
			int i = 0;
			
			while (iterator.hasNext()) {
				if (i != 0)
					stringBuffer.append(KEY_SEQUENCE_SEPARATOR);

				KeySequence keySequence = (KeySequence) iterator.next();	
				Iterator iterator2 = keySequence.getKeyStrokes().iterator();
				int j = 0;
				
				while (iterator2.hasNext()) {					
					if (j != 0)
						stringBuffer.append(KEY_STROKE_SEPARATOR);

					KeyStroke keyStroke = (KeyStroke) iterator2.next();
					int accelerator = keyStroke.getAccelerator();
					stringBuffer.append(
						org.eclipse.jface.action.Action.convertAccelerator(
						accelerator));					
					j++;
				}

				i++;
			}
	
			return stringBuffer.toString();
		}
	}
	
	private void invalidate() {
		actionKeySequenceSetMap = null;
		keySequenceActionMap = null;
		invalidateForMode();
	}
	
	private void invalidateForMode() {
		actionKeySequenceSetMapForMode = null;
		keySequenceActionMapForMode = null;
		keyStrokeSetForMode = null;		
	}
	
	private void solve() {
		State[] states = new State[scopes.length];
			
		for (int i = 0; i < scopes.length; i++) {
			List paths = new ArrayList();
			paths.add(scopes[i]);			
			paths.add(configuration);
			paths.add(platform);
			paths.add(locale);							
			states[i] = State.create(paths);
		}
		
		Node.solveTree(tree, states);
		invalidate();
	}
}
