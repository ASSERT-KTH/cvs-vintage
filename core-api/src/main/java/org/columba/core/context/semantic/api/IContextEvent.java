package org.columba.core.context.semantic.api;

import org.columba.core.context.base.api.IStructureValue;

public interface IContextEvent {

	public Object getSource();
	
	public IStructureValue getValue();
}
