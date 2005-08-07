package org.columba.api.command;

public interface ICommand {

	public abstract void updateGUI() throws Exception;

	/**
	 * Command must implement this method Executes the Command when run the
	 * first time
	 * 
	 * @param worker
	 * @throws Exception
	 */
	public abstract void execute(IWorkerStatusController worker)
			throws Exception;

	public abstract ICommandReference getReference();

}