/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.factories;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import org.jboss.minerva.pools.ObjectPool;
import org.jboss.minerva.pools.PoolObjectFactory;
import org.jboss.minerva.xa.TransactionListener;
import org.jboss.minerva.xa.XAConnectionImpl;
import org.jboss.logging.Logger;


/**
 * Object factory for JDBC 2.0 standard extension XAConnections.  You pool the
 * XAConnections instead of the java.sql.Connections since with vendor
 * conformant drivers, you don't have direct access to the java.sql.Connection,
 * and any work done isn't associated with the java.sql.Connection anyway.
 * <P><B>Note:</B> This implementation requires that the TransactionManager
 * be bound to a JNDI name.</P>
 * <P><B>Note:</B> This implementation has special handling for Minerva JDBC
 * 1/2 XA Wrappers.  Namely, when a request comes in, if it is for a wrapper
 * connection and it has the same current transaction as a previous active
 * connection, the same previous connection will be returned.  Otherwise,
 * you won't be able to share changes across connections like you can with
 * the native JDBC 2 Standard Extension implementations.</P>
 * @version $Revision: 1.11 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAConnectionFactory extends PoolObjectFactory {
    private InitialContext ctx;
    private XADataSource source;
    private String userName;
    private String password;
    private String tmJndiName;
    private ConnectionEventListener listener, errorListener;
    private TransactionListener transListener;
    private ObjectPool pool;
    private PrintWriter log;
    private HashMap wrapperTx, rms;
    private TransactionManager tm;

    /**
     * Creates a new factory.  You must set the XADataSource and
     * TransactionManager JNDI name before the factory can be used.
     */
    public XAConnectionFactory() throws NamingException {
        ctx = new InitialContext();
        wrapperTx = new HashMap();
        rms = new HashMap();
        errorListener = new ConnectionEventListener() {
            public void connectionErrorOccurred(ConnectionEvent evt) {
                if(pool.isInvalidateOnError()) {
                    pool.markObjectAsInvalid(evt.getSource());
                }
            }
            public void connectionClosed(ConnectionEvent evt) {}
        };

        listener = new ConnectionEventListener() {

            public void connectionErrorOccurred(ConnectionEvent evt) {
                if(pool.isInvalidateOnError()) {
                    pool.markObjectAsInvalid(evt.getSource());
                }
//                closeConnection(evt, XAResource.TMFAIL);
            }

            public void connectionClosed(ConnectionEvent evt) {
                closeConnection(evt, XAResource.TMSUCCESS);
            }

            private void closeConnection(ConnectionEvent evt, int status) {
                XAConnection con = (XAConnection)evt.getSource();
                try {
                    con.removeConnectionEventListener(listener);
                } catch(IllegalArgumentException e) {
                    return; // Removed twice somehow?
                }
                Transaction trans = null;
                try {
                    if(tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        trans = tm.getTransaction();
                        XAResource res = (XAResource)rms.get(con);
                        trans.delistResource(res, status);
                        rms.remove(con);
                    }
                } catch(Exception e) {
                    Logger.exception(e);
                    throw new RuntimeException("Unable to deregister with TransactionManager: "+e);
                }

                if(!(con instanceof XAConnectionImpl)) {
                    // Real XAConnection -> not associated w/ transaction
                    pool.releaseObject(con);
                } else {
                    if(trans == null) {
                        // Wrapper - we can only release it if there's no current transaction
                        try {
                            ((XAConnectionImpl)con).rollback();
                        } catch(SQLException e) {
                            pool.markObjectAsInvalid(con);
                        }
                        pool.releaseObject(con);
                    } else {
                        // Still track errors, but don't try to close again.
                        con.addConnectionEventListener(errorListener);
                    }
                }
            }
        };
        transListener = new TransactionListener() {
            public void transactionFinished(XAConnectionImpl con) {
                con.removeConnectionEventListener(errorListener);
                con.clearTransactionListener();
                Object tx = wrapperTx.remove(con);
                if(tx != null)
                    wrapperTx.remove(tx);
                pool.releaseObject(con);
            }

            public void transactionFailed(XAConnectionImpl con) {
                con.removeConnectionEventListener(errorListener);
                con.clearTransactionListener();
                Object tx = wrapperTx.remove(con);
                if(tx != null)
                    wrapperTx.remove(tx);
                pool.markObjectAsInvalid(con);
                pool.releaseObject(con);
            }
        };
    }

    /**
     * Sets the user name used to generate XAConnections.  This is optional,
     * and will only be used if present.
     */
    public void setUser(String userName) {this.userName = userName;}

    /**
     * Gets the user name used to generate XAConnections.
     */
    public String getUser() {return userName;}

    /**
     * Sets the password used to generate XAConnections.  This is optional,
     * and will only be used if present.
     */
    public void setPassword(String password) {this.password = password;}

    /**
     * Gets the password used to generate XAConnections.
     */
    public String getPassword() {return password;}

    /**
     * Sets the XADataSource used to generate XAConnections.  This may be
     * supplied by the vendor, or it may use the wrappers for non-compliant
     * drivers (see XADataSourceImpl).
     * @see org.jboss.minerva.xa.XADataSourceImpl
     */
    public void setDataSource(XADataSource dataSource) {source = dataSource;}

    /**
     * Gets the XADataSource used to generate XAConnections.
     */
    public XADataSource getDataSource() {return source;}

    /**
     * Sets the JNDI name that the TransactionManager is registered under.
     */
    public void setTransactionManagerJNDIName(String name) {tmJndiName = name;}

    /**
     * Gets the JNDI name that the TransactionManager is registered under.
     */
    public String getTransactionManagerJNDIName() {return tmJndiName;}

    /**
     * Verifies that the data source and transaction manager are accessible.
     */
    public void poolStarted(ObjectPool pool, PrintWriter log) {
        super.poolStarted(pool, log);
        this.log = log;
        this.pool = pool;
        if(source == null)
            throw new IllegalStateException("Must specify XADataSource to "+getClass().getName());
        if(tmJndiName == null)
            throw new IllegalStateException("Must specify TransactionManager JNDI Name to "+getClass().getName());
        if(ctx == null)
            throw new IllegalStateException("Must specify InitialContext to "+getClass().getName());
        try {
            tm = (TransactionManager)ctx.lookup(tmJndiName);
        } catch(NamingException e) {
            throw new IllegalStateException("Cannot lookup TransactionManager using specified context and name!");
        }
    }

    /**
     * Creates a new XAConnection from the provided XADataSource.
     */
    public Object createObject() {
        try {
            if(userName != null && userName.length() > 0)
                return source.getXAConnection(userName, password);
            else
                return source.getXAConnection();
        } catch(SQLException e) {
            Logger.exception(e);
        }
        return null;
    }

    /**
     * Registers the XAConnection's XAResource with the current transaction (if
     * there is one).  Sets listeners that will handle deregistering and
     * returning the XAConnection to the pool via callbacks.
     */
    public Object prepareObject(Object pooledObject) {
        XAConnection con = (XAConnection)pooledObject;
        con.addConnectionEventListener(listener);
        Transaction trans = null;
        try {
            if(tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                trans = tm.getTransaction();
                XAResource res = con.getXAResource();
                trans.enlistResource(res);
                rms.put(con, res);
                if(log != null) log.println("Resource '"+res+"' enlisted for '"+con+"'.");
            } else {
                if(log != null) log.println("No transaction right now.");
            }
        } catch(Exception e) {
            Logger.exception(e);
            throw new RuntimeException("Unable to register with TransactionManager: "+e);
        }
        if(con instanceof XAConnectionImpl) {
            ((XAConnectionImpl)con).setTransactionListener(transListener);
            if(trans != null) {
                wrapperTx.put(con, trans); // For JDBC 1/2 wrappers, remember which
                wrapperTx.put(trans, con); // connection goes with a given transaction
            }
        }
        return con;
    }

    /**
     * Closes a connection.
     */
    public void deleteObject(Object pooledObject) {
        XAConnection con = (XAConnection)pooledObject;
        try {
            con.close();
        } catch(SQLException e) {}
    }

    /**
     * If a new object is requested and it is a JDBC 1/2 wrapper connection
     * in the same Transaction as an existing connection, return that same
     * connection.
     */
    public Object isUniqueRequest() {
        try {
            if(tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                Transaction trans = tm.getTransaction();
                return wrapperTx.get(trans);
            }
        } catch(Exception e) {}
        return null;
    }
}