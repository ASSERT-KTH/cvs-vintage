/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.factories;

import java.io.PrintWriter;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import javax.transaction.*;
import javax.transaction.xa.*;
import org.jboss.minerva.pools.*;
import org.jboss.minerva.xa.*;

/**
 * Object factory for JDBC 2.0 standard extension XAConnections.  You pool the
 * XAConnections instead of the java.sql.Connections since with vendor
 * conformant drivers, you don't have direct access to the java.sql.Connection,
 * and any work done isn't associated with the java.sql.Connection anyway.
 * <P><B>Note:</B> This implementation requires that the TransactionManager
 * be bound to a JNDI name.</P>
 * @version $Revision: 1.3 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAConnectionFactory extends PoolObjectFactory {
    private InitialContext ctx;
    private XADataSource source;
    private String userName;
    private String password;
    private String tmJndiName;
    private ConnectionEventListener listener;
    private TransactionListener transListener;
    private ObjectPool pool;
    private PrintWriter log;

    /**
     * Creates a new factory.  You must set the XADataSource and
     * TransactionManager JNDI name before the factory can be used.
     */
    public XAConnectionFactory() throws NamingException {
        ctx = new InitialContext();
        listener = new ConnectionEventListener() {
            public void connectionErrorOccurred(ConnectionEvent evt) {
                closeConnection(evt, XAResource.TMFAIL);
            }

            public void connectionClosed(ConnectionEvent evt) {
                closeConnection(evt, XAResource.TMSUCCESS);
            }

            private void closeConnection(ConnectionEvent evt, int status) {
                XAConnection con = (XAConnection)evt.getSource();
                boolean transaction = false;
                try {
                    TransactionManager tm = (TransactionManager)ctx.lookup(tmJndiName);
                    if(tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        transaction = true;
                        tm.getTransaction().delistResource(con.getXAResource(), status);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unable to deregister with TransactionManager: "+e);
                }
                con.removeConnectionEventListener(listener);

                if(!(con instanceof XAConnectionImpl)) {
                    // Real XAConnection -> not associated w/ transaction
                    pool.releaseObject(con);
                } else {
                    if(!transaction) {
                        // Wrapper - we can only release it if there's no current transaction
                        ((XAConnectionImpl)con).rollback();
                        pool.releaseObject(con);
                    }
                }
            }
        };
        transListener = new TransactionListener() {
            public void transactionFinished(XAConnectionImpl con) {
                con.clearTransactionListener();
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
            TransactionManager tm = (TransactionManager)ctx.lookup(tmJndiName);
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
            e.printStackTrace();
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
        try {
            TransactionManager tm = (TransactionManager)ctx.lookup(tmJndiName);
            if(tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.getTransaction().enlistResource(con.getXAResource());
                con.addConnectionEventListener(listener);
                if(log != null) log.println("Enlisted with transaction.");
            }
            if(log != null) log.println("No transaction right now.");
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to register with TransactionManager: "+e);
        }
        if(con instanceof XAConnectionImpl)
            ((XAConnectionImpl)con).setTransactionListener(transListener);
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
}