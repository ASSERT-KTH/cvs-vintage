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
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuMoveEditor extends SystemMenuMove {

    public SystemMenuMoveEditor(IPresentablePart presentablePart,
            IStackPresentationSite stackPresentationSite) {
        super(presentablePart, stackPresentationSite);
    }

    public void fill(Menu menu, int index) {
        fill(menu, index, "EditorPane.moveEditor"); //$NON-NLS-1$ 
    }
}