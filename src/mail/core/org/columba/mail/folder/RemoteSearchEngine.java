//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.folder;

import java.util.LinkedList;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.message.AbstractMessage;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RemoteSearchEngine extends AbstractSearchEngine {

	private final static String[] caps = {
		"Body", "Subject", "From", "To", "Cc",
		"Bcc", "Custom Headerfield", "Date", "Flags",
		"Priority", "Size"
	};


	//protected IMAPProtocol imap;

	protected IMAPRootFolder rootFolder;

	public RemoteSearchEngine(Folder folder) {
		super(folder);

		rootFolder = (IMAPRootFolder) ((IMAPFolder) folder).getRootFolder();
		//imap = rootFolder.getImapServerConnection();

	}

	protected String createSubjectString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		searchString.append("SUBJECT ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createToString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		searchString.append("TO ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createCcString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		searchString.append("CC ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createBccString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		searchString.append("BCC ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createFromString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		searchString.append("FROM ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createToCCString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");
	
		searchString.append("OR ");

		searchString.append("TO ");

		searchString.append(criteria.getPattern());
		
		searchString.append("CC ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createBodyString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		searchString.append("BODY ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createSizeString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		if (criteria.getCriteria() == FilterCriteria.SIZE_BIGGER)
			searchString.append("LARGER ");
		else
			searchString.append("SMALLER ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createDateString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		if (criteria.getCriteria() == FilterCriteria.DATE_BEFORE)
			searchString.append("SENTBEFORE ");
		else
			searchString.append("SENTAFTER ");

		searchString.append(criteria.getPattern());

		return searchString.toString();
	}

	protected String createFlagsString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		String headerField = criteria.getPattern();

		if (headerField.equalsIgnoreCase("Answered")) {
			searchString.append("ANSWERED ");
		} else if (headerField.equalsIgnoreCase("Deleted")) {
			searchString.append("DELETED ");
		} else if (headerField.equalsIgnoreCase("Flagged")) {
			searchString.append("FLAGGED ");
		} else if (headerField.equalsIgnoreCase("Recent")) {
			searchString.append("NEW ");
		} else if (headerField.equalsIgnoreCase("Draft")) {
			searchString.append("DRAFT ");
		} else if (headerField.equalsIgnoreCase("Seen")) {
			searchString.append("SEEN ");
		}

		return searchString.toString();
	}

	protected String createPriorityString(FilterCriteria criteria) {
		StringBuffer searchString = new StringBuffer();

		// we need to append "NOT"
		if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT)
			searchString.append("NOT ");

		searchString.append("X-Priority ");

		Integer searchPattern = null;
		String pattern = criteria.getPattern();
		if (pattern.equalsIgnoreCase("Highest")) {
			searchPattern = new Integer(1);
		} else if (pattern.equalsIgnoreCase("High")) {
			searchPattern = new Integer(2);
		} else if (pattern.equalsIgnoreCase("Normal")) {
			searchPattern = new Integer(3);
		} else if (pattern.equalsIgnoreCase("Low")) {
			searchPattern = new Integer(4);
		} else if (pattern.equalsIgnoreCase("Lowest")) {
			searchPattern = new Integer(5);
		}
		searchString.append(searchPattern.toString());

		return searchString.toString();
	}

	protected String generateSearchString(
		FilterRule rule,
		Vector ruleStringList) {
		StringBuffer searchString = new StringBuffer();

		if (rule.count() > 1) {

			int condition = rule.getConditionInt();
			String conditionString;
			if (condition == FilterRule.MATCH_ALL) {
				// match all
				conditionString = "OR";

			} else {
				// match any
				conditionString = "AND";
			}

			// concatenate all criteria together
			//  -> create one search-request string
			for (int i = 0; i < rule.count(); i++) {

				if (i != rule.count() - 1)
					searchString.append(conditionString + " ");

				searchString.append((String) ruleStringList.get(i));

				if (i != rule.count() - 1)
					searchString.append(" ");

			}
		} else {
			searchString.append((String) ruleStringList.get(0));
		}

		return searchString.toString();
	}
	
	protected String generateSearchString( FilterRule rule )
	{
		Vector ruleStringList = new Vector();

		for (int i = 0; i < rule.count(); i++) {
			FilterCriteria criteria = rule.get(i);
			String headerItem;
			//StringBuffer searchString = new StringBuffer();
			String searchString = null;

			switch (criteria.getHeaderItem()) {
				case FilterCriteria.SUBJECT :
					{
						searchString = createSubjectString(criteria);

						break;
					}
				case FilterCriteria.TO :
					{
						searchString = createToString(criteria);
						break;
					}
				case FilterCriteria.FROM :
					{
						searchString = createFromString(criteria);
						break;
					}
				case FilterCriteria.CC :
					{
						searchString = createCcString(criteria);
						break;
					}
				case FilterCriteria.BCC :
					{
						searchString = createBccString(criteria);
						break;
					}
				case FilterCriteria.TO_CC :
					{
						searchString = createToString(criteria);

						break;
					}
				case FilterCriteria.BODY :
					{
						searchString = createBodyString(criteria);
						break;
					}
				case FilterCriteria.SIZE :
					{
						searchString = createSizeString(criteria);

						break;
					}
				case FilterCriteria.DATE :
					{
						searchString = createDateString(criteria);

						break;
					}
				case FilterCriteria.FLAGS :
					{
						searchString = createFlagsString(criteria);

						break;
					}
				case FilterCriteria.PRIORITY :
					{
						searchString = createPriorityString(criteria);

						break;
					}

			}
			ruleStringList.add(searchString.toString());
		}

		String searchString = generateSearchString(rule, ruleStringList);

		/*
		if (searchString.length() == 0)
			searchString =
				"1:* OR HEADER SUBJECT test OR HEADER FROM fdietz@gmx.de HEADER FROM freddy@uni-mannheim.de";
		*/
		
		
		ColumbaLogger.log.info("searchString=" + searchString.toString());
		
		return searchString;
	}
	
	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageAdded(org.columba.mail.message.AbstractMessage)
	 */
	public void messageAdded(AbstractMessage message) {
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageRemoved(java.lang.Object)
	 */
	public void messageRemoved(Object uid) {
	}

	/**
	 * @see org.columba.mail.folder.AbstractSearchEngine#getCaps()
	 */
	public String[] getCaps() {
		return caps;
	}

	/**
	 * @see org.columba.mail.folder.AbstractSearchEngine#queryEngine(org.columba.mail.filter.FilterRule, java.lang.Object)
	 */
	protected LinkedList queryEngine(FilterRule filter, Object[] uids, WorkerStatusController worker)
		throws Exception {
			return ((IMAPFolder) folder)
				.getStore()
				.search(uids, generateSearchString( filter), ((IMAPFolder) folder).getImapPath(), worker);
	}

	/**
	 * @see org.columba.mail.folder.AbstractSearchEngine#queryEngine(org.columba.mail.filter.FilterRule, org.columba.core.command.WorkerStatusController)
	 */
	protected LinkedList queryEngine(
		FilterRule filter,
		WorkerStatusController worker)
		throws Exception {
			return ((IMAPFolder) folder)
			.getStore()
			.search(generateSearchString( filter) , ((IMAPFolder) folder).getImapPath(), worker);
	}

}
