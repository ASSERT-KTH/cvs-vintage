/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.util.timeout;

import org.jboss.logging.Logger;


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
 *  @version $Revision: 1.2 $
*/
public class TimeoutFactory {

  /**
   *  Our private Timeout implementation.
   */
  private class TimeoutImpl implements Timeout {
    static final int DONE    = -1; // done, may be finalized or reused
    static final int TIMEOUT = -2; // target being called
    static final int CWAIT   = -3; // target being called and cancel waiting
                   
    int index; // index in queue, or one of constants above.
    long time; // time to fire
    TimeoutTarget target; // target to fire at
    TimeoutImpl nextFree; // next on free list

    public void cancel() {
      TimeoutFactory.this.dropTimeout(this);
    }
  }

  /** Linked list of free TimeoutImpl instances. */
  private TimeoutImpl freeList;

  /** The size of the timeout queue. */
  private int size;

  /**
   *  Our priority queue.
   *
   *  This is a balanced binary tree. If nonempty, the root is at index 1,
   *  and all nodes are at indices 1..size. Nodes with index greater than
   *  size are null. Index 0 is never used.
   *  Children of the node at index <code>j</code> are at <code>j*2</code>
   *  and <code>j*2+1</code>. The children of a node always fire the timeout
   *  no earlier than the node.
   *
   *  @see checkTree
   */
  private TimeoutImpl[] q;

  /**
   *  Debugging helper.
   */
  private void assert(boolean expr) {
    if (!expr) {
      Logger.error("***** assert failed *****");
      //System.err.println("***** assert failed *****");
      try {
        throw new RuntimeException("***** assert failed *****");
      } catch (RuntimeException ex) {
        Logger.exception(ex);
        //ex.printStackTrace();
      }
      try {
        Thread.sleep(30000);
      } catch (Exception ex) {}
    }
  }

  /**
   *  Check invariants of the queue.
   */
  private void checkTree() {
    assert(size >= 0);
    assert(size < q.length);
    assert(q[0] == null);

    if (size > 0) {
      assert(q[1] != null);
      assert(q[1].index == 1);
      for (int i = 2; i <= size; ++i) {
        assert(q[i] != null);
        assert(q[i].index == i);
        assert(q[i >> 1].time <= q[i].time); // parent fires first
      }
      for (int i = size+1; i < q.length; ++i)
        assert(q[i] == null);
    }
  }

  /**
   *  Check invariants of the free list.
   */
  private void checkFreeList() {
    TimeoutImpl to = freeList;

    while (to != null) {
      assert(to.index == TimeoutImpl.DONE);
      to = to.nextFree;
    }
  }

  /**
   *  Swap two nodes in the tree.
   */
  private void swap(int a, int b) {
      assert(a > 0);
      assert(a <= size);
      assert(b > 0);
      assert(b <= size);
      assert(q[a] != null);
      assert(q[b] != null);
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
   *
   *  @return True iff the tree was modified.
   */
  private boolean normalizeUp(int index) {
    assert(index > 0);
    assert(index <= size);
    assert(q[index] != null);

    if (index == 1)
      return false; // at root

    boolean ret = false;
    long t = q[index].time;
    int p = index >> 1;

    while (q[p].time > t) {
      assert(q[index].time == t);
      swap(p, index);
      ret = true;

      if (p == 1)
        break; // at root

      index = p;
      p >>= 1;
    }
    return ret;
  }

  /**
   *  Remove a node from the tree and normalize.
   *
   *  @return The removed node.
   */
  private TimeoutImpl removeNode(int index) {
    assert(index > 0);
    assert(index <= size);
    TimeoutImpl res = q[index];
    assert(res != null);
    assert(res.index == index);

    if (index == size)  {
      --size;
      q[index] = null;
      return res;
    }

    swap(index, size); // Exchange removed node with last leaf node
    --size;

    assert(res.index == size + 1);
    q[res.index] = null;

    if (normalizeUp(index))
      return res; // Node moved up, so it shouldn't move down

    long t = q[index].time;
    int c = index << 1;

    while (c <= size) {
      assert(q[index].time == t);

      TimeoutImpl l = q[c];
      assert(l != null);
      assert(l.index == c);

      if (c+1 <= size) {
        // two children, swap with smallest
        TimeoutImpl r = q[c+1];
        assert(r != null);
        assert(r.index == c+1);

        if (l.time <= r.time) {
          if (t <= l.time)
            break; // done
          swap(index, c);
          index = c;
        } else {
          if (t <= r.time)
            break; // done
          swap(index, c+1);
          index = c+1;
        }
      } else { // one child
        if (t <= l.time)
          break; // done
        swap(index, c);
        index = c;
      }

      c = index << 1;
    }

    return res;
  }

  /**
   *  Create a new timeout.
   */
  private synchronized Timeout newTimeout(long time, TimeoutTarget target) {
    checkTree();

    assert(size < q.length);
    if (++size == q.length) {
      TimeoutImpl[] newQ = new TimeoutImpl[2*q.length];
      System.arraycopy(q, 0, newQ, 0, q.length);
      q = newQ;
    }
    assert(size < q.length);
    assert(q[size] == null);

    TimeoutImpl timeout;

    if (freeList != null) {
      timeout = q[size] = freeList;
      freeList = timeout.nextFree;
      checkFreeList();
      assert(timeout.index == TimeoutImpl.DONE);
    } else
      timeout = q[size] = new TimeoutImpl();

    timeout.index = size;
    timeout.time = time;
    timeout.target = target;

    normalizeUp(size);

    if (timeout.index == 1)
      notify();

    checkTree();

    return timeout;
  }

  /**
   *  Cancel a timeout.
   */
  private void dropTimeout(TimeoutImpl timeout) {
    synchronized (this) {
      if (timeout.index > 0) {
        // Active timeout, remove it.
        assert(q[timeout.index] == timeout);
        checkTree();
        removeNode(timeout.index);
        checkTree();
        timeout.index = TimeoutImpl.DONE;
        timeout.nextFree = freeList;
        freeList = timeout;
        checkFreeList();
        return;
      }
    }

    // If timeout has already started, wait until done.
    synchronized (timeout) {
      if (timeout.index == TimeoutImpl.TIMEOUT ||
          timeout.index == TimeoutImpl.CWAIT) {
        // Wait to avoid race with the actual timeout that is happening now.
        timeout.index = TimeoutImpl.CWAIT;
        while (timeout.index == TimeoutImpl.CWAIT) {
          try {
            timeout.wait();
          } catch (InterruptedException ex) { }
        }
      }
    }
  }


  /**
   *  Timeout worker method.
   *  This method never returns. Whenever it is time to do a timeout,
   *  the callback method is called from here.
   */
  private void doWork() {
    while (true) {
      TimeoutImpl work = null;

      // Look for work
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
          }
          if (size > 0 && q[1].time <= System.currentTimeMillis()) {
            work = removeNode(1);
            work.index = TimeoutImpl.TIMEOUT;
          }
        }
      }

      // Do work, if any
      if (work != null) {
        try {
          work.target.timedOut(work);
        } catch (Throwable t) {
          Logger.exception(t);
          //t.printStackTrace();
        }
        synchronized (work) {
          if (work.index == TimeoutImpl.CWAIT) {
            work.index = TimeoutImpl.DONE;
            work.notifyAll(); // wake up cancel() threads.
          } else
            work.index = TimeoutImpl.DONE;
        }
      }
    }
  }

  /** Our singleton instance. */
  static private TimeoutFactory singleton;

  /** Our private constructor. */
  private TimeoutFactory() {
    freeList = null;
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
 
