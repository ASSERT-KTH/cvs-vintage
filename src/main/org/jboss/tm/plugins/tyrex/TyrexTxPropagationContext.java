/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm.plugins.tyrex;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

import org.jboss.logging.Logger;

// OMG CORBA related stuff (used by Tyrex for transaction context propagation)
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.otid_t;

// We need this to make a proxy for the OMG Coordinator for the remote site
import java.lang.reflect.Proxy;

/**
 *   This class wraps the OMG PropagationContext to be able to pass it
 *   via RMI. Currently we are only taking care of top-level transaction
 *   (no nested transactions) by sending via RMI only
 *   - timeout value
 *   - otid (@see org.omg.CosTransactions.otid_t - representation of Xid)
 *   - Coordinator's Proxy
 *
 *
 *   @see org.omg.CosTransactions.PropagationContext,
 *        org.omg.CosTransactions.otid_t,
 *        org.omg.CosTransactions.Coordinator,
 *        java.lang.reflect.Proxy
 *   @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 *   @version $Revision: 1.1 $
 */

public class TyrexTxPropagationContext implements Externalizable {

  public TyrexTxPropagationContext() {
    // public, no args constructor for externalization to work

    this.isNull = true;
  }

  protected TyrexTxPropagationContext (PropagationContext tpc) {
    this.isNull = false; // this is not a null Propagation Context
    this.timeout = tpc.timeout;
    this.coord = (Coordinator) Proxy.newProxyInstance(getClass().getClassLoader(),
                                        new Class[] {Coordinator.class},
                                        new CoordinatorInvoker(tpc.current.coord));
    this.otid = tpc.current.otid;
    // DEBUG    Logger.debug("TyrexTxPropagationContext: created new tpc");
  }

  // this is called on the remote side
  protected PropagationContext getPropagationContext() {

    if ( !isNull && (tpc == null) ) {
      // create once
      tpc = new PropagationContext( this.timeout,
                                    new TransIdentity(this.coord,
                                                      null,
                                                      this.otid),
                                    new TransIdentity[0], // no parents, but not null
                                    null);
    }

    // DEBUG    Logger.debug("TyrexTxPropagationContext recreated PropagationContext");
    return tpc;
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    try {
      out.writeBoolean(this.isNull);
      if (! isNull) {
        out.writeInt(this.timeout);
        // DEBUG       Logger.debug("TPC: wrote timeout");
        out.writeObject((Proxy) this.coord);
        // DEBUG       Logger.debug("TPC: wrote CoordinatorProxy");
        out.writeObject(this.otid); // otid implements IDLEntity which extends Serializable
        // DEBUG       Logger.debug("TPC: wrote otid");
      }
    } catch (Exception e) {
      Logger.warning("Unable to externalize tpc!");
      e.printStackTrace();
      throw new IOException(e.toString());
    }
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    try {
      this.isNull = in.readBoolean();
      if (!isNull) {
        this.timeout = in.readInt();
        // DEBUG      Logger.debug("TPC: read timeout");
        this.coord = (Coordinator) in.readObject();
        // DEBUG      Logger.debug("TPC: read coordinator");
        this.otid = (otid_t) in.readObject();
        // DEBUG      Logger.debug("TPC: read otid");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException (e.toString());
    }
  }

  /*
   * The fields of PropagationContext that we want to send to the remote side
   */

  protected int timeout;
  // this is a Proxy for the local transaction coordinator
  // since we need to serialize the Coordinator and it is not serializable
  protected Coordinator coord;
  protected otid_t otid;

  // this is a special field that gets propagated to the remote side to
  // indicate that this is a null propagation context (i.e. it represents a
  // null transaction). Simply using a null TyrexTxPropagationContext in RMI
  // calls crashes because of NullPointerException in serialization of a
  // method invocation
  protected boolean isNull;

  // cached copy of tpc, so that we need to create it only once
  // on the remote side
  protected PropagationContext tpc = null;
}