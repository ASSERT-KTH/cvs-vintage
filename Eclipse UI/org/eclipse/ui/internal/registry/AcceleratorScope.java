package org.eclipse.ui.internal.registry;

/**
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

public final class AcceleratorScope {

	private String id;
	private String name;
	private String description;
	private String parentId;
	private String pluginId;
	private AcceleratorScope parent;
		
	AcceleratorScope(String id, String name, String description, String parentId, String pluginId) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentId = parentId;
		this.pluginId = pluginId;
	}

	public String getId() {
		return id;	
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;	
	}
	
	public String getParentId() {
		return parentId;
	}
	
	public String getPluginId() {
		return pluginId;
	}

	public AcceleratorScope getParent() {
		if (IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID.equals(id))
			return null;
			
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		
		if (parent == null) {
			parent = registry.getScope(parentId);
			
			if (parent == null) 
				parent = registry.getScope(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);
		}
		
		return parent;
	}
}
