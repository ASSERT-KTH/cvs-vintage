//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.folder.search;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.util.ListTools;
import org.columba.core.util.Mutex;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.MimePart;
import org.columba.mail.parser.Rfc822Parser;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LuceneSearchEngine
	extends AbstractSearchEngine
	implements TaskInterface {

	private final static int OPTIMIZE_AFTER_N_OPERATIONS = 30;

	File indexDir;

	IndexReader fileIndexReader;
	IndexReader ramIndexReader;

	Directory luceneIndexDir;
	Directory ramIndexDir;

	long ramLastModified;
	long luceneLastModified;

	LinkedList deleted;

	int operationCounter;

	Analyzer analyzer;

	Mutex indexMutex;

	private Rfc822Parser rfcParser;

	private final static String[] caps =
		{ "Body", "Subject", "From", "To", "Cc", "Bcc", "Custom Headerfield" };

	/**
	 * Constructor for LuceneSearchEngine.
	 */
	public LuceneSearchEngine(LocalFolder folder) {
		super(folder);

		MainInterface.shutdownManager.register(this);

		rfcParser = new Rfc822Parser();

		analyzer = new CAnalyzer();

		try {
			initRAMDir();
		} catch (IOException e) {
			e.printStackTrace();
		}

		luceneLastModified = -1;
		ramLastModified = -1;

		deleted = new LinkedList();
		operationCounter = 0;

		File folderInDir = folder.getDirectoryFile();
		indexDir = new File(folderInDir, ".index");

		try {
			if (!indexDir.exists()) {
				createIndex();
			}
			luceneIndexDir = FSDirectory.getDirectory(indexDir, false);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				e.getLocalizedMessage(),
				"Error while creating Lucene Index",
				JOptionPane.ERROR_MESSAGE);
		}

		try {
			// If there is an existing lock then it must be from a
			// previous crash -> remove it!
			if (IndexReader.isLocked(luceneIndexDir))
				IndexReader.unlock(luceneIndexDir);
		} catch (IOException e) {
			// Remove of lock didn't work -> delete by hand
			File commitLock = new File(indexDir, "commit.lock");
			if (commitLock.exists())
				commitLock.delete();

			File writeLock = new File(indexDir, "write.lock");
			if (writeLock.exists())
				writeLock.delete();
		}

		// Check if index is consitent with mailbox
		//if( getReader().numDocs() != folder.size() ) {
		//	recreateIndex();
		//}

		indexMutex = new Mutex("indexMutex");
	}

	protected void createIndex() throws IOException {
		DiskIO.ensureDirectory(indexDir);
		IndexWriter indexWriter = new IndexWriter(indexDir, null, true);
		indexWriter.close();
	}

	protected IndexReader getFileReader() {
		try {
			if (IndexReader.lastModified(luceneIndexDir)
				!= luceneLastModified) {
				fileIndexReader = IndexReader.open(luceneIndexDir);
				luceneLastModified = IndexReader.lastModified(luceneIndexDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileIndexReader;
	}

	protected IndexReader getRAMReader() {
		try {
			if (IndexReader.lastModified(ramIndexDir) != ramLastModified) {
				ramIndexReader = IndexReader.open(ramIndexDir);
				ramLastModified = IndexReader.lastModified(ramIndexDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ramIndexReader;
	}

	private Query getLuceneQuery(FilterRule filterRule, Analyzer analyzer) {
		FilterCriteria criteria;
		String field;
		int mode;

		Query result = new BooleanQuery();
		Query subresult = null;

		int condition = filterRule.getConditionInt();
		boolean prohibited, required;

		if (condition == FilterRule.MATCH_ALL) {
			prohibited = false;
			required = true;
		} else {
			prohibited = false;
			required = false;
		}

		BooleanQuery termQuery = null;

		for (int i = 0; i < filterRule.count(); i++) {

			criteria = filterRule.get(i);
			mode = criteria.getCriteria();
			// FIXME
			//field = criteria.getHeaderItemString().toLowerCase();
			field = criteria.getHeaderItemString();

			TokenStream tokenStream =
				analyzer.tokenStream(
					field,
					new StringReader(criteria.getPattern()));

			termQuery = new BooleanQuery();

			try {
				Token token = tokenStream.next();

				while (token != null) {
					String pattern = "*" + token.termText() + "*";
					ColumbaLogger.log.debug(
						"Field = \"" + field + "\" Text = \"" + pattern + "\"");
					termQuery.add(
						new WildcardQuery(new Term(field, pattern)),
						true,
						false);

					token = tokenStream.next();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			switch (mode) {
				case FilterCriteria.CONTAINS :
					{
						subresult = new BooleanQuery();
						((BooleanQuery) subresult).add(termQuery, true, false);
						break;
					}

				case FilterCriteria.CONTAINS_NOT :
					{
						subresult = new BooleanQuery();
						((BooleanQuery) subresult).add(
							new WildcardQuery(new Term("uid", "*")),
							true,
							false);
						((BooleanQuery) subresult).add(termQuery, false, true);
						break;
					}
			}
			((BooleanQuery) result).add(subresult, required, prohibited);

		}

		return result;
	}

	protected LinkedList queryEngine(
		FilterRule filter,
		WorkerStatusController worker)
		throws Exception {

		Query query = getLuceneQuery(filter, analyzer);

		LinkedList result = search(query);

		ListTools.substract(result, deleted);

		if(!checkResult(result, worker)) {
			// Search again
			result = search(query);
			ListTools.substract(result, deleted);
		}

		return result;
	}

	protected LinkedList search(Query query) throws IOException {
		boolean needToRelease = false;
		LinkedList result = new LinkedList();
		try {
			needToRelease = indexMutex.getMutex();

			if (getFileReader().numDocs() > 0) {
				Hits hitsFile =
					new IndexSearcher(getFileReader()).search(query);

				for (int i = 0; i < hitsFile.length(); i++) {
					result.add(
						new Integer(
							hitsFile.doc(i).getField("uid").stringValue()));
				}
			}

			if (getRAMReader().numDocs() > 0) {
				Hits hitsRAM = new IndexSearcher(getRAMReader()).search(query);

				for (int i = 0; i < hitsRAM.length(); i++) {
					result.add(
						new Integer(
							hitsRAM.doc(i).getField("uid").stringValue()));
				}
			}
		} finally {
			if (needToRelease) {
				indexMutex.releaseMutex();
			}
		}
		return result;
	}

	protected LinkedList queryEngine(
		FilterRule filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
		LinkedList result = queryEngine(filter, worker);

		ListTools.intersect(result, Arrays.asList(uids));
		return result;
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageAdded(org.columba.mail.message.AbstractMessage)
	 */
	public void messageAdded(AbstractMessage message) throws Exception {
		Document messageDoc = getDocument(message);

		boolean needToRelease = false;
		try {
			needToRelease = indexMutex.getMutex();
			IndexWriter writer = new IndexWriter(ramIndexDir, analyzer, false);
			writer.addDocument(messageDoc);
			writer.close();
			incOperationCounter();
		} finally {
			if (needToRelease) {
				indexMutex.releaseMutex();
			}
		}
	}

	private Document getDocument(AbstractMessage message) {
		Document messageDoc = new Document();
		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		messageDoc.add(Field.Keyword("uid", message.getUID().toString()));

		if (message.getMimePartTree() == null) {
			String source = message.getSource();

			message = rfcParser.parse(source, header);
			message.setSource(source);
		}

		Enumeration headerEntries = header.getHashtable().keys();
		String key;

		while (headerEntries.hasMoreElements()) {
			key = (String) headerEntries.nextElement();
			if ((key != "Return-Path") && !(key.startsWith("columba."))) {
				messageDoc.add(Field.UnStored(key, header.get(key).toString()));
			}
		}

		MimePart body = message.getMimePartTree().getFirstTextPart("plain");
		if (body != null)
			messageDoc.add(Field.UnStored("body", body.getBody()));
		return messageDoc;
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageRemoved(java.lang.Object)
	 */
	public void messageRemoved(Object uid) throws Exception {
		deleted.add(uid);

		/*
		try {
			indexLock.tryToGetLock(null);
			getReader().delete(new Term("uid", uid.toString()));
			indexLock.release();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				e.getMessage(),
				"Error while removing Message from Lucene Index",
				JOptionPane.ERROR_MESSAGE);
		}*/

	}

	protected void mergeRAMtoIndex() throws IOException {
		IndexReader ramReader = getRAMReader();
		IndexReader fileReader = getFileReader();

		if (MainInterface.DEBUG) {
                        ColumbaLogger.log.debug("Lucene: Merging RAMIndex to FileIndex");
                }

		/*
		Document doc;
		for( int i=0; i<ramReader.numDocs(); i++) {
			doc = ramReader.document(i);
			if( !deleted.contains(new Integer(ramReader.document(i).getField("uid").stringValue())) ) {
				 fileIndex.addDocument(doc);
			}
		}*/
		ListIterator it = deleted.listIterator();

		while (it.hasNext()) {
			String uid = it.next().toString();
			if (ramReader.delete(new Term("uid", uid)) == 0) {
				fileReader.delete(new Term("uid", uid));
			}
		}

		fileReader.close();
		ramReader.close();

		IndexWriter fileIndex =
			new IndexWriter(luceneIndexDir, analyzer, false);

		fileIndex.addIndexes(new Directory[] { ramIndexDir });

		fileIndex.optimize();
		fileIndex.close();

		initRAMDir();

		deleted.clear();
	}

	private void initRAMDir() throws IOException {
		ramIndexDir = new RAMDirectory();
		IndexWriter writer = new IndexWriter(ramIndexDir, analyzer, true);
		writer.close();
		ramLastModified = -1;
	}

	private void incOperationCounter() throws IOException {
		operationCounter++;
		if (operationCounter > OPTIMIZE_AFTER_N_OPERATIONS) {
			mergeRAMtoIndex();
			operationCounter = 0;
		}
	}

	/**
	 * @see org.columba.core.backgroundtask.TaskInterface#run()
	 */
	public void run() {
		try {
			mergeRAMtoIndex();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the caps.
	 * @return String[]
	 */
	public String[] getCaps() {
		return caps;
	}

	private boolean checkResult(List result, WorkerStatusController wc) {
		ListIterator it = result.listIterator();
		try {
			while (it.hasNext()) {
				if (!folder.exists(it.next(), wc)) {
					result.clear();
					sync(wc);
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @see org.columba.mail.folder.AbstractSearchEngine#reset()
	 */
	public void reset(WorkerStatusController wc) throws Exception {
		createIndex();
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.AbstractSearchEngine#sync(org.columba.mail.folder.DataStorageInterface, org.columba.core.command.WorkerStatusController)
	 */
	public void sync(WorkerStatusController wc) throws Exception {
		//ColumbaLogger.log.error("Lucene Index inconsistent - recreation forced");

		DataStorageInterface ds = ((LocalFolder)folder).getDataStorageInstance();
		HeaderList hl = ((LocalFolder)folder).getHeaderList(wc);

		wc.setDisplayText(MailResourceLoader.getString(
                                "statusbar",
                                "message",
                                "lucene_sync"));
		wc.setProgressBarValue(0);

		try {
			createIndex();
			IndexWriter writer =
				new IndexWriter(luceneIndexDir, analyzer, false);

			int count = hl.count();
			wc.setProgressBarMaximum(count);
			
			Object uid;
			int i=0;
					
			for (Enumeration e = hl.keys(); e.hasMoreElements();) {
				uid = e.nextElement();

				String source = ds.loadMessage(uid );

				AbstractMessage message =
					rfcParser.parse(source, (ColumbaHeader) hl.getHeader(uid));
				message.setSource(source);
				
				Document doc = getDocument(message);

				writer.addDocument(doc);
				
				if (++i % 50 == 0)
					wc.setProgressBarValue(i);
			}

			wc.setProgressBarValue(count);

			writer.optimize();
			writer.close();

		} catch (Exception e) {
			if (MainInterface.DEBUG) {
                                ColumbaLogger.log.error(
                                    "Creation of Lucene Index failed :" + e.getLocalizedMessage());
                        }
                        //show neat error dialog here
		}
	}
}
