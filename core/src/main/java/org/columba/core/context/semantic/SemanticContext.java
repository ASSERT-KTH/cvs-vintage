package org.columba.core.context.semantic;

import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

import org.columba.core.context.base.api.IAttributeType;
import org.columba.core.context.base.api.IStructureType;
import org.columba.core.context.base.api.IStructureValue;
import org.columba.core.context.base.api.MULTIPLICITY;
import org.columba.core.context.base.api.IAttributeType.BASETYPE;
import org.columba.core.context.semantic.api.IContextEvent;
import org.columba.core.context.semantic.api.IContextListener;
import org.columba.core.context.semantic.api.ISemanticContext;
import org.columba.core.main.MainInterface;

public class SemanticContext implements ISemanticContext {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.context.semantic.SemanticContext");

	protected EventListenerList listenerList = new EventListenerList();

	private IStructureType type;

	private IStructureValue value;

	public SemanticContext() {
		super();

		initContext();
	}

	// initialize context
	private void initContext() {

		// <context>
		// <core>
		// <identity>
		// </identity>
		// <datetime>
		// </datetime>
		// </core>
		// </context>
		type = MainInterface.contextFactory.createStructure("context",
				ISemanticContext.CONTEXT_NAMESPACE_CORE);

		// identity definition
		IStructureType identity = type.addChild(ISemanticContext.CONTEXT_NODE_IDENTITY, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		// MULTIPLICITY.ZERO_TO_ONE is default
		IAttributeType emailAddress = identity.addAttribute(ISemanticContext.CONTEXT_ATTR_EMAIL_ADDRESS,
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		identity.addAttribute(ISemanticContext.CONTEXT_ATTR_DISPLAY_NAME, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		identity.addAttribute(ISemanticContext.CONTEXT_ATTR_FIRST_NAME, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		identity.addAttribute(ISemanticContext.CONTEXT_ATTR_LAST_NAME, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		identity.addAttribute(ISemanticContext.CONTEXT_ATTR_WEBSITE, ISemanticContext.CONTEXT_NAMESPACE_CORE);

		// date time timezone definition
		IStructureType dateTime = type.addChild(ISemanticContext.CONTEXT_NODE_DATE_TIME, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		IAttributeType date = dateTime.addAttribute(ISemanticContext.CONTEXT_ATTR_DATE, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		IAttributeType timeZone = dateTime.addAttribute(ISemanticContext.CONTEXT_ATTR_TIME_ZONE,
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		date.setBaseType(BASETYPE.DATE);

		// date range (start time, end time) definition
		IStructureType dateRange = type.addChild("dateRange",
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		IAttributeType startStart = dateRange.addAttribute("startDate",
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		startStart.setBaseType(BASETYPE.DATE);
		IAttributeType endDate = dateRange.addAttribute("endDate",
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		endDate.setBaseType(BASETYPE.DATE);

		// document definition
		IStructureType document = type.addChild("document", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		document.addAttribute("author", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		document.addAttribute("title", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		document.addAttribute("summary", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		document.addAttribute("body", ISemanticContext.CONTEXT_NAMESPACE_CORE);

		// locale definition
		IStructureType locale = type.addChild("locale", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		locale.addAttribute("language", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		locale.addAttribute("country", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		locale.addAttribute("variant", ISemanticContext.CONTEXT_NAMESPACE_CORE);

		// list of attachments
		IStructureType attachmentList = type.addChild("attachmentList",
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		IStructureType attachment = attachmentList.addChild("attachment",
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		attachment.setCardinality(MULTIPLICITY.ZERO_TO_MANY);
		// single attachment
		attachment.addAttribute("name", ISemanticContext.CONTEXT_NAMESPACE_CORE);
		IAttributeType contentType = attachment.addAttribute("content",
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		contentType.setBaseType(BASETYPE.BINARY);

		// message
		IStructureType message = type.addChild(ISemanticContext.CONTEXT_NODE_MESSAGE, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		message.addAttribute(ISemanticContext.CONTEXT_ATTR_SUBJECT, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		// single sender - re-use identity type
		IStructureType sender = message.addChild(ISemanticContext.CONTEXT_ATTR_SENDER, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		sender.addChild(identity);
		sender.setCardinality(MULTIPLICITY.ONE_TO_ONE);
		// re-use identity type for recipient list
		IStructureType recipients = message.addChild(ISemanticContext.CONTEXT_NODE_MESSAGE_RECIPIENTS,
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		recipients.setCardinality(MULTIPLICITY.ZERO_TO_MANY);
		recipients.addChild(identity);
		// message body
		message.addAttribute(ISemanticContext.CONTEXT_ATTR_BODY_TEXT, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		message.addAttribute(ISemanticContext.CONTEXT_ATTR_SELECTED_BODYTEXT, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		IAttributeType arrivalDate = message.addAttribute(ISemanticContext.CONTEXT_ATTR_DATE, ISemanticContext.CONTEXT_NAMESPACE_CORE);
		arrivalDate.setBaseType(BASETYPE.DATE);
		// message contains list of attachments
		message.addChild(attachmentList);

	}

	public IStructureValue createValue() {
		value = MainInterface.contextFactory.createValue("context",
				ISemanticContext.CONTEXT_NAMESPACE_CORE, type);

		return value;
	}

	public IStructureType getType() {
		return this.type;
	}

	public synchronized IStructureValue getValue() {
		return this.value;
	}

	public synchronized void setValue(IStructureValue value) {
		this.value = value;
		
		// notify all listeners
		fireContextChangedEvent(value);
	}
	
	public void addContextListener(IContextListener l) {
		listenerList.add(IContextListener.class, l);

	}

	public void removeContextListener(IContextListener l) {
		listenerList.remove(IContextListener.class, l);
	}

	protected void fireContextChangedEvent(IStructureValue value) {

		IContextEvent e = new ContextEvent(this, value);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IContextListener.class) {
				((IContextListener) listeners[i + 1]).contextChanged(e);
			}
		}
	}

	

}
