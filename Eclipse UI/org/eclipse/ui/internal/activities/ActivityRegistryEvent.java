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

package org.eclipse.ui.internal.activities;

final class ActivityRegistryEvent {
    private IActivityRegistry activityRegistry;

    ActivityRegistryEvent(IActivityRegistry activityRegistry) {
        if (activityRegistry == null)
            throw new NullPointerException();

        this.activityRegistry = activityRegistry;
    }

    IActivityRegistry getActivityRegistry() {
        return activityRegistry;
    }
}
