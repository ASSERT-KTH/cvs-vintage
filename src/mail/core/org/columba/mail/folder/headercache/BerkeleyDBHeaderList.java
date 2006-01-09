// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.headercache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.columba.mail.folder.IHeaderListCorruptedListener;
import org.columba.mail.message.ICloseableIterator;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IPersistantHeaderList;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredIterator;
import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.collections.StoredValueSet;
import com.sleepycat.je.BtreeStats;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class BerkeleyDBHeaderList implements IPersistantHeaderList {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.headercache");
	
	
	private static final String DB_NAME = "HeaderList";
	private File databaseFile;
	private Database db;
	private Environment environment;
	private DatabaseConfig databaseConfig;
	
	private TupleBinding headerBinding;
	private TupleBinding integerBinding;
	private TupleBinding stringBinding;
	
	private Class keyType = Integer.class;
	
	private List listeners;
	
	public BerkeleyDBHeaderList(File databaseFile){
		this(databaseFile, new DefaultHeaderBinding());
	}

	
	public BerkeleyDBHeaderList(File databaseFile, TupleBinding headerBinding){
		super();
		this.databaseFile = databaseFile;				
		this.headerBinding = headerBinding;
		
		integerBinding = new IntegerBinding();
		stringBinding = new StringBinding();
		
		listeners = new ArrayList();
	}

	private void openEnvironment() {
		if( environment != null) return;
		if( !databaseFile.exists()) databaseFile.mkdir();
		
		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		// perform other environment configurations
		try {
			environment = new Environment(databaseFile, environmentConfig);
		} catch (DatabaseException e) {
			LOG.severe(e.getMessage());
			fireHeaderListCorrupted();
		}
		
	}
	
	private void openDatabase() {
		if( db != null ) return;
		openEnvironment();

		try {
			databaseConfig = new DatabaseConfig();
			databaseConfig.setAllowCreate(true);
			// perform other database configurations
			db = environment.openDatabase(null, DB_NAME , databaseConfig);
		} catch (DatabaseException e) {
			LOG.severe(e.getMessage());
			fireHeaderListCorrupted();
		}		
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#add(org.columba.mail.message.IColumbaHeader, java.lang.Object)
	 */
	public void add(IColumbaHeader header, Object uid) {
		openDatabase();
		
		try {
			header.getAttributes().put("columba.uid", uid);
			
			db.put(null, getDatabaseEntry(uid), getDatabaseEntry(header));
		} catch (DatabaseException e) {
			LOG.severe(e.getMessage());
			fireHeaderListCorrupted();
		}
	}

	private DatabaseEntry getDatabaseEntry(Object in) {
		DatabaseEntry result = new DatabaseEntry();

		if( in instanceof String) {
			stringBinding.objectToEntry( in, result);
		} else if( in instanceof Integer) {
			integerBinding.objectToEntry(in, result);			
		} else if( in instanceof IColumbaHeader) {
			headerBinding.objectToEntry(in, result);
		}
		
		return result;
	}


	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#clear()
	 */
	public void clear() {
		closeDatabase();
		
		try {
			environment.truncateDatabase(null, DB_NAME, true);
		} catch (DatabaseException e) {
			LOG.warning(e.getMessage());
		}
	}

	private void closeEnvironment() {
		if( db != null) closeDatabase();
		
		try {
			if( environment != null ) {
				environment.close();
				environment = null;
			}
		} catch (DatabaseException e) {
			LOG.warning(e.getMessage());
		}			
		
	}
	
	private void closeDatabase() {
		try {
			if( db != null ) {
				db.close();
				db = null;
			}

		} catch (DatabaseException e) {
			LOG.warning(e.getMessage());
		} 
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#containsValue(java.lang.Object)
	 */
	public boolean exists(Object uid) {
		openDatabase();
		try {
			return db.get(null, getDatabaseEntry(uid), new DatabaseEntry(), LockMode.DEFAULT).equals(OperationStatus.SUCCESS);
		} catch (DatabaseException e) {
			LOG.fine(e.getMessage());
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#count()
	 */
	public int count() {
		openDatabase();

		try {
			return (int) ((BtreeStats)db.getStats(null)).getLeafNodeCount();
		} catch (DatabaseException e) {
			LOG.severe(e.getMessage());
			fireHeaderListCorrupted();
			
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#get(java.lang.Object)
	 */
	public IColumbaHeader get(Object uid) {
		openDatabase();
		
		DatabaseEntry result = new DatabaseEntry();
		try {
			OperationStatus status = db.get(null, getDatabaseEntry(uid), result, LockMode.DEFAULT);
			if( status.equals(OperationStatus.SUCCESS) ) {
				return (IColumbaHeader)headerBinding.entryToObject(result);
			}
		} catch (DatabaseException e) {
			LOG.fine(e.getMessage());
		}
		return null;		
	}


	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#getUids()
	 */
	public Object[] getUids() {
		openDatabase();
		if( keyType == Integer.class ) {
			return new StoredKeySet(db, integerBinding, false).toArray();
		} else if( keyType == String.class) {
			return new StoredKeySet(db, stringBinding, false).toArray();			
		}
		
		throw new IllegalArgumentException("keyType not implemented!");
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#keySet()
	 */
	public Set keySet() {
		openDatabase();
		
		if( keyType == Integer.class ) {
			return new StoredKeySet(db, integerBinding, false);
		} else if( keyType == String.class) {
			return new StoredKeySet(db, stringBinding, false);			
		}
		
		throw new IllegalArgumentException("keyType not implemented!");
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#remove(java.lang.Object)
	 */
	public IColumbaHeader remove(Object uid) {
		openDatabase();
		
		IColumbaHeader header = get(uid);
		try {
			db.delete(null, getDatabaseEntry(uid));
		} catch (DatabaseException e) {
			LOG.fine(e.getMessage());

			return null;
		}
		
		return header;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.PersistantHeaderList#persist()
	 */
	public void persist() throws IOException {
		closeEnvironment();
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#headerIterator()
	 */
	public ICloseableIterator headerIterator() {
		openDatabase();
		return new BerkeleyDBIterator( (StoredIterator)new StoredValueSet(db,headerBinding, false).iterator());
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#keyIterator()
	 */
	public ICloseableIterator keyIterator() {
		openDatabase();
		return new BerkeleyDBIterator( (StoredIterator)new StoredKeySet(db, integerBinding, false).iterator());
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#update(java.lang.Object, org.columba.mail.message.IColumbaHeader)
	 */
	public void update(Object uid, IColumbaHeader header) {
		openDatabase();
		DatabaseEntry key = getDatabaseEntry(uid);
		
		try {
			db.delete(null, key);
			db.put(null, key, getDatabaseEntry(header));
		} catch (DatabaseException e) {
			LOG.severe(e.getMessage());
			fireHeaderListCorrupted();
		}
	}

	/**
	 * @param headerBinding The headerBinding to set.
	 */
	public void setHeaderBinding(TupleBinding headerBinding) {
		this.headerBinding = headerBinding;
	}


	/**
	 * @return Returns the keyType.
	 */
	public Class getKeyType() {
		return keyType;
	}


	/**
	 * @param keyType The keyType to set.
	 */
	public void setKeyType(Class keyType) {
		this.keyType = keyType;
	}
	
	private void fireHeaderListCorrupted() {
		for( int i=0; i<listeners.size(); i++) {
			((IHeaderListCorruptedListener)listeners.get(i)).headerListCorrupted(this);
		}
	}


	public void addHeaderListCorruptedListener(IHeaderListCorruptedListener listener) {
		listeners.add(listener);
	}
	
	public void removeHeaderListCorruptedListener(IHeaderListCorruptedListener listener) {
		listeners.remove(listener);
	}
}
