package org.columba.mail.folder;

import org.columba.ristretto.message.MailboxInfo;

public class ColumbaMailboxInfo implements IMailboxInfo {

	private MailboxInfo mailboxInfo;
	
	public ColumbaMailboxInfo(MailboxInfo info) {
		this.mailboxInfo = info;
	}
	
	public ColumbaMailboxInfo() {
		this.mailboxInfo = new MailboxInfo();
	}
	
	public int getExists() {
		return mailboxInfo.getExists();
	}

	public void setExists(int v) {
		mailboxInfo.setExists(v);

	}

	public void incExists() throws MailboxInfoInvalidException {
		mailboxInfo.setExists(mailboxInfo.getExists()+1);
		sanityCheck();
	}

	public void decExists() throws MailboxInfoInvalidException {
		mailboxInfo.setExists(mailboxInfo.getExists()-1);
		sanityCheck();
	}

	public void setRecent(int v) {
		mailboxInfo.setRecent(v);
	}

	public int getRecent() {
		return mailboxInfo.getRecent();
	}

	public void incRecent() throws MailboxInfoInvalidException {
		mailboxInfo.setRecent(mailboxInfo.getRecent()+1);
		sanityCheck();
	}

	public void decRecent() throws MailboxInfoInvalidException {
		mailboxInfo.setRecent(mailboxInfo.getRecent()-1);
		sanityCheck();
	}

	public void setUnseen(int v) {
		mailboxInfo.setUnseen(v);
	}

	public int getUnseen() {
		return mailboxInfo.getUnseen();
	}

	public void incUnseen() throws MailboxInfoInvalidException {
		mailboxInfo.setUnseen(mailboxInfo.getUnseen()+1);
		sanityCheck();
	}

	public void decUnseen() throws MailboxInfoInvalidException {
		mailboxInfo.setUnseen(mailboxInfo.getUnseen()-1);
		sanityCheck();
	}

	public void reset() {
		mailboxInfo.reset();
	}

	public void setUidNext(int v) {
		mailboxInfo.setUidNext(v);
	}

	public void setUidValidity(int v) {
		mailboxInfo.setUidValidity(v);
	}

	public int getUidNext() {
		return mailboxInfo.getUidNext();
	}

	public int getUidValidity() {
		return mailboxInfo.getUidValidity();
	}

    private void sanityCheck() throws MailboxInfoInvalidException {
    	if( !isSane() ) throw new MailboxInfoInvalidException(); 
    }

	public boolean isSane() {
		// Sanity checks
		if( mailboxInfo.getExists() < 0) return false;
		
		if( mailboxInfo.getRecent() < 0) return false;
		
		if( mailboxInfo.getRecent() > mailboxInfo.getExists()) return false;
		
		if( mailboxInfo.getUnseen() < 0) return false;
		
		if( mailboxInfo.getUnseen() > mailboxInfo.getExists()) return false;
		
		return true;
	}

    
	
}
