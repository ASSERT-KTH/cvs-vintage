/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.jobs.Job;

/**
 * The IJobBusyListener is used to listen for running and 
 * terminating of jobs of a particular family.
 */
interface IJobBusyListener {

    /**
     * Increment the busy count for job.
     * @param job
     */
    public void incrementBusy(Job job);

    /**
     * Decrement the busy count for job.
     * @param job
     */
    public void decrementBusy(Job job);

}