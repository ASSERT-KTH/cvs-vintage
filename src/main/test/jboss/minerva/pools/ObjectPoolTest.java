/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test.jboss.minerva.pools;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;
import junit.framework.TestCase;
import org.jboss.minerva.pools.*;

/**
 * JUnit tests for ObjectPool class.  Doesn't test some of the subtleties or
 * data not accessible via the public interface, but tests every
 * public method at least once (except toString).
 * @version $Revision: 1.3 $
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 */
public class ObjectPoolTest extends TestCase {
    private ObjectPool pool;
    private PoolObjectFactory factory;
    private String name;

    public ObjectPoolTest(String s) {
        super(s);
    }

// setup for tests

    public void setUp() {
        pool = new ObjectPool();
        factory = new PoolObjectFactory() {
            public Object createObject() {
                return new Object();
            }
        };
        name = "TestPool";
    }

    private void startPool() {
        pool.setName(name);
        pool.setObjectFactory(factory);
    }

// Parameter tests

    public void testParamName() {
        try {
            assert(pool.getName() == null);
            pool.setName("");
            assert(pool.getName().equals(""));
            fail("Able to set pool name to empty string!");
        } catch(IllegalArgumentException e) {
            try {
                assert(pool.getName() == null);
                pool.setName(null);
                fail("Able to set pool name to null!");
            } catch(IllegalArgumentException e2) {
                assert(pool.getName() == null);
                pool.setName(name);
                assert(pool.getName().equals(name));
                pool.setName(name);
                try {
                    pool.setName(pool.getName()+"name");
                    fail("Able to change pool name after set!");
                } catch(IllegalStateException e3) {
                }
            }
        }
    }

    public void testParamBlocking() {
        pool.setBlocking(true);
        assert(pool.isBlocking());
        pool.setBlocking(false);
        assert(!pool.isBlocking());
    }

    public void testParamShrinking() {
        pool.setShrinkingEnabled(true);
        assert(pool.isShrinkingEnabled());
        pool.setShrinkingEnabled(false);
        assert(!pool.isShrinkingEnabled());
    }

    public void testParamGC() {
        pool.setGCEnabled(true);
        assert(pool.isGCEnabled());
        pool.setGCEnabled(false);
        assert(!pool.isGCEnabled());
    }

    public void testParamTimestamp() {
        pool.setTimestampUsed(true);
        assert(pool.isTimestampUsed());
        pool.setTimestampUsed(false);
        assert(!pool.isTimestampUsed());
    }

    public void testParamGCInterval() {
        pool.setGCInterval(4391l);
        assert(pool.getGCInterval() == 4391l);
        pool.setGCInterval(9734l);
        assert(pool.getGCInterval() == 9734l);
    }

    public void testParamGCMinIdle() {
        pool.setGCMinIdleTime(4391l);
        assert(pool.getGCMinIdleTime() == 4391l);
        pool.setGCMinIdleTime(9734l);
        assert(pool.getGCMinIdleTime() == 9734l);
    }

    public void testParamShrinkMinIdle() {
        pool.setShrinkMinIdleTime(4391l);
        assert(pool.getShrinkMinIdleTime() == 4391l);
        pool.setShrinkMinIdleTime(9734l);
        assert(pool.getShrinkMinIdleTime() == 9734l);
    }

    public void testParamShrinkPercent() {
        pool.setShrinkPercent(0.5f);
        assert(pool.getShrinkPercent() == 0.5f);
        pool.setShrinkPercent(0.2f);
        assert(pool.getShrinkPercent() == 0.2f);
        try {
            pool.setShrinkPercent(-0.01f);
            fail("Pool allowed negative shrink percent!");
        } catch(IllegalArgumentException e) {}
        try {
            pool.setShrinkPercent(1.01f);
            fail("Pool allowed shrink percent >1!");
        } catch(IllegalArgumentException e) {}
    }

    public void testParamLogWriter() {
        PrintWriter writer = new PrintWriter(System.out);
        try {
            pool.setLogWriter(writer);
            assert(pool.getLogWriter() == writer);
            pool.setLogWriter(null);
            assert(pool.getLogWriter() == null);
        } catch(SQLException e) {
            fail(e.getMessage());
        }
    }

    public void testParamMinSize() {
        pool.setMinSize(4);
        assert(pool.getMinSize() == 4);
        pool.setMinSize(8);
        assert(pool.getMinSize() == 8);
    }

