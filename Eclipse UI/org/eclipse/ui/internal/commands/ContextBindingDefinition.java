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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class ContextBindingDefinition
	implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ContextBindingDefinition.class.getName().hashCode();

	static Map activityBindingDefinitionsByCommandId(Collection activityBindingDefinitions) {
		if (activityBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = activityBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ContextBindingDefinition.class);
			ContextBindingDefinition activityBindingDefinition =
				(ContextBindingDefinition) object;
			String commandId = activityBindingDefinition.getCommandId();

			if (commandId != null) {
				Collection activityBindingDefinitions2 =
					(Collection) map.get(commandId);

				if (activityBindingDefinitions2 == null) {
					activityBindingDefinitions2 = new ArrayList();
					map.put(commandId, activityBindingDefinitions2);
				}

				activityBindingDefinitions2.add(activityBindingDefinition);
			}
		}

		return map;
	}

	private String activityId;
	private String commandId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String pluginId;
	private transient String string;

	public ContextBindingDefinition(
		String activityId,
		String commandId,
		String pluginId) {
		this.activityId = activityId;
		this.commandId = commandId;
		this.pluginId = pluginId;
	}

	public int compareTo(Object object) {
		ContextBindingDefinition castedObject =
			(ContextBindingDefinition) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(commandId, castedObject.commandId);

			if (compareTo == 0)
				compareTo = Util.compare(pluginId, castedObject.pluginId);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ContextBindingDefinition))
			return false;

		ContextBindingDefinition castedObject =
			(ContextBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(activityId, castedObject.activityId);
		equals &= Util.equals(commandId, castedObject.commandId);
		equals &= Util.equals(pluginId, castedObject.pluginId);
		return equals;
	}

	public String getActivityId() {
		return activityId;
	}

	public String getCommandId() {
		return commandId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(activityId);
			stringBuffer.append(',');
			stringBuffer.append(commandId);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
