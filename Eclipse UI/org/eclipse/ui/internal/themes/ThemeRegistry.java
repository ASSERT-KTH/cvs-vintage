/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.themes.IThemeManager;


/**
 * The central manager for Theme descriptors.
 *
 * @since 3.0
 */
public class ThemeRegistry implements IThemeRegistry {

    private List themes;
	private List colors;
	private List fonts;
	private List categories;
	private Map dataMap;

	/**
	 * Create a new ThemeRegistry.
	 */
	public ThemeRegistry() {
		themes = new ArrayList();
		colors = new ArrayList();
		fonts = new ArrayList();
		categories = new ArrayList();
		dataMap = new HashMap();
	}

	/**
	 * Add a descriptor to the registry.
	 */
	void add(IThemeDescriptor desc) {
		themes.add(desc);
	}
	
	/**
	 * Add a descriptor to the registry.
	 */
	void add(ColorDefinition desc) {
		colors.add(desc);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#findCategory(java.lang.String)
     */
    public ThemeElementCategory findCategory(String id) {
        return (ThemeElementCategory) findDescriptor(getCategories(), id);
    }	
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#findColor(java.lang.String)
     */
    public ColorDefinition findColor(String id) {
        return (ColorDefinition) findDescriptor(getColors(), id);
    }	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#find(java.lang.String)
	 */
	public IThemeDescriptor findTheme(String id) {
	    return (IThemeDescriptor) findDescriptor(getThemes(), id);
	}

	/**
     * @param descriptors
     * @param id
     * @return
     */
    private IThemeElementDefinition findDescriptor(IThemeElementDefinition [] descriptors, String id) {
        int idx =
			Arrays.binarySearch(
			        descriptors,
				    id,
				    ID_COMPARATOR);
		if (idx < 0)
			return null;
		return descriptors[idx];
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#getLookNFeels()
	 */
	public IThemeDescriptor [] getThemes() {
		int nSize = themes.size();
		IThemeDescriptor [] retArray = new IThemeDescriptor[nSize];
		themes.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#getLookNFeels()
	 */
	public ColorDefinition [] getColors() {
		int nSize = colors.size();
		ColorDefinition [] retArray = new ColorDefinition[nSize];
		colors.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
	}
	
	 /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getColorsFor(java.lang.String)
     */
    public ColorDefinition[] getColorsFor(String themeId) {
        ColorDefinition [] defs = getColors();
        if (themeId.equals(IThemeManager.DEFAULT_THEME)) 
            return defs;
        
        IThemeDescriptor desc = findTheme(themeId);
        ColorDefinition [] overrides = desc.getColors();
        return (ColorDefinition[]) overlay(defs, overrides);
    }	
    
	 /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getFontsFor(java.lang.String)
     */
    public FontDefinition[] getFontsFor(String themeId) {
        FontDefinition [] defs = getFonts();
        if (themeId.equals(IThemeManager.DEFAULT_THEME)) 
            return defs;
        
        IThemeDescriptor desc = findTheme(themeId);
        FontDefinition [] overrides = desc.getFonts();
        return (FontDefinition[]) overlay(defs, overrides);
    }	    
    
    /**
     * Overlay the overrides onto the base definitions.
     * 
     * @param defs the base definitions
     * @param overrides the overrides
     * @return the overlayed elements
     */
    private IThemeElementDefinition [] overlay(IThemeElementDefinition [] defs, IThemeElementDefinition [] overrides) {
        for (int i = 0; i < overrides.length; i++) {
            int idx = Arrays.binarySearch(defs, overrides[i], IThemeRegistry.ID_COMPARATOR);
            if (idx >= 0) {
                defs[idx] = overlay(defs[idx], overrides[i]); 
            }
        }
        return defs;
    }

    /**
     * Overlay the override onto the base definition.
     * 
     * @param defs the base definition
     * @param overrides the override
     * @return the overlayed element
     */
    private IThemeElementDefinition overlay(IThemeElementDefinition original, IThemeElementDefinition overlay) {
        if (original instanceof ColorDefinition) {
            ColorDefinition originalColor = (ColorDefinition) original;
            ColorDefinition overlayColor = (ColorDefinition) overlay;            
            return new ColorDefinition(originalColor, overlayColor.getValue());
        }
        else if (original instanceof FontDefinition){
            FontDefinition originalFont = (FontDefinition) original;
            FontDefinition overlayFont = (FontDefinition) overlay;            
            return new FontDefinition(originalFont, overlayFont.getValue());            
        }
        return null;
    }

    /**
     * @param definition
     */
    void add(FontDefinition definition) {
        fonts.add(definition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getGradients()
     */
    public FontDefinition [] getFonts() {
		int nSize = fonts.size();
		FontDefinition [] retArray = new FontDefinition[nSize];
		fonts.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#findFont(java.lang.String)
     */
    public FontDefinition findFont(String id) { 
        return (FontDefinition) findDescriptor(getFonts(), id);
    }

    /**
     * @param definition
     */
    void add(ThemeElementCategory definition) {
        categories.add(definition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getCategories()
     */
    public ThemeElementCategory [] getCategories() {
		int nSize = categories.size();
		ThemeElementCategory [] retArray = new ThemeElementCategory[nSize];
		categories.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
    }

    /**
     * @param name
     * @param value
     */
    void setData(String name, String value) {
        dataMap.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getData()
     */
    public Map getData() {
        return Collections.unmodifiableMap(dataMap);
    }
    
    /**
     * Add the data from another map to this data
     * 
     * @param otherData the other data to add
     */
    public void addData(Map otherData) {
        dataMap.putAll(otherData);
    }
}
