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
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;

/**
 * This property page contributor is created from page entry
 * in the registry. Since instances of this class are created
 * by the workbench, there is no danger of premature loading
 * of plugins.
 */

public class RegistryPageContributor implements IPropertyPageContributor {
    private String pluginId;
	private String pageName;
	private String iconName;
	private String pageId;
	private boolean isResourceContributor = false;
	private IConfigurationElement pageElement;
	private HashMap filterProperties;
	
	private static String [] resourceClassNames =
		{
			"org.eclipse.core.resources.IResource", //$NON-NLS-1$
			"org.eclipse.core.resources.IContainer", //$NON-NLS-1$
			"org.eclipse.core.resources.IFolder", //$NON-NLS-1$
			"org.eclipse.core.resources.IProject", //$NON-NLS-1$
			"org.eclipse.core.resources.IFile", //$NON-NLS-1$
		};
			
/**
 * PropertyPageContributor constructor.
 */
public RegistryPageContributor(String pluginId, String pageId, String pageName, String iconName, HashMap filterProperties, String objectClassName, boolean adaptable, IConfigurationElement pageElement) {
    this.pluginId = pluginId;
	this.pageId = pageId;
	this.pageName = pageName;
	this.iconName = iconName;
	this.filterProperties = filterProperties;
	this.pageElement = pageElement;
	
	//Only adapt if explicitly allowed to do so
	if(adaptable)
		checkIsResourcePage(objectClassName);
}
/**
 * Implements the interface by creating property page specified with
 * the configuration element.
 */
public boolean contributePropertyPages(PropertyPageManager mng, IAdaptable element) {
	PropertyPageNode node = new PropertyPageNode(this, element);
	mng.addToRoot(node);
	return true;
}
/**
 * Creates the page based on the information in the configuration element.
 */
public IWorkbenchPropertyPage createPage(IAdaptable element) throws CoreException {
	IWorkbenchPropertyPage ppage = null;
	ppage = (IWorkbenchPropertyPage)WorkbenchPlugin.createExtension(
		pageElement, PropertyPagesRegistryReader.ATT_CLASS);

	Class resourceClass = LegacyResourceSupport.getResourceClass();
	if (isResourceContributor && resourceClass != null) {
		ppage.setElement((IAdaptable) element.getAdapter(resourceClass));
	} else {
		ppage.setElement(element);
	}
	ppage.setTitle(pageName);
	return ppage;
}
/**
 * Returns page icon as defined in the registry.
 */
public ImageDescriptor getPageIcon() {
	if (iconName==null) return null;
	IExtension extension = pageElement.getDeclaringExtension();
	String extendingPluginId =
		extension.getDeclaringPluginDescriptor().getUniqueIdentifier();
	return AbstractUIPlugin.imageDescriptorFromPlugin(
		extendingPluginId, iconName);
}
/**
 * Returns page ID as defined in the registry.
 */

public String getPageId() {
	return pageId;
}

/**
 * Returns plugin ID as defined in the registry.
 */

public String getPluginId() {
    return pluginId;
}

/**
 * Returns page name as defined in the registry.
 */

public String getPageName() {
	return pageName;
}
/**
 * Return true if name filter is not defined in the registry for this page,
 * or if name of the selected object matches the name filter.
 */
public boolean isApplicableTo(Object object) {
	// Test name filter
	String nameFilter = pageElement.getAttribute(PropertyPagesRegistryReader.ATT_NAME_FILTER);
	if (nameFilter != null) {
		String objectName = object.toString();
		if (object instanceof IAdaptable) {
			IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)object).getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) {
				String elementName = adapter.getLabel(object);
				if (elementName != null) {
					objectName = elementName;
				}
			}
		}
		if (!SelectionEnabler.verifyNameMatch(objectName, nameFilter))
			return false;
	}

	// Test custom filter	
	if (filterProperties == null)
		return true;
	IActionFilter filter = null;

	// If this is a resource contributor and the object is not a resource but
	// is an adaptable then get the object's resource via the adaptable mechanism.
	Object testObject = object;
	Class resourceClass = LegacyResourceSupport.getResourceClass();
	if (isResourceContributor
	    && resourceClass != null
		&& !(resourceClass.isInstance(object))
		&& (object instanceof IAdaptable)) { 
			Object result = ((IAdaptable)object).getAdapter(resourceClass);
			if (result != null) {
				testObject = result;
			}
	}
	
	if (testObject instanceof IActionFilter) {
		filter = (IActionFilter)testObject;
	} else if (testObject instanceof IAdaptable) {
		filter = (IActionFilter)((IAdaptable)testObject).getAdapter(IActionFilter.class);
	}

	if (filter != null) {
		return testCustom(testObject, filter);
	} else {
		return true;
	}
}
/**
 * Returns whether the object passes a custom key value filter
 * implemented by a matcher.
 */
private boolean testCustom(Object object, IActionFilter filter) {
	if (filterProperties == null)
		return false;
	Iterator iter = filterProperties.keySet().iterator();
	while (iter.hasNext()) {
		String key = (String)iter.next();
		String value = (String)filterProperties.get(key);
		if (!filter.testAttribute(object, key, value))
			return false;
	}
	return true;
}

/**
 * Check if the object class name is for a class that
 * inherits from IResource. If so mark the receiver as 
 * a resource contributor
 */

private void checkIsResourcePage(String objectClassName){
	
	for(int i = 0; i < resourceClassNames.length; i++){
		if(resourceClassNames[i].equals(objectClassName)){
			isResourceContributor = true;
			return;
		}
	}
}


/*
 * @see IObjectContributor#canAdapt()
 */
public boolean canAdapt() {
	return isResourceContributor;
}

}