    public void testParamMaxSize() {
        pool.setMaxSize(4);
        assert(pool.getMaxSize() == 4);
        pool.setMaxSize(8);
        assert(pool.getMaxSize() == 8);
    }

// Initialization tests

    public void testNeedsName() {
        pool.setObjectFactory(factory);
        try {
            pool.initialize();
        } catch(IllegalStateException e) {
            return;
        }
        fail("Pool initialized without pool name!");
    }

    public void testNeedsFactory() {
        pool.setName(name);
        try {
            pool.initialize();
        } catch(IllegalStateException e) {
            return;
        }
        fail("Pool initialized without pool factory!");
    }

    public void testNeedsInitialize() {
        pool.setName(name);
        pool.setObjectFactory(factory);
        try {
            Object o = pool.getObject();
        } catch(IllegalStateException e) {
            return;
        }
        fail("Pool got object without initializing!");
    }

    public void testMinimumParameters() {
        pool.setName(name);
        pool.setObjectFactory(factory);
        pool.initialize();
        Object o = pool.getObject();
        assert(o != null);
        pool.shutDown();
    }

// Test pool behavior

    public void testPoolMax() {
        Vector v = new Vector();
        startPool();
        pool.setMinSize(0);
        pool.setMaxSize(5);
        pool.setBlocking(false);
        pool.initialize();
        for(Object o = pool.getObject(); o != null; o = pool.getObject()) {
            assert(!v.contains(o));
            v.addElement(o);
            assert(v.size() <= pool.getMaxSize());
        }
        assert(v.size() == pool.getMaxSize());
        for(int i=0; i<v.size(); i++)
            pool.releaseObject(v.elementAt(i));
        pool.shutDown();
    }

    public void testEvents() {
        Stack s = new Stack();
        startPool();
        pool.setMinSize(0);
        pool.setMaxSize(10);
        pool.setBlocking(false);
        pool.initialize();
        for(Object o = pool.getObject(); o != null; o = pool.getObject()) {
            s.push(o);
        }
        int count = 0;
        while(!s.isEmpty()) {
            Object o = s.pop();
            pool.objectUsed(new PoolEvent(o, PoolEvent.OBJECT_USED));
            if(++count%2 == 0)
                pool.objectClosed(new PoolEvent(o, PoolEvent.OBJECT_CLOSED));
            else
                pool.objectError(new PoolEvent(o, PoolEvent.OBJECT_ERROR));
        }
        count = 0;
        for(Object o = pool.getObject(); o != null; o = pool.getObject()) {
            s.push(o);
            ++count;
        }
        while(!s.isEmpty())
            pool.releaseObject(s.pop());
        assert(count == pool.getMaxSize());
        pool.shutDown();
    }

    public void testShutdown() {
        class MyFactory extends PoolObjectFactory {
            private HashSet objects = new HashSet();

            public Object createObject() {
                Object o = new Object();
                objects.add(o);
                return o;
            }

            public void deleteObject(Object o) {
                objects.remove(o);
            }

            public boolean isEmpty() {
                return objects.isEmpty();
            }
        };
        MyFactory fac = new MyFactory();
        Stack s = new Stack();
        pool.setName(name);
        pool.setObjectFactory(fac);
        pool.setMinSize(0);
        pool.setMaxSize(10);
        pool.setBlocking(false);
        pool.initialize();
        for(Object o = pool.getObject(); o != null; o = pool.getObject()) {
            s.push(o);
        }
        assert(!fac.isEmpty());
        pool.shutDown();
        assert(fac.isEmpty());
    }

    public void testLastUpdatesEnabled() {
        startPool();
        pool.setMinSize(0);
        pool.setMaxSize(5);
        pool.setBlocking(false);
        pool.setTimestampUsed(true);
        pool.initialize();
        testLastUpdates();
    }

    public void testLastUpdatesDisabled() {
        startPool();
        pool.setMinSize(0);
        pool.setMaxSize(5);
        pool.setBlocking(false);
        pool.setTimestampUsed(false);
        pool.initialize();
        testLastUpdates();
    }

    private void testLastUpdates() {
        Stack s = new Stack();
        for(Object o = pool.getObject(); o != null; o = pool.getObject()) {
            s.push(o);
            pool.setLastUsed(o);
        }
        try {
            pool.setLastUsed(new Object());
            if(pool.isTimestampUsed())
                fail("Pool set last used time for object not in the pool!");
        } catch(IllegalArgumentException e) {}
        while(!s.isEmpty()) {
            Object o = s.pop();
            pool.setLastUsed(o);
            pool.releaseObject(o);
            try {
                pool.setLastUsed(o);
                if(pool.isTimestampUsed())
                    fail("Pool set last used time for object not currently in use!");
            } catch(IllegalStateException e) {}
        }
        pool.shutDown();
    }

