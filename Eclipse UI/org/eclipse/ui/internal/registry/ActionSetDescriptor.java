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
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * ActionSetDescriptor
 */
public class ActionSetDescriptor
	implements IActionSetDescriptor, IAdaptable, IWorkbenchAdapter
{
	private static final Object[] NO_CHILDREN = new Object[0];
	private static final String INITIALLY_VISIBLE_PREF_ID_PREFIX = "actionSet.initiallyVisible."; //$NON-NLS-1$
	
	private String id;
	private String label;
	private String category;
	private boolean visible;
	private String description;
	private IConfigurationElement configElement;
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_LABEL="label";//$NON-NLS-1$
	private static final String ATT_VISIBLE="visible";//$NON-NLS-1$
	private static final String ATT_DESC="description";//$NON-NLS-1$
/**
 * Create a descriptor from a config element.
 */
public ActionSetDescriptor(IConfigurationElement configElement)
	throws CoreException
{
	super();
	this.configElement = configElement;
	id = configElement.getAttribute(ATT_ID);
	label = configElement.getAttribute(ATT_LABEL);
	description = configElement.getAttribute(ATT_DESC);
	String str = configElement.getAttribute(ATT_VISIBLE);
	if (str != null && str.equals("true"))//$NON-NLS-1$
		visible = true;

	// Sanity check.
	if (label == null) {
		throw new CoreException(new Status(IStatus.ERROR,
			WorkbenchPlugin.PI_WORKBENCH, 0,
			"Invalid extension (missing label): " + id,//$NON-NLS-1$
			null));
	}
}
/**
 * Returns the action set for this descriptor.
 *
 * @return the action set
 */
public IActionSet createActionSet()
	throws CoreException
{
	return new PluginActionSet(this);
}
/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) 
		return this;
	return null;
}
/**
 * Returns the category of this action set.
 *
 * @return a non-empty category name or <cod>null</code> if none specified
 */
public String getCategory() {
	return category;
}
/**
 * @see IWorkbenchAdapter#getChildren
 */
public Object[] getChildren(Object o) {
	if (o == this)
		return (new PluginActionSetReader()).readActionDescriptors(this);

	return NO_CHILDREN;
}
/**
 * Returns the config element
 */
public IConfigurationElement getConfigElement() {
	return configElement;
}
/**
 * Returns this action set's description. 
 * This is the value of its <code>"description"</code> attribute.
 *
 * @return the description
 */
public String getDescription() {
	return description;
}
/**
 * Returns this action set's id. 
 * This is the value of its <code>"id"</code> attribute.
 * <p>
 *
 * @return the action set id
 */
public String getId() {
	return id;
}

/**
 * Returns the preference identifier used to store the initially visible preference.
 * 
 * @since 3.0
 */
private String getInitiallyVisiblePrefId() {
	return INITIALLY_VISIBLE_PREF_ID_PREFIX + id;
 }

/**
 * Returns this action set's label. 
 * This is the value of its <code>"label"</code> attribute.
 *
 * @return the label
 */
public String getLabel() {
	return label;
}
/**
 * @see IWorkbenchAdapter#getLabel
 */
public String getLabel(Object o) {
	if (o == this)
		return getLabel();
	return "Unknown Label";//$NON-NLS-1$
}
/**
 * Returns whether this action set is initially visible.
 */
public boolean isInitiallyVisible() {
    if (id == null)
		return visible;
	Preferences prefs = WorkbenchPlugin.getDefault().getPluginPreferences();
	String prefId = getInitiallyVisiblePrefId();
	if (prefs.isDefault(prefId))
		return visible;
	return prefs.getBoolean(prefId);
}

/**
 * Sets whether this action set is initially visible.
 * If the action set identifier is undefined, then this is ignored.
 * 
 * @since 3.0
 */
public void setInitiallyVisible(boolean newValue) {
	if (id == null)
		return;
	Preferences prefs = WorkbenchPlugin.getDefault().getPluginPreferences();
	prefs.setValue(getInitiallyVisiblePrefId(), newValue);
}

/**
 * Sets the category of this action set.
 *
 * @param cat a non-empty category id
 */
public void setCategory(String id) {
	category = id;
}
/* (non-Javadoc)
 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
 */
public ImageDescriptor getImageDescriptor(Object object) {
	return null;
}
/* (non-Javadoc)
 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
 */
public Object getParent(Object o) {
	return null;
}
}
