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
package org.eclipse.ui;

import org.eclipse.core.resources.IMarker;
 
/** 
 * Creates resolutions for a given marker. 
 * When run, a resolution would typically eliminate 
 * the need for the marker.
 * 
 * @since 2.0
 */
public interface IMarkerResolutionGenerator {
    /** 
     * Returns resolutions for the given marker (may
     * be empty). 
     * 
     * @return resolutions for the given marker
     */ 
    public IMarkerResolution[] getResolutions(IMarker marker); 
}

