#!/usr/bin/python

from org.columba.core.action import AbstractColumbaAction

class PythonHelloWorldAction(AbstractColumbaAction):
	def __init__(self, controller):
		FrameAction.__init__(self, controller,"Python Hello World", "This is the long version", "Show me this tooltip", "PYTHON_HELLO_WORLD", None, None, 0, None)
		
	def actionPerformed(self, event):
		print "Python Hello World!"
		
		
	
