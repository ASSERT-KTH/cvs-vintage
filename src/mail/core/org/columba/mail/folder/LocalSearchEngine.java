package org.columba.mail.folder;

import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.filter.plugins.AbstractFilter;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.plugin.AbstractFilterPluginHandler;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LocalSearchEngine implements SearchEngineInterface {

	protected Folder folder;

	protected Hashtable filterCache;

	public LocalSearchEngine(Folder folder) {
		this.folder = folder;

		filterCache = new Hashtable();
	}

	/********* you need to implement these methods **************/

	/**
	 * 
	 * take a look at <class>LocalSearchEngine</class> for an
	 * implementation example
	 * 
	 */

	/*
	public abstract boolean processHeader(
		HeaderInterface header,
		String headerField,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception;
	
	public abstract boolean processDate(
		HeaderInterface header,
		Date date,
		int condition,
		WorkerStatusController worker)
		throws Exception;
	
	public abstract boolean processSize(
		HeaderInterface header,
		Integer size,
		int condition,
		WorkerStatusController worker)
		throws Exception;
	
	public abstract boolean processFlags(
		HeaderInterface header,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception;
	
	public abstract boolean processPriority(
		HeaderInterface header,
		Integer searchPattern,
		int condition,
		WorkerStatusController worker)
		throws Exception;
	
	public abstract boolean processBody(
		String messageSource,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception;
	*/

	/******************* framework ********************************/

	/*
	public Vector processHeaderCriteria(
		Object[] uids,
		String headerField,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		Vector v = new Vector();
	
		for (int i = 0; i < uids.length; i++) {
			if (folder.exists(uids[i], worker) == false)
				continue;
	
			HeaderInterface header = folder.getMessageHeader(uids[i], worker);
			boolean b =
				processHeader(header, headerField, pattern, condition, worker);
			if (b == true)
				v.add(uids[i]);
	
		}
	
		return v;
	}
	
	public Vector processDateCriteria(
		Object[] uids,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		Vector v = new Vector();
	
		Date searchPattern = transformDate(pattern);
	
		for (int i = 0; i < uids.length; i++) {
			if (folder.exists(uids[i], worker) == false)
				continue;
	
			HeaderInterface header = folder.getMessageHeader(uids[i], worker);
			boolean b = processDate(header, searchPattern, condition, worker);
			if (b == true)
				v.add(uids[i]);
	
		}
	
		return v;
	}
	
	protected Date transformDate(String pattern) {
		java.text.DateFormat df = java.text.DateFormat.getDateInstance();
		Date searchPattern = null;
		try {
			searchPattern = df.parse(pattern);
		} catch (java.text.ParseException ex) {
			System.out.println("exception: " + ex.getMessage());
			ex.printStackTrace();
	
			//return new Vector();
		}
		return searchPattern;
	}
	
	public Vector processSizeCriteria(
		Object[] uids,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		Vector v = new Vector();
	
		Integer searchPattern = transformSize(pattern);
	
		for (int i = 0; i < uids.length; i++) {
			if (folder.exists(uids[i], worker) == false)
				continue;
			HeaderInterface header = folder.getMessageHeader(uids[i], worker);
			boolean b = processSize(header, searchPattern, condition, worker);
			if (b == true)
				v.add(uids[i]);
	
		}
	
		return v;
	}
	
	protected Integer transformSize(String pattern) {
		Integer searchPattern = Integer.valueOf(pattern);
		return searchPattern;
	}
	
	public Vector processFlagsCriteria(
		Object[] uids,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		Vector v = new Vector();
	
		String headerField = pattern;
	
		String searchHeaderField = null;
	
		if (headerField.equalsIgnoreCase("Answered")) {
			searchHeaderField = new String("columba.flags.answered");
		} else if (headerField.equalsIgnoreCase("Deleted")) {
			searchHeaderField = new String("columba.flags.deleted");
		} else if (headerField.equalsIgnoreCase("Flagged")) {
			searchHeaderField = new String("columba.flags.flagged");
		} else if (headerField.equalsIgnoreCase("Recent")) {
			searchHeaderField = new String("columba.flags.recent");
		} else if (headerField.equalsIgnoreCase("Draft")) {
			searchHeaderField = new String("columba.flags.draft");
		} else if (headerField.equalsIgnoreCase("Seen")) {
			searchHeaderField = new String("columba.flags.seen");
		}
	
		if (searchHeaderField == null)
			return new Vector();
	
		for (int i = 0; i < uids.length; i++) {
			if (folder.exists(uids[i], worker) == false)
				continue;
			HeaderInterface header = folder.getMessageHeader(uids[i], worker);
			boolean b =
				processFlags(header, searchHeaderField, condition, worker);
			if (b == true)
				v.add(uids[i]);
		}
	
		return v;
	}
	
	public Vector processPriorityCriteria(
		Object[] uids,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		Vector v = new Vector();
	
		Integer priority = transformPriority(pattern);
	
		for (int i = 0; i < uids.length; i++) {
			if (folder.exists(uids[i], worker) == false)
				continue;
			HeaderInterface header = folder.getMessageHeader(uids[i], worker);
			boolean b = processPriority(header, priority, condition, worker);
			if (b == true)
				v.add(uids[i]);
	
		}
	
		return v;
	
	}
	
	protected Integer transformPriority(String pattern) {
		Integer priority = Integer.valueOf(pattern);
		return priority;
	}
	
	public Vector processBodyCriteria(
		Object[] uids,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		Vector v = new Vector();
	
		String bodyText = pattern;
	
		for (int i = 0; i < uids.length; i++) {
			if (folder.exists(uids[i], worker) == false)
				continue;
			String source = folder.getMessageSource(uids[i], worker);
			boolean b = processBody(source, pattern, condition, worker);
			if (b == true)
				v.add(uids[i]);
	
		}
	
		return v;
	}
	*/

	public Object[] searchMessages(
		Filter filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
		FilterRule rule = filter.getFilterRule();

		Vector v = processCriteria(rule, uids, worker);
		//printList(v);

		return v.toArray();

	}

	public Object[] searchMessages(
		Filter filter,
		WorkerStatusController worker)
		throws Exception {
		FilterRule rule = filter.getFilterRule();

		Object[] uids = folder.getUids(worker);

		Vector v = processCriteria(rule, uids, worker);
		//printList(v);

		return v.toArray();

	}

	AbstractFilter getFilter(String type) {

		ColumbaLogger.log.debug("trying to re-use cached instanciation ="+type);
		
		// try to re-use already instanciated class
		if (filterCache.containsKey(type) == true)
			return (AbstractFilter) filterCache.get(type);

		ColumbaLogger.log.debug("loading new instance ="+type);
		
		AbstractFilter instance = null;
		try {
			AbstractFilterPluginHandler handler =
				(AbstractFilterPluginHandler) folder.getFilterPluginHandler();
			instance = (AbstractFilter) handler.getActionPlugin(type, null);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Error while trying to load filter plugin ="+type);
			ex.printStackTrace();
		}

		if ( instance != null ) filterCache.put(type, instance);

		return instance;

	}

	protected Vector processCriteria(
		FilterRule rule,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {

		TreeMap[] v = new TreeMap[rule.count()];
		Vector result = new Vector();

		/*
		Vector v = new Vector();
		Vector list = new Vector();
		Vector result = new Vector();
		*/

		int match = rule.getConditionInt();

		for (int index = 0; index < uids.length; index++) {

			for (int i = 0; i < rule.count(); i++) {
				if (v[i] == null)
					v[i] = new TreeMap();

				FilterCriteria criteria = rule.get(i);

				String type = criteria.getType();
				
				if ( type == null )
				{
					JOptionPane.showMessageDialog(null, "Filter type couldn't been found", "Error occured", JOptionPane.ERROR_MESSAGE);
					
					break;	
				}
				
				AbstractFilter instance = getFilter(type);

				if (instance == null)
					continue;

				Object[] args = instance.getAttributes();

				Object[] attributes = new Object[args.length];
				for (int j = 0; j < args.length; j++) {
					attributes[j] = criteria.get((String) args[j]);
				}

				boolean b =
					instance.process(attributes, folder, uids[index], worker);

				if (b == true) {
					Object o = v[i].put(uids[index], "a");

				}
			}
		}

		result = mergeFilterResult(v, uids, match);

		// only for debugging purpose
		//printList( result );

		return result;
		/*
		String pattern = criteria.getPattern();
		//String condition = criteria.getCriteria();
		int condition = criteria.getCriteria();
		
		//System.out.println("i="+i+ " - type="+ type );
		
		if (type.equalsIgnoreCase("Custom Headerfield")) {
			String headerField = criteria.getHeaderItemString();
		
			v =
				processHeaderCriteria(
					uids,
					headerField,
					pattern,
					condition,
					worker);
		
			//printList( v );
		} else if (type.equalsIgnoreCase("To or Cc")) {
			Vector v1 =
				processHeaderCriteria(
					uids,
					"To",
					pattern,
					condition,
					worker);
			Vector v2 =
				processHeaderCriteria(
					uids,
					"Cc",
					pattern,
					condition,
					worker);
		
			v = mergeLists(v1, v2);
		} else if (type.equalsIgnoreCase("Date")) {
			v = processDateCriteria(uids, pattern, condition, worker);
		} else if (type.equalsIgnoreCase("Size")) {
			v = processSizeCriteria(uids, pattern, condition, worker);
		} else if (type.equalsIgnoreCase("Flags")) {
			v = processFlagsCriteria(uids, pattern, condition, worker);
		} else if (type.equalsIgnoreCase("Priority")) {
			v = processPriorityCriteria(uids, pattern, condition, worker);
		} else if (type.equalsIgnoreCase("Body")) {
			v = processBodyCriteria(uids, pattern, condition, worker);
		} else {
			v =
				processHeaderCriteria(
					uids,
					type,
					pattern,
					condition,
					worker);
			//printList( v );
		}
		
		
		list.add(v);
		}
		*/

	}

	protected Vector mergeFilterResult(TreeMap[] v, Object[] uids, int match) {
		Vector result = new Vector();

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];

			boolean b;
			if (match == FilterRule.MATCH_ALL)
				b = true;
			else
				b = false;

			boolean r;
			for (int j = 0; j < v.length; j++) {
				TreeMap table = v[j];

				r = table.containsKey(uid);

				if (match == FilterRule.MATCH_ALL)
					b &= r;
				else
					b |= r;

			}

			if (b == true) {
				// match
				result.add(uid);
			}
		}

		return result;
	}

	/********************** helper classes ****************************/

	// only for debugging
	protected void printList(Vector v) {
		System.out.println("print list:");

		for (int i = 0; i < v.size(); i++) {
			System.out.println(i + "->" + v.get(i));
		}
	}

	protected Vector mergeLists(Vector v1, Vector v2) {
		Vector v = new Vector(v1);
		v.addAll(v2);

		return v;
	}

	/*
	protected Vector mergeFilterResult(Vector list, int match) {
		Vector result = (Vector) list.get(0);
	
		if (list.size() == 1)
			return result;
	
		if (match == 0) {
	
			for (int i = 1; i < list.size(); i++) {
				result.retainAll((Vector) list.get(i));
				if (result.size() == 0)
					break;
			}
		} else if (match == 1) {
	
			// match any
	
			//System.out.println("match any");
	
			int j = 1;
	
			// First make one big List
	
			for (int i = 1; i < list.size(); i++) {
				result.addAll((Vector) list.get(i));
			}
	
			if (result.size() == 0)
				return result;
	
			// Sort it
	
			Collections.sort(result);
	
			// remove redundancies
	
			Object lastuid = result.get(0);
			Object actuid;
	
			int size = result.size();
	
			for (int i = 1; i < size; i++) {
				actuid = result.get(j);
	
				if (actuid.equals(lastuid))
					result.remove(j);
				else
					j++;
	
				lastuid = actuid;
			}
		}
	
		return result;
	}
	*/
	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageAdded(org.columba.mail.message.AbstractMessage)
	 */
	public void messageAdded(AbstractMessage message) {
	}

	/**
	 * @see org.columba.mail.folder.SearchEngineInterface#messageRemoved(java.lang.Object)
	 */
	public void messageRemoved(Object uid) {
	}

}
