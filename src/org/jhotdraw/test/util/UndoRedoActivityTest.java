package CH.ifa.draw.test.util;

import java.awt.Point;

// JUnitDoclet begin import
import CH.ifa.draw.figures.RectangleFigure;
import CH.ifa.draw.standard.PasteCommand;
import CH.ifa.draw.standard.SingleFigureEnumerator;
import CH.ifa.draw.test.JHDTestCase;
import CH.ifa.draw.util.UndoRedoActivity;
import CH.ifa.draw.util.Undoable;
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
* TestCase UndoRedoActivityTest is generated by
* JUnitDoclet to hold the tests for UndoRedoActivity.
* @see CH.ifa.draw.util.UndoRedoActivity
*/
// JUnitDoclet end javadoc_class
public class UndoRedoActivityTest
// JUnitDoclet begin extends_implements
extends JHDTestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  // instance variables, helper methods, ... put them in this marker
  CH.ifa.draw.util.UndoRedoActivity undoredoactivity = null;
  // JUnitDoclet end class
  
  /**
  * Constructor UndoRedoActivityTest is
  * basically calling the inherited constructor to
  * initiate the TestCase for use by the Framework.
  */
  public UndoRedoActivityTest(String name) {
    // JUnitDoclet begin method UndoRedoActivityTest
    super(name);
    // JUnitDoclet end method UndoRedoActivityTest
  }
  
  /**
  * Factory method for instances of the class to be tested.
  */
  public CH.ifa.draw.util.UndoRedoActivity createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
	Undoable undoActivity = new PasteCommand.UndoActivity(getDrawingEditor().view());
	undoActivity.setAffectedFigures(new SingleFigureEnumerator(new RectangleFigure(new Point(33,33), new Point(44,44))));
    return (UndoRedoActivity)UndoRedoActivity.createUndoRedoActivity(undoActivity);
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
    undoredoactivity = createInstance();
    // JUnitDoclet end method testcase.setUp
  }
  
  /**
  * Method tearDown is overwriting the framework method to
  * clean up after each single test of this TestCase.
  * It's called from the JUnit framework only.
  */
  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    undoredoactivity = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }
  
  // JUnitDoclet begin javadoc_method undo()
  /**
  * Method testUndo is testing undo
  * @see CH.ifa.draw.util.UndoRedoActivity#undo()
  */
  // JUnitDoclet end javadoc_method undo()
  public void testUndo() throws Exception {
    // JUnitDoclet begin method undo
    // JUnitDoclet end method undo
  }
  
  // JUnitDoclet begin javadoc_method redo()
  /**
  * Method testRedo is testing redo
  * @see CH.ifa.draw.util.UndoRedoActivity#redo()
  */
  // JUnitDoclet end javadoc_method redo()
  public void testRedo() throws Exception {
    // JUnitDoclet begin method redo
    // JUnitDoclet end method redo
  }
  
  // JUnitDoclet begin javadoc_method setUndoable()
  /**
  * Method testSetIsUndoable is testing setUndoable
  * and isUndoable together by setting some value
  * and verifying it by reading.
  * @see CH.ifa.draw.util.UndoRedoActivity#setUndoable(boolean)
  * @see CH.ifa.draw.util.UndoRedoActivity#isUndoable()
  */
  // JUnitDoclet end javadoc_method setUndoable()
  public void testSetIsUndoable() throws Exception {
    // JUnitDoclet begin method setUndoable isUndoable
    boolean[] tests = {true, false};
    
    for (int i = 0; i < tests.length; i++) {
      undoredoactivity.setUndoable(tests[i]);
      assertEquals(tests[i], undoredoactivity.isUndoable());
    }
    // JUnitDoclet end method setUndoable isUndoable
  }
  
  // JUnitDoclet begin javadoc_method setRedoable()
  /**
  * Method testSetIsRedoable is testing setRedoable
  * and isRedoable together by setting some value
  * and verifying it by reading.
  * @see CH.ifa.draw.util.UndoRedoActivity#setRedoable(boolean)
  * @see CH.ifa.draw.util.UndoRedoActivity#isRedoable()
  */
  // JUnitDoclet end javadoc_method setRedoable()
  public void testSetIsRedoable() throws Exception {
    // JUnitDoclet begin method setRedoable isRedoable
    boolean[] tests = {true, false};
    
    for (int i = 0; i < tests.length; i++) {
      undoredoactivity.setRedoable(tests[i]);
      assertEquals(tests[i], undoredoactivity.isRedoable());
    }
    // JUnitDoclet end method setRedoable isRedoable
  }
  
  // JUnitDoclet begin javadoc_method setAffectedFigures()
  /**
  * Method testSetGetAffectedFigures is testing setAffectedFigures
  * and getAffectedFigures together by setting some value
  * and verifying it by reading.
  * @see CH.ifa.draw.util.UndoRedoActivity#setAffectedFigures(CH.ifa.draw.framework.FigureEnumeration)
  * @see CH.ifa.draw.util.UndoRedoActivity#getAffectedFigures()
  */
  // JUnitDoclet end javadoc_method setAffectedFigures()
  public void testSetGetAffectedFigures() throws Exception {
    // JUnitDoclet begin method setAffectedFigures getAffectedFigures
    CH.ifa.draw.framework.FigureEnumeration[] tests = {new SingleFigureEnumerator(new RectangleFigure(new Point(44,44), new Point(55,55))), null};
    
    for (int i = 0; i < tests.length; i++) {
      undoredoactivity.setAffectedFigures(tests[i]);
      assertEquals(tests[i], undoredoactivity.getAffectedFigures());
    }
    // JUnitDoclet end method setAffectedFigures getAffectedFigures
  }
  
  // JUnitDoclet begin javadoc_method getAffectedFiguresCount()
  /**
  * Method testGetAffectedFiguresCount is testing getAffectedFiguresCount
  * @see CH.ifa.draw.util.UndoRedoActivity#getAffectedFiguresCount()
  */
  // JUnitDoclet end javadoc_method getAffectedFiguresCount()
  public void testGetAffectedFiguresCount() throws Exception {
    // JUnitDoclet begin method getAffectedFiguresCount
    // JUnitDoclet end method getAffectedFiguresCount
  }
  
  // JUnitDoclet begin javadoc_method getDrawingView()
  /**
  * Method testGetDrawingView is testing getDrawingView
  * @see CH.ifa.draw.util.UndoRedoActivity#getDrawingView()
  */
  // JUnitDoclet end javadoc_method getDrawingView()
  public void testGetDrawingView() throws Exception {
    // JUnitDoclet begin method getDrawingView
    // JUnitDoclet end method getDrawingView
  }
  
  // JUnitDoclet begin javadoc_method release()
  /**
  * Method testRelease is testing release
  * @see CH.ifa.draw.util.UndoRedoActivity#release()
  */
  // JUnitDoclet end javadoc_method release()
  public void testRelease() throws Exception {
    // JUnitDoclet begin method release
    // JUnitDoclet end method release
  }
  
  // JUnitDoclet begin javadoc_method getReversedActivity()
  /**
  * Method testGetReversedActivity is testing getReversedActivity
  * @see CH.ifa.draw.util.UndoRedoActivity#getReversedActivity()
  */
  // JUnitDoclet end javadoc_method getReversedActivity()
  public void testGetReversedActivity() throws Exception {
    // JUnitDoclet begin method getReversedActivity
    // JUnitDoclet end method getReversedActivity
  }
  
  // JUnitDoclet begin javadoc_method createUndoRedoActivity()
  /**
  * Method testCreateUndoRedoActivity is testing createUndoRedoActivity
  * @see CH.ifa.draw.util.UndoRedoActivity#createUndoRedoActivity(CH.ifa.draw.util.Undoable)
  */
  // JUnitDoclet end javadoc_method createUndoRedoActivity()
  public void testCreateUndoRedoActivity() throws Exception {
    // JUnitDoclet begin method createUndoRedoActivity
    // JUnitDoclet end method createUndoRedoActivity
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
    junit.textui.TestRunner.run(UndoRedoActivityTest.class);
    // JUnitDoclet end method testcase.main
  }
}
