package org.apache.joran.action;

import org.apache.joran.ExecutionContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;
import org.w3c.dom.Element;

import java.lang.reflect.Method;

public class LevelAction extends Action {

  final static Logger logger = Logger.getLogger(LevelAction.class);

  static final String VALUE_ATTR = "value";
	static final String CLASS_ATTR = "class";
  static final String INHERITED = "INHERITED";
  static final String NULL = "NULL";
  static final String EMPTY_STR = "";

  static final Class[] ONE_STRING_PARAM = new Class[] { String.class };

  public void begin(ExecutionContext ec, Element element) {
    
		Object o = ec.peekObject();
		
		if(!(o instanceof Logger)) {
			logger.warn("Could not find a logger at the top of execution stack.");
			inError = true; 
			ec.addError("For element <level>, could not find a logger at the top of execution stack.");
			return;
		}
    Logger l = (Logger) o;
    
    
    String loggerName = l.getName();

    String levelStr = element.getAttribute(VALUE_ATTR);
    logger.debug(
      "Level value for logger [" + loggerName + "] is  [" + levelStr + "].");

    if (INHERITED.equalsIgnoreCase(levelStr)
      || NULL.equalsIgnoreCase(levelStr)) {
      l.setLevel(null);
    } else {

      String className = element.getAttribute(CLASS_ATTR);

      if (EMPTY_STR.equals(className)) {
        l.setLevel(OptionConverter.toLevel(levelStr, Level.DEBUG));
      } else {
        logger.debug("Desired Level sub-class: [" + className + ']');

        try {
          Class clazz = Loader.loadClass(className);
          Method toLevelMethod = clazz.getMethod("toLevel", ONE_STRING_PARAM);
          Level pri =
            (Level) toLevelMethod.invoke(null, new Object[] { levelStr });
          l.setLevel(pri);
        } catch (Exception oops) {
          logger.error(
            "Could not create level [" + levelStr + "]. Reported error follows.",
            oops);
          return;
        }
      }
    }

    logger.debug(loggerName + " level set to " + l.getLevel());

  }

  public void end(ExecutionContext ec, Element e) {
  }

  public void finish(ExecutionContext ec) {
  }
}
