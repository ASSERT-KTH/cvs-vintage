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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.activities.ActivityEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityActivityBinding;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Activity implements IActivity {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Activity.class.getName().hashCode();
	private Set activityActivityBindings;
	private transient IActivityActivityBinding[] activityActivityBindingsAsArray;
	private List activityListeners;
	private MutableActivityManager activityManager;
	private Set activityPatternBindings;
	private transient IActivityPatternBinding[] activityPatternBindingsAsArray;
	private boolean defined;
	private String description;
	private boolean enabled;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String id;
	private String name;
	private String parentId;
	private transient String string;

	Activity(MutableActivityManager activityManager, String id) {
		if (activityManager == null || id == null)
			throw new NullPointerException();

		this.activityManager = activityManager;
		this.id = id;
	}

	public void addActivityListener(IActivityListener activityListener) {
		if (activityListener == null)
			throw new NullPointerException();

		if (activityListeners == null)
			activityListeners = new ArrayList();

		if (!activityListeners.contains(activityListener))
			activityListeners.add(activityListener);

		activityManager.getActivitiesWithListeners().add(this);
	}

	public int compareTo(Object object) {
		Activity castedObject = (Activity) object;

		int compareTo =
			Util.compare(
				(Comparable[]) activityActivityBindingsAsArray,
				(Comparable[]) castedObject.activityActivityBindingsAsArray);

		if (compareTo == 0) {
			compareTo =
				Util.compare(
					(Comparable[]) activityPatternBindingsAsArray,
					(Comparable[]) castedObject.activityPatternBindingsAsArray);

			if (compareTo == 0) {
				compareTo = Util.compare(defined, castedObject.defined);

				if (compareTo == 0) {
					compareTo =
						Util.compare(description, castedObject.description);

					if (compareTo == 0) {
						compareTo = Util.compare(enabled, castedObject.enabled);

						if (compareTo == 0) {
							compareTo = Util.compare(id, castedObject.id);

							if (compareTo == 0) {
								compareTo =
									Util.compare(name, castedObject.name);

								if (compareTo == 0)
									compareTo =
										Util.compare(
											parentId,
											castedObject.parentId);
							}
						}
					}
				}
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Activity))
			return false;

		Activity castedObject = (Activity) object;
		boolean equals = true;
		equals
			&= Util.equals(
				activityActivityBindings,
				castedObject.activityActivityBindings);
		equals
			&= Util.equals(
				activityPatternBindings,
				castedObject.activityPatternBindings);
		equals &= Util.equals(defined, castedObject.defined);
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(enabled, castedObject.enabled);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		equals &= Util.equals(parentId, castedObject.parentId);
		return equals;
	}

	void fireActivityChanged(ActivityEvent activityEvent) {
		if (activityEvent == null)
			throw new NullPointerException();

		if (activityListeners != null)
			for (int i = 0; i < activityListeners.size(); i++)
				((IActivityListener) activityListeners.get(i)).activityChanged(
					activityEvent);
	}

	public Set getActivityActivityBindings() {
		return activityActivityBindings;
	}

	public Set getActivityPatternBindings() {
		return activityPatternBindings;
	}

	public String getDescription() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return name;
	}

	public String getParentId() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return parentId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode =
				hashCode * HASH_FACTOR
					+ Util.hashCode(activityActivityBindings);
			hashCode =
				hashCode * HASH_FACTOR + Util.hashCode(activityPatternBindings);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(enabled);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public boolean isDefined() {
		return defined;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isMatch(String string) {
		if (isDefined())
			for (Iterator iterator = activityPatternBindings.iterator();
				iterator.hasNext();
				) {
				IActivityPatternBinding activityPatternBinding =
					(IActivityPatternBinding) iterator.next();

				if (activityPatternBinding
					.getPattern()
					.matcher(string)
					.matches())
					return true;
			}

		return false;
	}

	public void removeActivityListener(IActivityListener activityListener) {
		if (activityListener == null)
			throw new NullPointerException();

		if (activityListeners != null)
			activityListeners.remove(activityListener);

		if (activityListeners.isEmpty())
			activityManager.getActivitiesWithListeners().remove(this);
	}

	boolean setActivityActivityBindings(Set activityActivityBindings) {
		activityActivityBindings =
			Util.safeCopy(
				activityActivityBindings,
				IActivityActivityBinding.class);

		if (!Util
			.equals(activityActivityBindings, this.activityActivityBindings)) {
			this.activityActivityBindings = activityActivityBindings;
			this.activityActivityBindingsAsArray =
				(
					IActivityActivityBinding[]) this
						.activityActivityBindings
						.toArray(
					new IActivityActivityBinding[this
						.activityActivityBindings
						.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setActivityPatternBindings(Set activityPatternBindings) {
		activityPatternBindings =
			Util.safeCopy(
				activityPatternBindings,
				IActivityPatternBinding.class);

		if (!Util
			.equals(activityPatternBindings, this.activityPatternBindings)) {
			this.activityPatternBindings = activityPatternBindings;
			this.activityPatternBindingsAsArray =
				(
					IActivityPatternBinding[]) this
						.activityPatternBindings
						.toArray(
					new IActivityPatternBinding[this
						.activityPatternBindings
						.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setDefined(boolean defined) {
		if (defined != this.defined) {
			this.defined = defined;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setDescription(String description) {
		if (!Util.equals(description, this.description)) {
			this.description = description;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setName(String name) {
		if (!Util.equals(name, this.name)) {
			this.name = name;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setParentId(String parentId) {
		if (!Util.equals(parentId, this.parentId)) {
			this.parentId = parentId;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(activityActivityBindings);
			stringBuffer.append(',');
			stringBuffer.append(activityPatternBindings);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(enabled);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(parentId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
