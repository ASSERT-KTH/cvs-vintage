/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.registry.ActionSetCategory;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

/**
 * This is used to sort action sets in the perspective customization dialog.
 */
public class ActionSetSorter extends ViewerSorter {

    /**
     * Creates a new sorter.
     */
    public ActionSetSorter() {
    }

    /**
     * Returns a negative, zero, or positive number depending on whether
     * the first element is less than, equal to, or greater than
     * the second element.
     */
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof IActionSetDescriptor) {
            String str1 = DialogUtil.removeAccel(((IActionSetDescriptor) e1)
                    .getLabel());
            String str2 = DialogUtil.removeAccel(((IActionSetDescriptor) e2)
                    .getLabel());
            return collator.compare(str1, str2);
        } else if (e1 instanceof ActionSetCategory) {
            ActionSetCategory cat1 = (ActionSetCategory) e1;
            ActionSetCategory cat2 = (ActionSetCategory) e2;
            if (cat1.getId().equals(ActionSetRegistry.OTHER_CATEGORY))
                return 1;
            if (cat2.getId().equals(ActionSetRegistry.OTHER_CATEGORY))
                return -1;
            String str1 = cat1.getLabel();
            String str2 = cat2.getLabel();
            return collator.compare(str1, str2);
        }
        return 0;
    }
}
