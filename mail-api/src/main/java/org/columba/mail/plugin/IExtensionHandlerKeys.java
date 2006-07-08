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
package org.columba.mail.plugin;

public interface IExtensionHandlerKeys {

	/**
	 * Message Filter Action 
	 */
	public static final String ORG_COLUMBA_MAIL_FILTERACTION = "org.columba.mail.filteraction";
	
	/**
	 * UI Component for Message Filter Action
	 */
	public static final String ORG_COLUMBA_MAIL_FILTERACTIONUI = "org.columba.mail.filteractionui";
	
	/**
	 * Message Filter
	 */
	public static final String ORG_COLUMBA_MAIL_FILTER = "org.columba.mail.filter";
	
	/**
	 * UI Component for Message Filter
	 */
	public static final String ORG_COLUMBA_MAIL_FILTERUI = "org.columba.mail.filterui";
	
	/**
	 * Message Folders
	 */
	public static final String ORG_COLUMBA_MAIL_FOLDER = "org.columba.mail.folder";
	
	/**
	 * Message Folder Options
	 */
	public static final String ORG_COLUMBA_MAIL_FOLDEROPTIONS = "org.columba.mail.folderoptions";
	
	/**
	 * Message import filter.
	 */
	public static final String ORG_COLUMBA_MAIL_IMPORT = "org.columba.mail.import";
	
	/**
	 * Spam Filter Component
	 */
	public static final String ORG_COLUMBA_MAIL_SPAM = "org.columba.mail.spam";
	
	/**
	 * Column renderer for the message list
	 */
	public static final String ORG_COLUMBA_MAIL_TABLERENDERER = "org.columba.mail.tablerenderer";

	/**
	 * Handler for executing custom actions on attachment contents. 
	 * <p>
	 * Extensions retrieve the <code>File</code> containing attachment content and attachment metadata.
	 */
	public static final String ORG_COLUMBA_ATTACHMENT_HANDLER = "org.columba.mail.attachment.handler";
	
	
}
