/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Interface for reusable editors. 
 * 
 * An editors may support changing its input so that 
 * the workbench may change its contents instead of 
 * opening a new editor.
 */
public interface IReusableEditor extends IEditorPart {
    /**
     * Sets the input to this editor.
     *
     * @param input the editor input
     */
    public void setInput(IEditorInput input);
}

