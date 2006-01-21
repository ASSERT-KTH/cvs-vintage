package org.columba.core.gui.frame.event;

import java.util.EventObject;

public class FrameEvent extends EventObject {

	private String text;

	private boolean visible;

	public FrameEvent(Object source) {
		super(source);
	}
	
	public FrameEvent(Object source, String text) {
		this(source);

		this.text = text;
	}

	public FrameEvent(Object source, boolean visible) {
		this(source);

		this.visible = visible;
	}

	public String getText() {
		return text;
	}

	public boolean isVisible() {
		return visible;
	}

}
