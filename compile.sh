#!/bin/bash

export CLASSPATH=`for i in $PWD/lib/*.jar ; do echo -n $i\; ; done`
ant $@
