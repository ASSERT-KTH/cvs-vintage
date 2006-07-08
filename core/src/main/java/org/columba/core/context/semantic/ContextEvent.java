package org.columba.core.context.semantic;

import java.util.EventObject;

import org.columba.core.context.base.api.IStructureValue;
import org.columba.core.context.semantic.api.IContextEvent;

public class ContextEvent extends EventObject implements IContextEvent {

	private Object source;

	private IStructureValue value;

	public ContextEvent(Object source, IStructureValue value) {
		super(source);

		this.value = value;
	}

	public IStructureValue getValue() {
		return value;
	}

}
