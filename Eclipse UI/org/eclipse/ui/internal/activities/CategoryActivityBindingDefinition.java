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

final class CategoryActivityBindingDefinition
	implements ICategoryActivityBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		CategoryActivityBindingDefinition.class.getName().hashCode();

	static Map categoryActivityBindingDefinitionsByCategoryId(Collection categoryActivityBindingDefinitions) {
		if (categoryActivityBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = categoryActivityBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(
				object,
				ICategoryActivityBindingDefinition.class);
			ICategoryActivityBindingDefinition categoryActivityBindingDefinition =
				(ICategoryActivityBindingDefinition) object;
			String categoryId =
				categoryActivityBindingDefinition.getCategoryId();

			if (categoryId != null) {
				Collection categoryActivityBindingDefinitions2 =
					(Collection) map.get(categoryId);

				if (categoryActivityBindingDefinitions2 == null) {
					categoryActivityBindingDefinitions2 = new HashSet();
					map.put(categoryId, categoryActivityBindingDefinitions2);
				}

				categoryActivityBindingDefinitions2.add(
					categoryActivityBindingDefinition);
			}
		}

		return map;
	}

	private String activityId;
	private String categoryId;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String pluginId;
	private transient String string;

	CategoryActivityBindingDefinition(
		String activityId,
		String categoryId,
		String pluginId) {
		this.activityId = activityId;
		this.categoryId = categoryId;
		this.pluginId = pluginId;
	}

	public int compareTo(Object object) {
		CategoryActivityBindingDefinition castedObject =
			(CategoryActivityBindingDefinition) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(categoryId, castedObject.categoryId);

			if (compareTo == 0)
				compareTo = Util.compare(pluginId, castedObject.pluginId);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CategoryActivityBindingDefinition))
			return false;

		CategoryActivityBindingDefinition castedObject =
			(CategoryActivityBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(activityId, castedObject.activityId);
		equals &= Util.equals(categoryId, castedObject.categoryId);
		equals &= Util.equals(pluginId, castedObject.pluginId);
		return equals;
	}

	public String getActivityId() {
		return activityId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
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
			stringBuffer.append(categoryId);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
