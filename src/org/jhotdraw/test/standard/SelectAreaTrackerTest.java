package CH.ifa.draw.test.standard;

// JUnitDoclet begin import
import CH.ifa.draw.test.JHDTestCase;
// JUnitDoclet end import

/*
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


// JUnitDoclet begin javadoc_class
/**
* TestCase SelectAreaTrackerTest is generated by
* JUnitDoclet to hold the tests for SelectAreaTracker.
* @see CH.ifa.draw.standard.SelectAreaTracker
*/
// JUnitDoclet end javadoc_class
public class SelectAreaTrackerTest
// JUnitDoclet begin extends_implements
extends JHDTestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  // instance variables, helper methods, ... put them in this marker
  CH.ifa.draw.standard.SelectAreaTracker selectareatracker = null;
  // JUnitDoclet end class
  
  /**
  * Constructor SelectAreaTrackerTest is
  * basically calling the inherited constructor to
  * initiate the TestCase for use by the Framework.
  */
  public SelectAreaTrackerTest(String name) {
    // JUnitDoclet begin method SelectAreaTrackerTest
    super(name);
    // JUnitDoclet end method SelectAreaTrackerTest
  }
  
  /**
  * Factory method for instances of the class to be tested.
  */
  public CH.ifa.draw.standard.SelectAreaTracker createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
    return new CH.ifa.draw.standard.SelectAreaTracker(getDrawingEditor());
    // JUnitDoclet end method testcase.createInstance
  }
  
  /**
  * Method setUp is overwriting the framework method to
  * prepare an instance of this TestCase for a single test.
  * It's called from the JUnit framework only.
  */
  protected void setUp() throws Exception {
    // JUnitDoclet begin method testcase.setUp
    super.setUp();
    selectareatracker = createInstance();
    // JUnitDoclet end method testcase.setUp
  }
  
  /**
  * Method tearDown is overwriting the framework method to
  * clean up after each single test of this TestCase.
  * It's called from the JUnit framework only.
  */
  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    selectareatracker = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }
  
  // JUnitDoclet begin javadoc_method mouseDown()
  /**
  * Method testMouseDown is testing mouseDown
  * @see CH.ifa.draw.standard.SelectAreaTracker#mouseDown(java.awt.event.MouseEvent, int, int)
  */
  // JUnitDoclet end javadoc_method mouseDown()
  public void testMouseDown() throws Exception {
    // JUnitDoclet begin method mouseDown
    // JUnitDoclet end method mouseDown
  }
  
  // JUnitDoclet begin javadoc_method mouseDrag()
  /**
  * Method testMouseDrag is testing mouseDrag
  * @see CH.ifa.draw.standard.SelectAreaTracker#mouseDrag(java.awt.event.MouseEvent, int, int)
  */
  // JUnitDoclet end javadoc_method mouseDrag()
  public void testMouseDrag() throws Exception {
    // JUnitDoclet begin method mouseDrag
    // JUnitDoclet end method mouseDrag
  }
  
  // JUnitDoclet begin javadoc_method mouseUp()
  /**
  * Method testMouseUp is testing mouseUp
  * @see CH.ifa.draw.standard.SelectAreaTracker#mouseUp(java.awt.event.MouseEvent, int, int)
  */
  // JUnitDoclet end javadoc_method mouseUp()
  public void testMouseUp() throws Exception {
    // JUnitDoclet begin method mouseUp
    // JUnitDoclet end method mouseUp
  }
  
  
  
  // JUnitDoclet begin javadoc_method testVault
  /**
  * JUnitDoclet moves marker to this method, if there is not match
  * for them in the regenerated code and if the marker is not empty.
  * This way, no test gets lost when regenerating after renaming.
  * <b>Method testVault is supposed to be empty.</b>
  */
  // JUnitDoclet end javadoc_method testVault
  public void testVault() throws Exception {
    // JUnitDoclet begin method testcase.testVault
    // JUnitDoclet end method testcase.testVault
  }
  
  /**
  * Method to execute the TestCase from command line
  * using JUnit's textui.TestRunner .
  */
  public static void main(String[] args) {
    // JUnitDoclet begin method testcase.main
    junit.textui.TestRunner.run(SelectAreaTrackerTest.class);
    // JUnitDoclet end method testcase.main
  }
}
