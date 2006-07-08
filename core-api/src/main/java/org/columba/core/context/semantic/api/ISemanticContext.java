package org.columba.core.context.semantic.api;

import org.columba.core.context.base.api.IStructureType;
import org.columba.core.context.base.api.IStructureValue;

public interface ISemanticContext {

	public static final String CONTEXT_ATTR_EMAIL_ADDRESS = "emailAddress";
	public static final String CONTEXT_ATTR_WEBSITE = "website";
	public static final String CONTEXT_ATTR_LAST_NAME = "lastName";
	public static final String CONTEXT_ATTR_FIRST_NAME = "firstName";
	public static final String CONTEXT_ATTR_DISPLAY_NAME = "displayName";
	public static final String CONTEXT_NAMESPACE_CORE = "org.columba.core";
	public static final String CONTEXT_NODE_IDENTITY = "identity";
	public static final String CONTEXT_ATTR_SELECTED_BODYTEXT = "selectedBodytext";
	public static final String CONTEXT_ATTR_BODY_TEXT = "bodyText";
	public static final String CONTEXT_NODE_MESSAGE_RECIPIENTS = "recipients";
	public static final String CONTEXT_ATTR_SENDER = "sender";
	public static final String CONTEXT_ATTR_SUBJECT = "subject";
	public static final String CONTEXT_ATTR_DATE = "date";
	
	public static final String CONTEXT_NODE_MESSAGE = "message";
	public static final String CONTEXT_ATTR_TIME_ZONE = "timeZone";
	public static final String CONTEXT_NODE_DATE_TIME = "dateTime";
	public IStructureType getType();
	
	public IStructureValue getValue();
	public void setValue(IStructureValue value);
	public IStructureValue createValue();
	
	public void addContextListener(IContextListener l);
	public void removeContextListener(IContextListener l);
}
