package org.columba.api.backgroundtask;

/**
 * This manager runs in background.
 * <p>
 * If the user doesn't do anything with Columba, it starts some cleanup
 * workers, like saving configuration, saving header-cache, etc.
 *
 * @author fdietz
 */
public interface IBackgroundTaskManager {
	
	/**
	 * Register new background task.
	 * 
	 * @param runnable	background task
	 */
	public abstract void register(Runnable runnable);
}