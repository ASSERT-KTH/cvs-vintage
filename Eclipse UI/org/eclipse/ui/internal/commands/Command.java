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

import java.text.Collator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.registry.ICommandDefinition;
import org.eclipse.ui.commands.registry.IContextBindingDefinition;
import org.eclipse.ui.commands.registry.IImageBindingDefinition;
import org.eclipse.ui.commands.registry.IKeyBindingDefinition;
import org.eclipse.ui.internal.util.Util;

final class Command implements ICommand {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Command.class.getName().hashCode();

	private static Comparator nameComparator;

	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((ICommand) left).getCommandDefinition().getName(), ((ICommand) right).getCommandDefinition().getName());
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List commands) {
		if (commands == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICommand.class);				
			ICommand command = (ICommand) object;
			sortedMap.put(command.getCommandDefinition().getId(), command);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List commands) {
		if (commands == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICommand.class);
			ICommand command = (ICommand) object;
			sortedMap.put(command.getCommandDefinition().getName(), command);									
		}			
		
		return sortedMap;
	}

	private boolean active;
	private ICommandDefinition commandDefinition;
	private List contextBindings;
	private List imageBindings;
	private List keyBindings;
	
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	Command(boolean active, ICommandDefinition commandDefinition, List contextBindings, List imageBindings, List keyBindings) {
		if (commandDefinition == null)
			throw new NullPointerException();
		
		this.active = active;
		this.commandDefinition = commandDefinition;
		this.contextBindings = Util.safeCopy(contextBindings, IContextBindingDefinition.class);
		this.imageBindings = Util.safeCopy(imageBindings, IImageBindingDefinition.class);
		this.keyBindings = Util.safeCopy(keyBindings, IKeyBindingDefinition.class);
	}
	
	public int compareTo(Object object) {
		Command command = (Command) object;
		int compareTo = active == false ? (command.active == true ? -1 : 0) : 1;
		
		if (compareTo == 0) {
			compareTo = commandDefinition.compareTo(command.commandDefinition);

			if (compareTo == 0) {	
				compareTo = Util.compare(contextBindings, command.contextBindings);

				if (compareTo == 0) {	
					compareTo = Util.compare(imageBindings, command.imageBindings);

					if (compareTo == 0)	
						compareTo = Util.compare(keyBindings, command.keyBindings);
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Command))
			return false;

		Command command = (Command) object;	
		boolean equals = true;
		equals &= active == command.active;
		equals &= commandDefinition.equals(command.commandDefinition);
		equals &= contextBindings.equals(command.contextBindings);
		equals &= imageBindings.equals(command.imageBindings);
		equals &= keyBindings.equals(command.keyBindings);
		return equals;
	}

	public boolean getActive() {
		return active;
	}

	public ICommandDefinition getCommandDefinition() {
		return commandDefinition;
	}

	public List getContextBindings() {
		return contextBindings;
	}

	public List getImageBindings() {
		return imageBindings;
	}

	public List getKeyBindings() {
		return keyBindings;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + (active ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());
			hashCode = hashCode * HASH_FACTOR + commandDefinition.hashCode();
			hashCode = hashCode * HASH_FACTOR + contextBindings.hashCode();
			hashCode = hashCode * HASH_FACTOR + imageBindings.hashCode();
			hashCode = hashCode * HASH_FACTOR + keyBindings.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(active);
			stringBuffer.append(',');
			stringBuffer.append(commandDefinition);
			stringBuffer.append(',');
			stringBuffer.append(contextBindings);
			stringBuffer.append(',');
			stringBuffer.append(imageBindings);
			stringBuffer.append(',');
			stringBuffer.append(keyBindings);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}
}
