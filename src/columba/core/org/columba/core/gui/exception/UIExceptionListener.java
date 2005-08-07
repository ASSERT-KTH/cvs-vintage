package org.columba.core.gui.exception;

import java.beans.ExceptionListener;

public class UIExceptionListener implements ExceptionListener {

	public UIExceptionListener() {
		super();
	}

	public void exceptionThrown(Exception e) {
		// process exception -> show error dialog to user
		new ExceptionHandler().processException(e);
	}

}
