package org.columba.mail.folder;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
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
import org.columba.core.command.WorkerStatusController;
import org.columba.core.io.DiskIO;
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
public class LuceneSearchEngine implements SearchEngineInterface {

	Folder folder;
	IndexWriter indexWriter;
	IndexReader indexReader;
	File indexDir;

	Analyzer analyzer;

	/**
	 * Constructor for LuceneSearchEngine.
	 */
	public LuceneSearchEngine(Folder folder) {
		this.folder = folder;

		analyzer = new SimpleAnalyzer();

		File folderDir = folder.getDirectoryFile();

		indexDir = new File(folderDir.getAbsolutePath() + "/.index");

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

	private Query getLuceneQuery(FilterRule filterRule) {
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

		for (int i = 0; i < filterRule.count(); i++) {

			criteria = filterRule.get(i);
			mode = criteria.getCriteria();
			field = criteria.getHeaderItemString().toLowerCase();

			switch (mode) {
				case FilterCriteria.CONTAINS :
					{
						subresult = new BooleanQuery();
						((BooleanQuery) subresult).add(
							new TermQuery(
								new Term(
									field,
									criteria.getPattern().toLowerCase())),
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
							new TermQuery(
								new Term(
									field,
									criteria.getPattern().toLowerCase())),
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

		Query query = getLuceneQuery(filter.getFilterRule());

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

		messageDoc.add(Field.Text("qall", "on"));

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
				messageDoc.add(Field.Text(key, header.get(key).toString()));
			}
		}

		MimePart body = message.getMimePartTree().getFirstTextPart("plain");
		if (body != null)
			messageDoc.add(Field.Text("body", body.getBody()));

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

}
