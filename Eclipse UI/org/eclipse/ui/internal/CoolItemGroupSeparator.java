/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Separator;

/**
 */
public class CoolItemGroupSeparator extends Separator implements ICoolItemGroup {
	private String contributingId;
	private String beforeGroupId;
	
	public CoolItemGroupSeparator(String groupName, String actionSetId) {
		this(groupName, actionSetId, null);
	}
	public CoolItemGroupSeparator(String groupName, String contributingId, String beforeGroupId) {
		super(groupName);
		this.contributingId = contributingId;
		this.beforeGroupId = beforeGroupId;
	}
	/**
	 */
	public String getContributingId() {
		return contributingId;
	}
	/**
	 * Returns the before group id.
	 */
	public String getBeforeGroupId() {
		return beforeGroupId;
	}
}
