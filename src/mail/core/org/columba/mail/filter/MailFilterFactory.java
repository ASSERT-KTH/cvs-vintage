// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.filter;

/**
 * @author fdietz
 *  
 */
public class MailFilterFactory {

	private MailFilterFactory() {
	}

	public static MailFilterCriteria createSubjectContains(String pattern) {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.SUBJECT);
		c.setCriteria(MailFilterCriteria.CONTAINS);
		c.setPatternString(pattern);
		c.setHeaderfieldString("Subject");

		return c;
	}

	public static MailFilterCriteria createFromContains(String pattern) {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.FROM);
		c.setCriteria(MailFilterCriteria.CONTAINS);
		c.setPatternString(pattern);
		c.setHeaderfieldString("From");

		return c;
	}

	public static MailFilterCriteria createToContains(String pattern) {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.TO);
		c.setCriteria(MailFilterCriteria.CONTAINS);
		c.setPatternString(pattern);
		c.setHeaderfieldString("To");

		return c;
	}

	public static MailFilterCriteria createCcContains(String pattern) {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.CC);
		c.setCriteria(MailFilterCriteria.CONTAINS);
		c.setPatternString(pattern);
		c.setHeaderfieldString("Cc");

		return c;
	}

	public static MailFilterCriteria createBccContains(String pattern) {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.BCC);
		c.setCriteria(MailFilterCriteria.CONTAINS);
		c.setPatternString(pattern);
		c.setHeaderfieldString("Bcc");

		return c;
	}

	public static MailFilterCriteria createBodyContains(String pattern) {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.BODY);
		c.setCriteria(MailFilterCriteria.CONTAINS);
		c.setPatternString(pattern);

		return c;
	}

	public static MailFilterCriteria createUnreadMessages() {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.FLAGS);
		c.setCriteria(MailFilterCriteria.IS_NOT);
		c.setPatternString("Seen");

		return c;
	}

	public static MailFilterCriteria createFlaggedMessages() {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.FLAGS);
		c.setCriteria(MailFilterCriteria.IS);
		c.setPatternString("Flagged");

		return c;
	}
	
	public static MailFilterCriteria createSpamMessages() {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.FLAGS);
		c.setCriteria(MailFilterCriteria.IS);
		c.setPatternString("Spam");

		return c;
	}

	public static MailFilterCriteria createHighestPriority() {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.PRIORITY);
		c.setCriteria(MailFilterCriteria.IS);
		c.setPatternString("Highest");

		return c;
	}

	public static MailFilterCriteria createHighPriority() {
		MailFilterCriteria c = new MailFilterCriteria();
		c.setType(MailFilterCriteria.PRIORITY);
		c.setCriteria(MailFilterCriteria.IS);
		c.setPatternString("High");

		return c;
	}
}