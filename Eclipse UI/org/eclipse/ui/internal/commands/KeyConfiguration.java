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
import java.util.List;
import java.util.Set;

import org.eclipse.ui.internal.commands.api.IKeyConfiguration;
import org.eclipse.ui.internal.commands.api.IKeyConfigurationEvent;
import org.eclipse.ui.internal.commands.api.IKeyConfigurationListener;
import org.eclipse.ui.internal.commands.api.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class KeyConfiguration implements IKeyConfiguration {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyConfiguration.class.getName().hashCode();

	private boolean active;
	private List keyConfigurationListeners;
	private Set keyConfigurationsWithListeners;
	private boolean defined;
	private String description;
	private String id;
	private String name;
	private String parentId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	KeyConfiguration(Set keyConfigurationsWithListeners, String id) {	
		if (keyConfigurationsWithListeners == null || id == null)
			throw new NullPointerException();

		this.keyConfigurationsWithListeners = keyConfigurationsWithListeners;
		this.id = id;
	}

	public void addKeyConfigurationListener(IKeyConfigurationListener keyConfigurationListener) {
		if (keyConfigurationListener == null)
			throw new NullPointerException();
		
		if (keyConfigurationListeners == null)
			keyConfigurationListeners = new ArrayList();
		
		if (!keyConfigurationListeners.contains(keyConfigurationListener))
			keyConfigurationListeners.add(keyConfigurationListener);
		
		keyConfigurationsWithListeners.add(this);
	}

	public int compareTo(Object object) {
		KeyConfiguration castedObject = (KeyConfiguration) object;
		int compareTo = Util.compare(active, castedObject.active);
		
		if (compareTo == 0) {
			compareTo = Util.compare(defined, castedObject.defined);
			
			if (compareTo == 0) {
				compareTo = Util.compare(description, castedObject.description);
									
				if (compareTo == 0) {		
					compareTo = Util.compare(id, castedObject.id);			
				
					if (compareTo == 0) {
						compareTo = Util.compare(name, castedObject.name);

						if (compareTo == 0)
							compareTo = Util.compare(parentId, castedObject.parentId);
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyConfiguration))
			return false;

		KeyConfiguration castedObject = (KeyConfiguration) object;	
		boolean equals = true;
		equals &= Util.equals(active, castedObject.active);
		equals &= Util.equals(defined, castedObject.defined);
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		equals &= Util.equals(parentId, castedObject.parentId);
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

	public String getParentId()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return parentId;
	}		
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(active);			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);	
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public boolean isActive() {
		return active;
	}
	
	public boolean isDefined() {
		return defined;
	}

	public void removeKeyConfigurationListener(IKeyConfigurationListener keyConfigurationListener) {
		if (keyConfigurationListener == null)
			throw new NullPointerException();

		if (keyConfigurationListeners != null)
			keyConfigurationListeners.remove(keyConfigurationListener);
		
		if (keyConfigurationListeners.isEmpty())
			keyConfigurationsWithListeners.remove(this);
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(active);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
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
	
	void fireKeyConfigurationChanged(IKeyConfigurationEvent keyConfigurationEvent) {
		if (keyConfigurationEvent == null)
			throw new NullPointerException();
		
		if (keyConfigurationListeners != null)
			for (int i = 0; i < keyConfigurationListeners.size(); i++)
				((IKeyConfigurationListener) keyConfigurationListeners.get(i)).keyConfigurationChanged(keyConfigurationEvent);
	}
	
	boolean setActive(boolean active) {
		if (active != this.active) {
			this.active = active;
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
}
