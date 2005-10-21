package org.columba.addressbook.model;

public interface IEmailModel {

	/**
	 * @return Returns the address.
	 */
	public abstract String getAddress();

	/**
	 * @return Returns the type.
	 */
	public abstract int getType();

	public abstract String getTypeString();

}