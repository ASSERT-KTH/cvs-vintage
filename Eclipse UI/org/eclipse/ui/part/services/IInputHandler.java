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

import org.eclipse.ui.IEditorInput;

/**
 * Service that allows an editor to change its input.
 */
public interface IInputHandler {
    /**
     * Changes the current input for an editor
     *
     * @param input new input
     */
    public void setEditorInput(IEditorInput input);
}
