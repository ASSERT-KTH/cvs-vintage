/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

final class Node {
	
	static boolean add(SortedMap plugins, Identifier plugin, Action action)
		throws IllegalArgumentException {
		if (plugins == null || plugin == null || action == null)
			throw new IllegalArgumentException();
						
		SortedSet actions = (SortedSet) plugins.get(plugin);
			
		if (actions == null) {
			actions = new TreeSet();
			plugins.put(plugin, actions);
		}	
		
		return actions.add(action);
	}

	static boolean remove(SortedMap plugins, Identifier plugin, Action action)
		throws IllegalArgumentException {
		if (plugins == null || plugin == null || action == null)
			throw new IllegalArgumentException();

		SortedSet actions = (SortedSet) plugins.get(plugin);

		if (actions == null)
			return false;

		boolean removed = actions.remove(action);
		
		if (actions.isEmpty())
			plugins.remove(plugin);

		return removed;			
	}

	static Action solve(SortedMap plugins)
		throws IllegalArgumentException {
		if (plugins == null)
			throw new IllegalArgumentException();	
	
		SortedSet actions = (SortedSet) plugins.get(Identifier.create(null));
		
		if (actions == null) {
			actions = new TreeSet();
			Iterator iterator = plugins.values().iterator();
		
			while (iterator.hasNext())
				actions.addAll((SortedSet) iterator.next());
		}

		return actions.size() == 1 ? (Action) actions.first() : Action.create(null);
	}

	static boolean add(SortedMap states, State state, Identifier plugin, Action action)
		throws IllegalArgumentException {
		if (states == null || state == null || plugin == null || action == null)
			throw new IllegalArgumentException();		
		
		SortedMap plugins = (SortedMap) states.get(state);
			
		if (plugins != null) {
			plugins = new TreeMap();
			states.put(state, plugins);
		}	
		
		return add(plugins, plugin, action);
	}

	static boolean remove(SortedMap states, State state, Identifier plugin, Action action)
		throws IllegalArgumentException {
		if (states == null || state == null || plugin == null || action == null)
			throw new IllegalArgumentException();			
		
		SortedMap plugins = (SortedMap) states.get(state);

		if (plugins == null)
			return false;

		boolean removed = remove(plugins, plugin, action);
		
		if (plugins.isEmpty())
			states.remove(plugins);

		return removed;			 
	}

	static ActionMatch solve(SortedMap states, State state)
		throws IllegalArgumentException {
		if (states == null || state == null)
			throw new IllegalArgumentException();			
	
		ActionMatch actionMatch = null;
		Iterator iterator = states.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testState = (State) entry.getKey();
			SortedMap testContributions = (SortedMap) entry.getValue();

			if (testContributions != null) {
				Action testAction = solve(testContributions);
			
				if (testAction != null && testAction.getValue() != null) {
					int match = testState.match(state);
					
					if (match >= 0) {
						if (match == 0)
							return ActionMatch.create(testAction, 0);
						else if (actionMatch == null || match < actionMatch.getMatch())
							actionMatch = ActionMatch.create(testAction, match);
					}
				}
			}
		}
			
