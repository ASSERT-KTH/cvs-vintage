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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.columba.api.command.IStatusObservable;
import org.columba.core.base.ListTools;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.filter.FilterRule;
import org.columba.core.io.DiskIO;
import org.columba.core.io.StreamUtils;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.event.IFolderEvent;
import org.columba.mail.message.ICloseableIterator;
import org.columba.mail.message.IHeaderList;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * @author timo
 */
public class LuceneQueryEngine implements QueryEngine {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.search");

	private static final int OPTIMIZE_AFTER_N_OPERATIONS = 30;

	private static final String[] CAPS = { "Body" };

	private File indexDir;

	private IndexReader fileIndexReader;

	private IndexReader ramIndexReader;

	private Directory luceneIndexDir;

	private Directory ramIndexDir;

	private long ramLastModified;

	private long luceneLastModified;

	private LinkedList deleted;

	private int operationCounter;

	private Analyzer analyzer;

	private IMailbox folder;

	/**
	 * Constructor for LuceneQueryEngine.
	 */
	public LuceneQueryEngine(IMailbox folder) {
		this.folder = folder;

		analyzer = new StandardAnalyzer();

		try {
			initRAMDir();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					// to be executed on shutdown
					mergeRAMtoIndex();
				} catch (IOException ioe) {
					LOG.severe(ioe.getMessage());
				}
			}
		}, "LuceneIndexMerger"));

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
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
					"Error while creating Lucene Index",
					JOptionPane.ERROR_MESSAGE);
		}

		try {
			// If there is an existing lock then it must be from a
			// previous crash -> remove it!
			if (IndexReader.isLocked(luceneIndexDir)) {
				IndexReader.unlock(luceneIndexDir);
			}
		} catch (IOException e) {
			// Remove of lock didn't work -> delete by hand
			File commitLock = new File(indexDir, "commit.lock");

			if (commitLock.exists()) {
				commitLock.delete();
			}

			File writeLock = new File(indexDir, "write.lock");

			if (writeLock.exists()) {
				writeLock.delete();
			}
		}

		// Check if index is consitent with mailbox
		try {
			if( getFileReader().numDocs() != folder.getHeaderList().count() ) {
				sync();
			}
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	protected void createIndex() throws IOException {
		DiskIO.ensureDirectory(indexDir);

		IndexWriter indexWriter = new IndexWriter(indexDir, null, true);
		indexWriter.close();
	}

	protected IndexReader getFileReader() {
		try {
			// @TODO dont use deprecated method
			if (IndexReader.getCurrentVersion(luceneIndexDir) != luceneLastModified) {
				fileIndexReader = IndexReader.open(luceneIndexDir);
				// @TODO dont use deprecated method
				luceneLastModified = IndexReader
						.getCurrentVersion(luceneIndexDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileIndexReader;
	}

	protected IndexReader getRAMReader() {
		try {
			if (IndexReader.getCurrentVersion(ramIndexDir) != ramLastModified) {
				ramIndexReader = IndexReader.open(ramIndexDir);
				ramLastModified = IndexReader.getCurrentVersion(ramIndexDir);
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
		boolean prohibited;
		boolean required;

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

			field = "Body";

			TokenStream tokenStream = analyzer.tokenStream(field,
					new StringReader(criteria.getPatternString()));

			termQuery = new BooleanQuery();

			try {
				Token token = tokenStream.next();

				while (token != null) {
					String pattern = "*" + token.termText() + "*";
					LOG.info("Field = \"" + field + "\" Text = \"" + pattern
							+ "\"");
					termQuery.add(new WildcardQuery(new Term(field, pattern)),
							true, false);

					token = tokenStream.next();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			switch (mode) {
			case FilterCriteria.CONTAINS: {
				subresult = new BooleanQuery();
				((BooleanQuery) subresult).add(termQuery, true, false);

				break;
			}

			case FilterCriteria.CONTAINS_NOT: {
				subresult = new BooleanQuery();
				((BooleanQuery) subresult).add(new WildcardQuery(new Term(
						"uid", "*")), true, false);
				((BooleanQuery) subresult).add(termQuery, false, true);

				break;
			}
			}

			((BooleanQuery) result).add(subresult, required, prohibited);
		}

		return result;
	}

	public List queryEngine(FilterRule filter) throws Exception {
		Query query = getLuceneQuery(filter, analyzer);

		List result = search(query);

		ListTools.substract(result, deleted);

		if (!checkResult(result)) {
			// Search again
			result = search(query);
			ListTools.substract(result, deleted);
		}

		return result;
	}

	protected List search(Query query) throws IOException {
		LinkedList result = new LinkedList();

		if (getFileReader().numDocs() > 0) {
			Hits hitsFile = new IndexSearcher(getFileReader()).search(query);

			for (int i = 0; i < hitsFile.length(); i++) {
				result.add(new Integer(hitsFile.doc(i).getField("uid")
						.stringValue()));
			}
		}

		if (getRAMReader().numDocs() > 0) {
			Hits hitsRAM = new IndexSearcher(getRAMReader()).search(query);

			for (int i = 0; i < hitsRAM.length(); i++) {
				result.add(new Integer(hitsRAM.doc(i).getField("uid")
						.stringValue()));
			}
		}

		return result;
	}

	public List queryEngine(FilterRule filter, Object[] uids) throws Exception {
		List result = queryEngine(filter);

		ListTools.intersect(result, Arrays.asList(uids));

		return result;
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageAdded(IFolderEvent)
	 */
	public void messageAdded(Object uid) throws Exception {
		Document messageDoc = getDocument(uid);

		IndexWriter writer = new IndexWriter(ramIndexDir, analyzer, false);
		writer.addDocument(messageDoc);
		writer.close();
		incOperationCounter();
	}

	private Document getDocument(Object uid) {
		Document messageDoc = new Document();

		messageDoc.add(Field.Keyword("uid", uid.toString()));

		// Find the body text part
		try {
			MimeTree mimeTree = folder.getMimePartTree(uid);
			MimePart bodyPart = mimeTree.getFirstTextPart("plain");

			if (bodyPart != null) {
				messageDoc.add(Field.UnStored("Body", StreamUtils
						.readCharacterStream(
								folder.getMimePartBodyStream(uid, bodyPart
										.getAddress())).toString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOG.severe(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.severe(e.getMessage());
		}

		return messageDoc;
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageRemoved(IFolderEvent)
	 */
	public void messageRemoved(Object uid) throws Exception {
		deleted.add(uid);

		/*
		 * try { indexLock.tryToGetLock(null); getReader().delete(new
		 * Term("uid", uid.toString())); indexLock.release(); } catch
		 * (IOException e) { JOptionPane.showMessageDialog( null,
		 * e.getMessage(), "Error while removing Message from Lucene Index",
		 * JOptionPane.ERROR_MESSAGE); }
		 */
	}

	protected void mergeRAMtoIndex() throws IOException {
		IndexReader ramReader = getRAMReader();
		IndexReader fileReader = getFileReader();

		LOG.fine("Lucene: Merging RAMIndex to FileIndex");

		/*
		 * Document doc; for( int i=0; i<ramReader.numDocs(); i++) { doc =
		 * ramReader.document(i); if( !deleted.contains(new
		 * Integer(ramReader.document(i).getField("uid").stringValue())) ) {
		 * fileIndex.addDocument(doc); } }
		 */
		ListIterator it = deleted.listIterator();

		while (it.hasNext()) {
			String uid = it.next().toString();

			if (ramReader.delete(new Term("uid", uid)) == 0) {
				fileReader.delete(new Term("uid", uid));
			}
		}

		fileReader.close();
		ramReader.close();

		IndexWriter fileIndex = new IndexWriter(luceneIndexDir, analyzer, false);

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
	 * Returns the caps.
	 * 
	 * @return String[]
	 */
	public String[] getCaps() {
		return CAPS;
	}

	private boolean checkResult(List result) {
		ListIterator it = result.listIterator();

		try {
			while (it.hasNext()) {
				if (!folder.exists(it.next())) {
					result.clear();
					sync();

					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * @see org.columba.mail.folder.DefaultSearchEngine#reset()
	 */
	public void reset() throws Exception {
		createIndex();
	}

	/** {@inheritDoc} */
	public void sync() throws Exception {
		LOG.severe("Lucene Index inconsistent - recreation forced");
		IHeaderList hl = folder.getHeaderList();

		if (getObservable() != null) {
			getObservable().setMessage(
					MailResourceLoader.getString("statusbar", "message",
							"lucene_sync"));
		}

		getObservable().setCurrent(0);

		try {
			createIndex();

			IndexWriter writer = new IndexWriter(luceneIndexDir, analyzer,
					false);

			int count = hl.count();
			getObservable().setCurrent(count);

			Object uid;
			int i = 0;
			ICloseableIterator it;
			for (it = hl.keyIterator(); it.hasNext();) {
				uid = it.next();
				
				writer.addDocument(getDocument(uid));

				getObservable().setCurrent(i);
			}
			it.close();

			getObservable().setCurrent(count);

			writer.optimize();
			writer.close();
		} catch (Exception e) {
			LOG.severe("Creation of Lucene Index failed :"
					+ e.getLocalizedMessage());

			// show neat error dialog here
		}
	}

	public IStatusObservable getObservable() {
		return folder.getObservable();
	}

	public void save() {
		try {
			mergeRAMtoIndex();
		} catch (IOException e) {
			LOG.severe(e.getMessage());
		}
		
	}

}
