package org.columba.mail.folder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

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
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.io.DiskIO;
import org.columba.core.main.MainInterface;
import org.columba.core.shutdown.ShutdownPluginInterface;
import org.columba.mail.filter.Filter;
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
public class LuceneSearchEngine implements SearchEngineInterface, ShutdownPluginInterface {

	Folder folder;
	IndexWriter indexWriter;
	IndexReader indexReader;
	File indexDir;
	Directory luceneIndexDir;

	Analyzer analyzer;

	/**
	 * Constructor for LuceneSearchEngine.
	 */
	public LuceneSearchEngine(Folder folder) {
		this.folder = folder;

		MainInterface.shutdownManager.register(this);

		analyzer = new StandardAnalyzer();

		File folderDir = folder.getDirectoryFile();

		indexDir = new File(folderDir,".index");

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
				if( IndexReader.isLocked(luceneIndexDir))
					IndexReader.unlock(luceneIndexDir);
			} catch (IOException e) {
				// Remove of lock didn't work -> delete by hand
				File commitLock = new File(indexDir,"commit.lock");
				if( commitLock.exists()) commitLock.delete();

				File writeLock = new File(indexDir,"write.lock");
				if( writeLock.exists()) writeLock.delete();
			}
				

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
				indexWriter.close();
				indexWriter = null;
			}

			if (indexReader == null) {
				indexReader = IndexReader.open(indexDir);
			}

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
			
			TokenStream tokenStream = analyzer.tokenStream(field,new StringReader(criteria.getPattern()));
			
			termQuery = new BooleanQuery();

			try {
				Token token = tokenStream.next();
				
				while( token != null) {
					termQuery.add(new TermQuery(new Term(field,token.termText())),
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
						((BooleanQuery) subresult).add(
							termQuery,
							true,
							false);
						break;
					}

				case FilterCriteria.CONTAINS_NOT :
					{
						subresult = new BooleanQuery();
						((BooleanQuery) subresult).add(
							new TermQuery(new Term("qall", "on")),
							true,
							false);
						((BooleanQuery) subresult).add(
							termQuery,
							false,
							true);
						break;
					}
			}
			((BooleanQuery) result).add(subresult, required, prohibited);

		}

		return result;
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#searchMessages(org.columba.mail.filter.Filter, java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public Object[] searchMessages(
		Filter filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {

		Object[] result = queryLucene(filter);

		return result;
	}

	private Object[] queryLucene(Filter filter) throws IOException {
		if (indexWriter != null) {
			indexWriter.optimize();
			indexWriter.close();
			indexWriter = null;
		}

		Query query = getLuceneQuery(filter.getFilterRule(), analyzer);

		Object[] result;
		Searcher searcher = new IndexSearcher(getReader());

		Hits hits = searcher.search(query);

		result = new Object[hits.length()];
		for (int i = 0; i < hits.length(); i++)
			result[i] = new Integer(hits.doc(i).getField("uid").stringValue());
		return result;
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#searchMessages(org.columba.mail.filter.Filter, org.columba.core.command.WorkerStatusController)
	 */
	public Object[] searchMessages(
		Filter filter,
		WorkerStatusController worker)
		throws Exception {

		Object[] result = queryLucene(filter);
		return result;
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageAdded(org.columba.mail.message.AbstractMessage)
	 */
	public void messageAdded(AbstractMessage message) {
		Document messageDoc = new Document();
		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		messageDoc.add(Field.Keyword("uid", message.getUID().toString()));

		messageDoc.add(Field.UnStored("qall", "on"));

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
			getWriter().addDocument(messageDoc);
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
		try {
			getReader().delete(new Term("uid", uid.toString()));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null,
				e.getMessage(),
				"Error while removing Message from Lucene Index",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @see org.columba.core.shutdown.ShutdownPluginInterface#run()
	 */
	public void shutdown() {		
		try {
			if( indexWriter != null) {
				indexWriter.close();
			}
			
			if( indexReader != null){
				indexReader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
