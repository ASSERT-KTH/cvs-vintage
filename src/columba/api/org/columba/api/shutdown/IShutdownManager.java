package org.columba.api.shutdown;

public interface IShutdownManager {

	/**
	 * Registers a runnable plugin that should be executed on shutdown.
	 */
	public abstract void register(Runnable plugin);

	/**
	 * Starts the shutdown procedure.
	 */
	public abstract void shutdown(final int status);

}