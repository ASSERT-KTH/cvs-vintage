/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IPluginContribution;

/**
 * A <code>ColorDefiniton </code> is the representation of the extensions 
 * defined by the <code>org.eclipse.ui.colorDefinitions</code> extension point.
 * 
 *  @since 3.0
 */
public class ColorDefinition implements IPluginContribution, IHierarchalThemeElementDefinition, ICategorizedThemeElementDefinition {
	private String defaultsTo;
	private String description;
	private String id;
	private String label;
	private String pluginId;
	private String rawValue;
	private String categoryId;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param label the label for this definition
	 * @param id the identifier for this definition
	 * @param defaultsTo the id of a definition that this definition will 
	 * 		default to.
	 * @param value the default value of this definition, either in the form 
	 * rrr,ggg,bbb or the name of an SWT color constant. 
	 * @param description the description for this definition.
	 * @param pluginId the identifier of the plugin that contributed this 
	 * 		definition.
	 */
	public ColorDefinition(
		String label,
		String id,
		String defaultsTo,
		String value,
		String categoryId,
		String description,
		String pluginId) {

		this.label = label;
		this.id = id;
		this.defaultsTo = defaultsTo;
		this.rawValue = value;
		this.categoryId = categoryId;
		this.description = description;
		this.pluginId = pluginId;
	}

    /**
     * @return the categoryId, or <code>null</code> if none was supplied.
     */
    public String getCategoryId() {
        return categoryId;
    }	
	
	/**
	 * @return the defaultsTo value, or <code>null</code> if none was supplied.
	 */
	public String getDefaultsTo() {
		return defaultsTo;
	}

	/**
	 * @return the description text, or <code>null</code> if none was supplied.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the id of this definition.  Should not be <code>null</code>.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the label text.  Should not be <code>null</code>.
	 */
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @return the value. Any SWT constants  supplied to the constructor will be 
	 * evaluated and converted into their RGB value.
	 */
	public RGB getValue() {
	    return ColorUtils.getColorValue(rawValue);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getId();
	}	
}