    public void testPoolBlocking() {
        final Stack s = new Stack();
        startPool();
        pool.setMinSize(0);
        pool.setMaxSize(5);
        pool.setBlocking(true);
        pool.initialize();
        class LocalRun implements Runnable { // Need something to unblock
            private boolean done = false;    // the main thread periodically
            public void run() {
                while(!done) {
                    try {
                        Thread.currentThread().sleep(500);
                    } catch(InterruptedException e) {}
                    Object o = s.pop();
                    pool.releaseObject(o);
                    if(s.isEmpty()) {
                        done = true;
                        synchronized(this) {notify();}
                    }
                }
            }
            public synchronized void waitForFinish() {
                while(!done)
                    try {
                        wait();
                    } catch(InterruptedException e) {}
            }
        };
        LocalRun r = new LocalRun();
        s.push(pool.getObject());
        new Thread(r).start();
        for(int i=1; i<8; i++) {
            Object o = pool.getObject();
            assert(o != null);
            s.push(o);
        }
        r.waitForFinish();
        pool.shutDown();
    }

    /**
     * Does a random walk of adding/removing for a lot of steps.  Tests that
     * there are not more objects created than the max pool size, that objects
     * are null if and only if the pool is fully utilized, etc.
     */
    public void testManyGets() {
        final int MAX = 10000;
        Stack s = new Stack();
        HashSet h = new HashSet();
        Random r = new Random();
        boolean add = true;

        startPool();
        pool.setMinSize(0);
        pool.setMaxSize(5);
        pool.setBlocking(false);
        pool.initialize();

        for(int i=0; i<MAX; i++) {
            if(s.size() == 0) add = true;
            else add = r.nextBoolean();
            if(add) {
                Object o = pool.getObject();
                assert((s.size() == pool.getMaxSize() && o == null) ||
                       (s.size() < pool.getMaxSize() && o != null));
                if(o == null) continue;
                s.push(o);
                if(!h.contains(o)) h.add(o);
                assert(h.size() <= pool.getMaxSize());
            } else {
                pool.releaseObject(s.pop());
            }
        }

        while(!s.empty())
            pool.releaseObject(s.pop());
        pool.shutDown();
    }

    public void testSetsAfterInit() {
        startPool();
        pool.setBlocking(false);
        pool.setGCEnabled(false);
        pool.setGCInterval(1000l);
        pool.setGCMinIdleTime(1000l);
        try {
            pool.setLogWriter(new PrintWriter(System.out));
        } catch(SQLException e) {
            fail(e.getMessage());
        }
        pool.setMaxSize(10);
        pool.setMinSize(4);
        pool.setShrinkingEnabled(false);
        pool.setShrinkMinIdleTime(1000l);
        pool.setShrinkPercent(0.5f);
        pool.setTimestampUsed(false);
        pool.initialize();
        try {
            pool.setBlocking(true);
            fail("Pool allowed setBlocking after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setGCEnabled(true);
            fail("Pool allowed setGCEnabled after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setGCInterval(2000l);
            fail("Pool allowed setGCInterval after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setGCMinIdleTime(2000l);
            fail("Pool allowed setGCMinIdleTime after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setLogWriter(new PrintWriter(System.out));
            fail("Pool allowed setLogWriter after initialize!");
        } catch(SQLException e) {
            fail(e.getMessage());
        } catch(IllegalStateException e) {
        }
        try {
            pool.setMaxSize(5);
            fail("Pool allowed setMax after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setMinSize(3);
            fail("Pool allowed setMinSize after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setShrinkingEnabled(true);
            fail("Pool allowed setShrinkingEnabled after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setShrinkMinIdleTime(2000l);
            fail("Pool allowed setShrinkMinIdleTime after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setShrinkPercent(0.2f);
            fail("Pool allowed setShrinkPercent after initialize!");
        } catch(IllegalStateException e) {}
        try {
            pool.setTimestampUsed(true);
            fail("Pool allowed setTimestampUsed after initialize!");
        } catch(IllegalStateException e) {}
        pool.shutDown();
    }
}