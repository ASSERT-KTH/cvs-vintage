package org.columba.mail.folder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
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
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.io.DiskIO;
import org.columba.core.main.MainInterface;
import org.columba.core.shutdown.ShutdownPluginInterface;
import org.columba.core.util.ListTools;
import org.columba.core.util.Lock;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.parser.Rfc822Parser;

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
	implements ShutdownPluginInterface {

	private final static int OPTIMIZE_AFTER_N_OPERATIONS = 30;

	IndexWriter indexWriter;
	IndexReader indexReader;
	File indexDir;
	Directory luceneIndexDir;

	LinkedList deleted;

	int operationCounter;

	Analyzer analyzer;

	Lock indexLock;
	
	long lastModified;

	private final static String[] caps =
		{ "Body", "Subject", "From", "To", "Cc", "Bcc", "Custom Headerfield" };

	/**
	 * Constructor for LuceneSearchEngine.
	 */
	public LuceneSearchEngine(Folder folder) {
		super(folder);

		MainInterface.shutdownManager.register(this);

		analyzer = new CAnalyzer();

		deleted = new LinkedList();
		operationCounter = 0;

		File folderDir = folder.getDirectoryFile();

		indexDir = new File(folderDir, ".index");

		try {
			if (!indexDir.exists()) {
				DiskIO.ensureDirectory(indexDir);
				indexWriter = new IndexWriter(indexDir, null, true);
				indexWriter.close();
				indexWriter = null;
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				e.getMessage(),
				"Error while creating Lucene Index",
				JOptionPane.ERROR_MESSAGE);
		}

		try {
			// If there is an existing lock then it must be from a
			// previous crash -> remove it!
			luceneIndexDir = FSDirectory.getDirectory(indexDir, false);
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

		indexLock = new Lock();
	}

	protected IndexWriter getWriter() {
		try {
			if (indexReader != null) {
				indexReader.close();
				indexReader = null;
			}

			if (indexWriter == null) {
				indexWriter = new IndexWriter(indexDir, analyzer, false);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return indexWriter;
	}

	protected IndexReader getReader() {
		try {
			if (indexWriter != null) {
				indexWriter.optimize();
				indexWriter.close();
				indexWriter = null;
			}

			if (indexReader == null) {
				indexReader = IndexReader.open(indexDir);
			}

/*			
			if( indexReader==null) {
				lastModified = IndexReader.lastModified(indexDir);
				indexReader = IndexReader.open(indexDir);				
			} else {
				if(IndexReader.lastModified(indexDir)>lastModified ) {					
					indexReader.close();
					indexReader = IndexReader.open(indexDir);				
					lastModified = IndexReader.lastModified(indexDir);
				}
			}
*/
			
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		return indexReader;
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
			field = criteria.getHeaderItemString().toLowerCase();

			TokenStream tokenStream =
				analyzer.tokenStream(
					field,
					new StringReader(criteria.getPattern()));

			termQuery = new BooleanQuery();

			try {
				Token token = tokenStream.next();

				while (token != null) {
					termQuery.add(
						new TermQuery(new Term(field, token.termText())),
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
							new TermQuery(new Term("qall", "hit")),
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

		indexLock.tryToGetLock(null);
		Searcher searcher = new IndexSearcher(getReader());

		Hits hits = searcher.search(query);
		indexLock.release();

		LinkedList result = new LinkedList();
		for (int i = 0; i < hits.length(); i++) {
			result.add(new Integer(hits.doc(i).getField("uid").stringValue()));
		}

		ListTools.substract(result, deleted);

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
	public void messageAdded(AbstractMessage message) {
		Document messageDoc = new Document();
		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		messageDoc.add(Field.Keyword("uid", message.getUID().toString()));

		messageDoc.add(Field.Keyword("qall", "hit"));

		if (message.getMimePartTree() == null) {
			String source = message.getSource();

			message = new Rfc822Parser().parse(source, true, header, 0);
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

		try {
			indexLock.tryToGetLock(null);
			getWriter().addDocument(messageDoc);
			incOperationCounter();
			indexLock.release();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				e.getMessage(),
				"Error while adding Message to Lucene Index",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageRemoved(java.lang.Object)
	 */
	public void messageRemoved(Object uid) {
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

	private void commitDeletion() {
		if( deleted.size() == 0) return;
		
		try {
			if( indexWriter != null) {
				indexWriter.close();
				indexWriter = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ListIterator it = deleted.listIterator();
		
		while( it.hasNext() ) {		
			try {
				getReader().delete(new Term("uid", it.next().toString()));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					"Error while removing Message from Lucene Index",
					JOptionPane.ERROR_MESSAGE);
			}
		}
		
		deleted.clear();
	}

	private void incOperationCounter() {
		operationCounter++;
		if( operationCounter > OPTIMIZE_AFTER_N_OPERATIONS ) {
			
			commitDeletion();
			try {
				getWriter().optimize();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					"Error while optimizing Lucene Index",
					JOptionPane.ERROR_MESSAGE);				
			}
			operationCounter = 0; 
		}
		
	}

	/**
	 * @see org.columba.core.shutdown.ShutdownPluginInterface#run()
	 */
	public void shutdown() {
		
		commitDeletion();
		try {
			if (indexWriter != null) {
				indexWriter.optimize();
				indexWriter.close();
			}

			if (indexReader != null) {
				indexReader.close();
			}
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

}