		return actionMatch;	
	}

	static ActionMatch solve(SortedMap states, State[] stack)
		throws IllegalArgumentException {
		if (states == null || stack == null)
			throw new IllegalArgumentException();
	
		for (int i = 0; i < stack.length; i++)
			if (stack == null)
				throw new IllegalArgumentException();	
	
		for (int i = 0; i < stack.length; i++) {
			ActionMatch actionMatch = solve(states, stack[i]);
				
			if (actionMatch != null)
				return actionMatch;
		}
		
		return null;
	}
	
	static void addToTree(SortedMap tree, KeyBinding keyBinding, SortedMap configurationMap, SortedMap scopeMap) {
		List keyStrokes = keyBinding.getKeySequence().getKeyStrokes();		
		Path configuration = (Path) configurationMap.get(keyBinding.getConfiguration());
		
		if (configuration == null)
			return;

		Path locale = KeyBindingManager.pathForLocale(keyBinding.getLocale());
		Path platform = KeyBindingManager.pathForPlatform(keyBinding.getPlatform());
		Path scope = (Path) scopeMap.get(keyBinding.getScope());
		
		if (scope == null)
			return;
		
		SortedMap root = tree;
		Node node = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			node = (Node) root.get(keyStroke);
			
			if (node == null) {
				node = new Node();	
				root.put(keyStroke, node);
			}
			
			root = node.children;
		}

		if (node != null) {
			SortedMap states = node.states;	
			List paths = new ArrayList();
			paths.add(scope);			
			paths.add(configuration);
			paths.add(platform);
			paths.add(locale);					
			State state = State.create(paths);			
			SortedMap contributorToActionSetMap = (SortedMap) states.get(state);
			
			if (contributorToActionSetMap == null) {
				contributorToActionSetMap = new TreeMap();	
				states.put(state, contributorToActionSetMap);
			}
			
			add(contributorToActionSetMap, Identifier.create(keyBinding.getPlugin()), Action.create(keyBinding.getAction()));			
		}
	}

	static boolean removeFromTree(SortedMap tree, 
		KeyBinding keyBinding) {
		// TBD
		return false;
	}

	static void solveTree(SortedMap tree, State[] stack) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			Node node = (Node) iterator.next();			
			node.bestActionMatch = solve(node.states, stack);
			solveTree(node.children, stack);								
			Iterator iterator2 = node.children.values().iterator();	
			
			while (iterator2.hasNext()) {
				Node child = (Node) iterator2.next();
				ActionMatch childActionMatch = child.bestActionMatch;				
				
				if (childActionMatch != null && 
					(node.bestChildActionMatch == null || childActionMatch.getMatch() < node.bestChildActionMatch.getMatch())) 
					node.bestChildActionMatch = childActionMatch;
			}
		}		
	}

	static SortedMap find(SortedMap tree, KeySequence prefix) {	
		Iterator iterator = prefix.getKeyStrokes().iterator();
	
		while (iterator.hasNext()) {
			Node node = (Node) tree.get(iterator.next());
			
			if (node == null)
				return null;
				
			tree = node.children;
		}		
		
		return tree;			
	}

	static List toBindings(SortedMap tree) {
		List bindings = new ArrayList();
		toBindings(tree, KeySequence.create(), bindings);
		return bindings;
	}
	
	private static void toBindings(SortedMap tree, KeySequence prefix,
		List bindings) {
		Iterator iterator = tree.entrySet().iterator();	
			
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.create(keyStrokes);				
			Node node = (Node) entry.getValue();
			Iterator iterator2 = node.states.entrySet().iterator();	
			
			while (iterator2.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iterator2.next();
				State state = (State) entry2.getKey();	
				SortedMap plugins = (SortedMap) entry2.getValue();			
				Iterator iterator3 = plugins.entrySet().iterator();
				
				while (iterator3.hasNext()) {
					Map.Entry entry3 = (Map.Entry) iterator3.next();
					Identifier plugin = (Identifier) entry3.getKey();	
					SortedSet actions = (SortedSet) entry3.getValue();
					Iterator iterator4 = actions.iterator();
					
					while (iterator4.hasNext()) {
						Action action = (Action) iterator4.next();						
						/* TBD:
						bindings.add(KeyBinding.create(keySequence, state, plugin, action));
						*/
					}				
				}			
			}
			
			toBindings(node.children, keySequence, bindings);
		}	
	}

	static SortedMap toKeySequenceActionMap(SortedMap tree) {
		SortedMap keySequenceActionMap = new TreeMap();
		toKeySequenceActionMap(tree, KeySequence.create(), keySequenceActionMap);
		return keySequenceActionMap;	
	}				

	private static void toKeySequenceActionMap(SortedMap tree, 
		KeySequence prefix, SortedMap keySequenceActionMap) {
		Iterator iterator = tree.entrySet().iterator();	
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.create(keyStrokes);				
			Node node = (Node) entry.getValue();			
			
			if (node.bestChildActionMatch != null && 
				(node.bestActionMatch == null || node.bestChildActionMatch.getMatch() < node.bestActionMatch.getMatch()))
				toKeySequenceActionMap(node.children, keySequence, keySequenceActionMap);	
			else if (node.bestActionMatch != null) {
				Action action = node.bestActionMatch.getAction();
				
				if (action.getValue() != null)				
					keySequenceActionMap.put(keySequence, action);				
			}
		}	
	}

	SortedMap children = new TreeMap();	
	SortedMap states = new TreeMap();
		
	ActionMatch bestActionMatch = null;
	ActionMatch bestChildActionMatch = null;
}
