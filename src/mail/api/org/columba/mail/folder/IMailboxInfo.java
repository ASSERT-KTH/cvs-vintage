package org.columba.mail.folder;

public interface IMailboxInfo {
	
	public int getExists(); 

	public void setExists(int v); 

	
	public void incExists() throws MailboxInfoInvalidException; 

	public void decExists() throws MailboxInfoInvalidException; 


	public void setRecent(int v); 

	public int getRecent(); 

	
	public void incRecent() throws MailboxInfoInvalidException; 

	public void decRecent() throws MailboxInfoInvalidException; 


	public void setUnseen(int v); 

	public int getUnseen(); 

	
	public void incUnseen() throws MailboxInfoInvalidException; 

	public void decUnseen() throws MailboxInfoInvalidException;
	
	public void reset();
	
	public int getUidNext();

	public void setUidNext(int v);

	
	public int getUidValidity();

	public void setUidValidity(int v);

	public boolean isSane();
	
}
