#!/bin/bash

export CLASSPATH=`for i in $PWD/ext/*.jar ; do echo -n $i\; ; done`
ant $@
