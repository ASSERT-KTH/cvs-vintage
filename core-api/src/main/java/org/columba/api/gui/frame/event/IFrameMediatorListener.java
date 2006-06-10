package org.columba.api.gui.frame.event;

import java.util.EventListener;

public interface IFrameMediatorListener extends EventListener {

	public abstract void titleChanged(FrameEvent event);
	public abstract void statusMessageChanged(FrameEvent event);
	public abstract void taskStatusChanged(FrameEvent event);
	public abstract void visibilityChanged(FrameEvent event);
	public abstract void layoutChanged(FrameEvent event);
	public abstract void closed(FrameEvent event);
	public abstract void toolBarVisibilityChanged(FrameEvent event);
	public abstract void switchedComponent(FrameEvent event);
}

