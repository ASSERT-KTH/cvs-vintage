#!/usr/bin/python

import javax.swing as swing
from org.columba.core.action import AbstractColumbaAction

class PythonHelloWorldAction(AbstractColumbaAction):
	def __init__(self, controller):
		AbstractColumbaAction.__init__(self, controller,"Python Hello World")
		
	def actionPerformed(self, event):
		print "Python Hello World!"
		swing.JOptionPane.showMessageDialog(None,"Python Hello World!");
		
		
	
