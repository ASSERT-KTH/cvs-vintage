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

package org.eclipse.ui.internal.commands.old;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

abstract class AbstractRegistry 
	implements IRegistry {

	protected List activeGestureConfigurations = Collections.EMPTY_LIST;
	protected List activeKeyConfigurations = Collections.EMPTY_LIST;
	protected List categories = Collections.EMPTY_LIST; 
	protected List commands = Collections.EMPTY_LIST; 
	protected List contextBindings = Collections.EMPTY_LIST;
	protected List contexts = Collections.EMPTY_LIST; 
	protected List gestureBindings = Collections.EMPTY_LIST;
	protected List gestureConfigurations = Collections.EMPTY_LIST;
	protected List keyBindings = Collections.EMPTY_LIST;
	protected List keyConfigurations = Collections.EMPTY_LIST;
	
	protected AbstractRegistry() {
		super();
	}

	public List getActiveGestureConfigurations() {
		return activeGestureConfigurations;
	}

	public List getActiveKeyConfigurations() {
		return activeKeyConfigurations;
	}

	public List getCategories() {
		return categories;
	}
	
	public List getCommands() {
		return commands;
	}

	public List getContextBindings() {
		return contextBindings;
	}

	public List getContexts() {
		return contexts;
	}

	public List getGestureBindings() {
		return gestureBindings;
	}

	public List getGestureConfigurations() {
		return gestureConfigurations;
	}
	
	public List getKeyBindings() {
		return keyBindings;
	}

	public List getKeyConfigurations() {
		return keyConfigurations;
	}

	public abstract void load()
		throws IOException;
}	
