/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.util.timeout;


/**
 *  The timeout factory.
 *
 *  This is written with performance in mind. In case of <code>n</code>
 *  active timeouts, creating, cancelling and firing timeouts all operate
 *  in time <code>O(log(n))</code>.
 *
 *  If a timeout is cancelled, the timeout is not discarded. Instead the
 *  timeout is saved to be reused for another timeout. This means that if
 *  no timeouts are fired, this class will eventually operate without
 *  allocating anything on the heap.
 *   
 *  @author <a href="osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.1 $
*/
public class TimeoutFactory {

  /**
   *  Our private Timeout implementation.
   */
  private class TimeoutImpl implements Timeout {
    static final int DONE = -1; // done, may be finalized
    static final int TIMEOUT = -2; // target being called
    static final int CWAIT = -3; // target being called and cancel waiting

    int index; // index in queue, or one of constants above.
    long time;
    TimeoutTarget target;

    public void cancel() {
      TimeoutFactory.this.cancelTimeout(this);
    }
  }

  /** The size of the timeout queue. */
  private int size;

  /**
   *  Our priority queue.
   *  This is a binary tree. If nonempty, the root is at index 1, and all
   *  nodes are at indices 1..size. Nodes with index greater than size
   *  are considered null. Index 0 is never used.
   *  Children of the node at index <code>j</code> are at <code>j*2</code>
   *  and <code>j*2+1</code>. The children of a node always fire the timeout
   *  no later than the node.
   */
  private TimeoutImpl[] q;

  /**
   *  Debugging helper.
   */
  private void assert(boolean expr) {
    if (!expr) {
      System.err.println("***** assert failed *****");
      Thread.currentThread().dumpStack();
    }
  }

  /**
   *  Swap two nodes in the tree.
   */
  private void swap(int a, int b) {
      assert(q[a].index == a);
      assert(q[b].index == b);
      TimeoutImpl temp = q[a];
      q[a] = q[b];
      q[a].index = a;
      q[b] = temp;
      q[b].index = b;
  }

  /**
   *  A new node has been added at index <code>index</code>.
   *  Normalize the tree by moving the new node up the tree.
   */
  private void normalizeUp(int index) {
    assert(index > 0);
    assert(index <= size);
    assert(q[index] != null);
    long t = q[index].time;

    if (index == 1)
      return; // at root

    int p = index >> 1;

    while (q[p].time > t) {
      swap(p, index);

      if (p == 1)
        return; // at root

      index = p;
      p >>= 1;
    }
  }

  /**
   *  Remove a node from the tree and normalize.
   *  Returns the removed node.
   */
  private TimeoutImpl removeNode(int index) {
    assert(index > 0);
    assert(index <= size);
    TimeoutImpl res = q[index];

    // one less entry
    if (--size <= 1)
      return res; // Already normal

    // Normalize
    int c = index << 1;
    long t = res.time;

    while (q[c].time <= t) {
      swap(index, c);

      index = c;

      c <<= 1;
      if (c > size)
        break; // node at index is a leaf
    }
    return res;
  }


  /**
   *  Create a new timeout.
   */
  private synchronized Timeout newTimeout(long time, TimeoutTarget target) {
    if (size == q.length) {
      TimeoutImpl[] newQ = new TimeoutImpl[2*q.length];
      System.arraycopy(q, 0, newQ, 0, q.length);
      q = newQ;
    }
    ++size;
    if (q[size] == null)
      q[size] = new TimeoutImpl();

    TimeoutImpl timeout = q[size];

    timeout.index = size;
    timeout.time = time;
    timeout.target = target;

    normalizeUp(size);

    if (timeout.index == 1 || size == 1)
      notify();

    return timeout;
  }

  /**
   *  Cancel a timeout.
   */
  private void dropTimeout(TimeoutImpl timeout) {
    synchronized (this) {
      if (timeout.index > 0) {
        removeNode(timeout.index);
        return;
      }
    }

    // Timeout has already started, wait until done.
    synchronized (timeout) {
      if (timeout.index == TimeoutImpl.TIMEOUT) {
        // Wait to avoid race with the actual timeout that is happening now.
        timeout.index = TimeoutImpl.CWAIT;
        while (timeout.index == TimeoutImpl.CWAIT) {
          try {
            timeout.wait();
          } catch (InterruptedException ex) {}
        }
      }
    }
  }


  /**
   *  Cancel a timeout.
   */
  private void cancelTimeout(Timeout timeout) {
    if (timeout == null)
      throw new IllegalArgumentException("Null timeout");
    if (!(timeout instanceof TimeoutImpl))
      throw new IllegalArgumentException("Unknown timeout");

    dropTimeout((TimeoutImpl)timeout);
  }

  /**
   *  Timeout worker method.
   *  This method never returns. Whenever it is time to do a timeout,
   *  the callback method is called from here.
   */
  private void doWork() {
    while (true) {
      TimeoutImpl work = null;

      synchronized (this) {
        if (size == 0) {
          try {
            wait();
          } catch (InterruptedException ex) {}
        } else {
          long now = System.currentTimeMillis();
          if (q[1].time > now) {
            try {
              wait(q[1].time - now);
            } catch (InterruptedException ex) {}
          } else {
            work = removeNode(1);
            q[work.index] = null;
            work.index = TimeoutImpl.TIMEOUT;
          }
        }
      }

      if (work != null) {
        work.target.timedOut(work);
        synchronized (work) {
          if (work.index == TimeoutImpl.CWAIT) {
            work.index = TimeoutImpl.DONE;
            work.notify(); // wake up cancel() thread.
          }
        }
      }
    }
  }

  /** Our singleton instance. */
  static private TimeoutFactory singleton;

  /** Our private constructor. */
  private TimeoutFactory() {
    size = 0;
    q = new TimeoutImpl[16];
  }

  /**
   *  Initialize class.
   *  The will initialize the singleton and create a single
   *  worker thread.
   */
  static {
    singleton = new TimeoutFactory();
    Thread thread = new Thread() {
      public void run() {
        singleton.doWork();
      }
    };
    thread.setDaemon(true);
    thread.start();
  }

  /**
   *  Schedule a new timeout.
   */
  static public Timeout createTimeout(long time, TimeoutTarget target) {
    if (time <= 0)
      throw new IllegalArgumentException("Time not positive");
    if (target == null)
      throw new IllegalArgumentException("Null target");

    return singleton.newTimeout(time, target);
  }

}
 
