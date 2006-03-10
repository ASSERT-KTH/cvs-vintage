package org.columba.mail.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import org.columba.api.command.IStatusObservable;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.filter.FilterRule;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.ImapItem;
import org.columba.mail.folder.headercache.MemoryHeaderList;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.imap.IMAPException;
import org.columba.ristretto.imap.IMAPFlags;
import org.columba.ristretto.imap.ListInfo;
import org.columba.ristretto.imap.MailboxStatus;
import org.columba.ristretto.imap.NamespaceCollection;
import org.columba.ristretto.imap.SearchKey;
import org.columba.ristretto.imap.SequenceSet;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MailboxInfo;
import org.columba.ristretto.message.MimeTree;

public class TestServer implements IImapServer {

	
	
	private MailboxStatus status;
	private IHeaderList headerList;
	private ArrayList<IColumbaHeader> indices;
	
	
	public TestServer(MailboxStatus status) {
		super();
		this.status = status;
		indices = new ArrayList<IColumbaHeader>();
		headerList = new MemoryHeaderList();
	}
	
	public void addHeader(IColumbaHeader h, Object uid) {
		headerList.add(h, uid);
		indices.add(h);		
	}
	
	public IColumbaHeader removeHeader(int index) {
		IColumbaHeader h = indices.remove(index-1);
		headerList.remove(h.get("columba.uid"));
		
		return h;
	}
	
	public String getDelimiter() throws IOException, IMAPException,
			CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public void logout() throws Exception {
		// TODO Auto-generated method stub

	}

	public List checkSupportedAuthenticationMethods() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSupported(String command) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFirstLoginAction(IFirstLoginAction action) {
		// TODO Auto-generated method stub

	}

	public void ensureSelectedState(IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public MailboxStatus getStatus(IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException {
		return status;
	}

	public ListInfo[] list(String reference, String pattern) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer append(InputStream messageSource, IMAPFlags flags,
			IMAPFolder folder) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer append(InputStream messageSource, IMAPFolder folder)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void createMailbox(String mailboxName, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public void deleteFolder(String path) throws Exception {
		// TODO Auto-generated method stub

	}

	public void renameFolder(String oldMailboxName, String newMailboxName)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public void subscribeFolder(String mailboxName) throws IOException,
			IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public void unsubscribeFolder(String mailboxName) throws IOException,
			IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public void expunge(IMAPFolder folder) throws IOException, IMAPException,
			CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public Integer[] copy(IMAPFolder destFolder, Object[] uids,
			IMAPFolder folder) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public int fetchUid(SequenceSet set, IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException {
		
		//if(set.toString().equals("*")) {
		return (Integer)indices.get(indices.size()-1).get("columba.uid");
	}

	public Integer[] fetchUids(SequenceSet set, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		
		
		int[] idx = set.toArray(indices.size());
		Integer[] result = new Integer[idx.length];

		int pos = 0;
		for(  int i: idx) {
			result[pos++] = (Integer)indices.get(i-1).get("columba.uid");
		}
		
		return result;
	}

	public IMAPFlags[] fetchFlagsListStartFrom(int startIdx, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public IMAPFlags[] fetchFlagsListStartFrom2(int startIdx, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		
		int size = indices.size() - startIdx + 1;
		IMAPFlags[] result = new IMAPFlags[size];
		
		for(int i=0;i<size;i++) {
			IColumbaHeader h = indices.get(startIdx-1+i);
			IMAPFlags flags = new IMAPFlags(h.getFlags().getFlags());
			flags.setUid(h.get("columba.uid"));
			
			result[i] = flags;
		}		
		
		return result;
	}

	public NamespaceCollection fetchNamespaces() throws IOException,
			IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public void fetchHeaderList(IHeaderList resultList, List list,
			IMAPFolder folder) throws Exception {
		
		for(Object uid: list) {
			resultList.add(headerList.get(uid), uid);
		}
	}

	public MimeTree getMimeTree(Object uid, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getMimePartBodyStream(Object uid, Integer[] address,
			IMAPFolder folder) throws IOException, IMAPException,
			CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public Header getHeaders(Object uid, String[] keys, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public Header getAllHeaders(Object uid, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getMimePartSourceStream(Object uid, Integer[] address,
			IMAPFolder folder) throws IOException, IMAPException,
			CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getMessageSourceStream(Object uid, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public void markMessage(Object[] uids, int variant, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public void setFlags(Object[] uids, IMAPFlags flags, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub

	}

	public List search(Object[] uids, FilterRule filterRule, IMAPFolder folder)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public int getIndex(Integer uid, IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException {
		int pos = 0;
		for( IColumbaHeader h: indices) {
			if(h.get("columba.uid").equals(uid)) {
				return pos+1;
			}
			pos ++;
		}
		
		return -1;
	}

	public Integer[] search(SearchKey key, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		return new Integer[0];
	}

	public List search(FilterRule filterRule, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		
		return new ArrayList();
	}

	public MailboxInfo getMessageFolderInfo(IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public ListInfo[] fetchSubscribedFolders() throws IOException,
			IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSelected(IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException {
		// TODO Auto-generated method stub
		return false;
	}

	public void alertMessage(String arg0) {
		// TODO Auto-generated method stub

	}

	public void connectionClosed(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void existsChanged(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void flagsChanged(String arg0, IMAPFlags arg1) {
		// TODO Auto-generated method stub

	}

	public void parseError(String arg0) {
		// TODO Auto-generated method stub

	}

	public void recentChanged(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void warningMessage(String arg0) {
		// TODO Auto-generated method stub

	}

	public ImapItem getItem() {
		return new ImapItem(new XmlElement());
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

	public void setExistsChangedAction(IExistsChangedAction existsChangedAction) {
		// TODO Auto-generated method stub

	}

	public void setUpdateFlagAction(IUpdateFlagAction updateFlagAction) {
		// TODO Auto-generated method stub

	}

	public void setObservable(IStatusObservable observable) {
		// TODO Auto-generated method stub

	}

	public void setStatus(MailboxStatus status) {
		this.status = status;
	}

	public IHeaderList getHeaderList() {
		return headerList;
	}

}
