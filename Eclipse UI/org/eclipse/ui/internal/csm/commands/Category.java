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

package org.eclipse.ui.internal.csm.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.internal.csm.commands.api.ICategory;
import org.eclipse.ui.internal.csm.commands.api.ICategoryEvent;
import org.eclipse.ui.internal.csm.commands.api.ICategoryListener;
import org.eclipse.ui.internal.csm.commands.api.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Category implements ICategory {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Category.class.getName().hashCode();

	private Set categoriesWithListeners;
	private List categoryListeners;
	private boolean defined;
	private String description;
	private String id;
	private String name;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	Category(Set categoriesWithListeners, String id) {	
		if (categoriesWithListeners == null || id == null)
			throw new NullPointerException();

		this.categoriesWithListeners = categoriesWithListeners;
		this.id = id;
	}

	public void addCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null)
			throw new NullPointerException();
		
		if (categoryListeners == null)
			categoryListeners = new ArrayList();
		
		if (!categoryListeners.contains(categoryListener))
			categoryListeners.add(categoryListener);
		
		categoriesWithListeners.add(this);
	}

	public int compareTo(Object object) {
		Category castedObject = (Category) object;
		int compareTo = Util.compare(defined, castedObject.defined);
				
		if (compareTo == 0) {
			compareTo = Util.compare(description, castedObject.description);
									
			if (compareTo == 0) {		
				compareTo = Util.compare(id, castedObject.id);			
			
				if (compareTo == 0)
					compareTo = Util.compare(name, castedObject.name);
			}
		}

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Category))
			return false;

		Category castedObject = (Category) object;	
		boolean equals = true;
		equals &= Util.equals(defined, castedObject.defined);
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		return equals;
	}

	public String getDescription()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();
			
		return description;	
	}
	
	public String getId() {
		return id;	
	}

	public String getName()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return name;
	}	
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);	
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}
	
	public boolean isDefined() {
		return defined;
	}

	public void removeCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null)
			throw new NullPointerException();

		if (categoryListeners != null)
			categoryListeners.remove(categoryListener);
		
		if (categoryListeners.isEmpty())
			categoriesWithListeners.remove(this);
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
	
	void fireCategoryChanged(ICategoryEvent categoryEvent) {
		if (categoryEvent == null)
			throw new NullPointerException();
		
		if (categoryListeners != null)
			for (int i = 0; i < categoryListeners.size(); i++)
				((ICategoryListener) categoryListeners.get(i)).categoryChanged(categoryEvent);
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
}
