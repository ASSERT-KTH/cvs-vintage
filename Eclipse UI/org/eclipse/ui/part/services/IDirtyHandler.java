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
package org.eclipse.ui.part.services;

/**
 * Parts can take an IDirtyHandler in their constructor in order to set or clear 
 * their dirty state.
 * 
 * @since 3.1
 */
public interface IDirtyHandler {
    /**
     * Sets the new dirty state
     *
     * @param isDirty true iff the part is dirty
     */
    public void setDirty(boolean isDirty);
}
