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

package org.eclipse.ui.internal.activities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

final class ActivityDefinition implements Comparable, IActivityDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ActivityDefinition.class.getName().hashCode();

	static Map activityDefinitionsById(
		Collection activityDefinitions,
		boolean allowNullIds) {
		if (activityDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = activityDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IActivityDefinition.class);
			IActivityDefinition activityDefinition =
				(IActivityDefinition) object;
			String id = activityDefinition.getId();

			if (allowNullIds || id != null)
				map.put(id, activityDefinition);
		}

		return map;
	}

	static Map activityDefinitionsByName(
		Collection activityDefinitions,
		boolean allowNullNames) {
		if (activityDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = activityDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IActivityDefinition.class);
			IActivityDefinition activityDefinition =
				(IActivityDefinition) object;
			String name = activityDefinition.getName();

			if (allowNullNames || name != null) {
				Collection activityDefinitions2 = (Collection) map.get(name);

				if (activityDefinitions2 == null) {
					activityDefinitions2 = new HashSet();
					map.put(name, activityDefinitions2);
				}

				activityDefinitions2.add(activityDefinition);
			}
		}

		return map;
	}

	private String description;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String id;
	private String name;
	private String parentId;
	private String pluginId;
	private transient String string;

	ActivityDefinition(
		String description,
		String id,
		String name,
		String parentId,
		String pluginId) {
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.pluginId = pluginId;
	}

	public int compareTo(Object object) {
		ActivityDefinition castedObject = (ActivityDefinition) object;
		int compareTo = Util.compare(description, castedObject.description);

		if (compareTo == 0) {
			compareTo = Util.compare(id, castedObject.id);

			if (compareTo == 0) {
				compareTo = Util.compare(name, castedObject.name);

				if (compareTo == 0) {
					compareTo = Util.compare(parentId, castedObject.parentId);

					if (compareTo == 0)
						compareTo =
							Util.compare(pluginId, castedObject.pluginId);
				}
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ActivityDefinition))
			return false;

		ActivityDefinition castedObject = (ActivityDefinition) object;
		boolean equals = true;
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		equals &= Util.equals(parentId, castedObject.parentId);
		equals &= Util.equals(pluginId, castedObject.pluginId);
		return equals;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getParentId() {
		return parentId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(parentId);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
