package org.columba.core.command;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class CommandCancelledException extends Exception {

	/**
	 * Constructor for CommandCancelledException.
	 */
	public CommandCancelledException() {
		super();
	}

	/**
	 * Constructor for CommandCancelledException.
	 * @param message
	 */
	public CommandCancelledException(String message) {
		super(message);
	}

	/**
	 * Constructor for CommandCancelledException.
	 * @param message
	 * @param cause
	 */
	public CommandCancelledException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for CommandCancelledException.
	 * @param cause
	 */
	public CommandCancelledException(Throwable cause) {
		super(cause);
	}

}
