package org.tigris.scarab.tools;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */

import java.util.Collections;
import java.util.List;

import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.Log;

/**
 * Currently significant amounts of business logic is stored in the
 * ScarabRequestTool and ScarabGlobalTool. This isn't very accesible b/c the
 * tools make assumptions about having a rundata object and running in them..
 * 
 * This class will slowly take out the business logic from the tools so it can
 * be unit tested and reused in more situations.
 */
public class ScarabToolManager {
	private ScarabLocalizationTool l10n;

	public ScarabToolManager(ScarabLocalizationTool l10n) {
		this.l10n = l10n;
	}

	/**
	 * Get reason for modification.
	 */
	public String getActivityReason(ActivitySet activitySet, Activity activity)
			throws Exception {
		String reason = null;
		Attachment attachment = activitySet.getAttachment();
		if (attachment != null) {
			String data = attachment.getData();
			// Reason is the attachment entered for this transaction
			if (data != null && data.length() > 0) {
				reason = data;
			} else {
				reason = l10n.get(L10NKeySet.NotProvided);
			}
		}
		// No reasons given for initial issue entry
		else if (activitySet.getTypeId().equals(
				ActivitySetTypePeer.CREATE_ISSUE__PK)) {
			reason = l10n.get(L10NKeySet.InitialEntry);
		} else {
			reason = l10n.get(L10NKeySet.NotProvided);
		}
		return reason;
	}

	/**
	 * First attempts to get the RModuleUserAttributes from the user. If it is
	 * empty, then it will try to get the defaults from the module. If anything
	 * fails, it will return an empty list.
	 */
	public List getRModuleUserAttributes(ScarabUser user, Module module, IssueType issueType) {
		List issueListColumns = null;
		try {
			//
			// First check whether an MIT list is currently
			// active and if so, whether it has attributes
			// associated with it.
			//
			MITList currentList = user.getCurrentMITList();
			if (currentList != null) {
				//
				// Here we fetch the collection of attributes
				// associated with the current MIT list.
				//
				issueListColumns = currentList.getCommonRModuleUserAttributes();

				//
				// If there are no attributes associated with
				// the list, and the list only contains a single
				// module and a single issue type, get the default
				// attributes for that combination of module and
				// issue type.
				//
				if (issueListColumns.isEmpty()
						&& currentList.isSingleModuleIssueType()) {
					issueListColumns = currentList.getModule()
							.getDefaultRModuleUserAttributes(
									currentList.getIssueType());
				}
			}

			if (issueListColumns == null) {
				issueListColumns = user.getRModuleUserAttributes(module,
						issueType);
				if (issueListColumns.isEmpty()) {
					issueListColumns = module
							.getDefaultRModuleUserAttributes(issueType);
				}
			}
			if (issueListColumns == null)
	        {
	            issueListColumns = Collections.EMPTY_LIST;
	        }

		} catch (Exception e) {
			Log.get().error("Could not get list attributes", e);
		}

		return issueListColumns;
	}

}

