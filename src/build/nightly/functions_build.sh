#!/bin/sh

# Config: ZIPDIR CVSROOT ANT_HOME JAVA_HOME
# PATH 
# JDK11_HOME JDK12_HOME

# Defaults 
if [ "$WS" = "" ] ; then
   WS=$HOME/ws
fi

if [ "$JAVA_HOME" = "" ] ; then
   JAVA_HOME=$HOME/java/java1.2
fi

ZIP=zip
ZIPDIR=$WS/zip
LOGDIR=$WS/log
CVSROOT=:pserver:anoncvs@jakarta.apache.org:/home/cvspublic
PATH=$HOME/bin:$HOME/bin/nightly:/usr/local/bin:$PATH
JSSE=$HOME/java/jsse
EXTENSIONS=$JSSE/jsse.jar:$JSSE/jnet.jar:$JSSE/jcert.jar:.
ANT_HOME=$HOME/opt/ant-1.2

if [ ! -d $WS ] ; then 
    mkdir $WS
fi 
if [ ! -d $LOGDIR ] ; then 
   mkdir $LOGDIR
fi
if [ ! -d $ZIPDIR ] ; then 
   mkdir $ZIPDIR
fi

export JAVA_HOME
export ZIP
export ZIPDIR
export LOGDIR
export CVSROOT
export PATH
export JSSE
export EXTENSIONS
export ANT_HOME

#Override
if [ -f $HOME/.nightlyrc ] ; then
   . $HOME/.nightlyrc
fi

## Make sure all dirs exists
mkdir -p $ZIPDIR
mkdir -p $LOGDIR

help_nightly() {
    # nothing yet
    echo 
}


check() {
    # Java version
    # JSSE
    # Ant 1, 2
    echo
}


# functions

cvs_get() {
  MOD=$1
  TAG=$2

  cd $WS
  rm -rf $MOD
  echo cvs -d $CVSROOT co $TAG $MOD
  cvs co $TAG $MOD >$LOGDIR/cvs-get-$MOD.log 2>&1
  wc $LOGDIR/cvs-get-$MOD.log
  tail -5 $LOGDIR/cvs-get-$MOD.log
  echo 
}

cvs_update() {
  MOD=$1
  TAG=$2

  cd $WS/$MOD
  echo cvs -d -P $CVSROOT update 
  cvs update >$LOGDIR/cvs-update-$MOD.log 2>&1
  wc $LOGDIR/cvs-update-$MOD.log
  tail -5 $LOGDIR/cvs-update-$MOD.log
  echo 
}

ant_build() {
  MOD=$1
  DIST=$2
  LOG=$LOGDIR/$3
  TARGET=$4
  # ANT_HOME
  # JAVA_HOME

  if [ "$TARGET" = "" ] ; 	
	then  TARGET=dist ; fi

  echo ---------- $DIST BUILD `date` ---------- 
  echo JAVA_HOME=$JAVA_HOME
  echo LOG=$LOG
  echo WS=$WS/$MOD
  echo CLASSPATH=$CLASSPATH 
  echo 

  echo ---------- $DIST BUILD `date` ---------- >> $LOG 2>&1 
  echo JAVA_HOME=$JAVA_HOME >> $LOG 2>&1
  echo LOG=$LOG >> $LOG 2>&1
  echo WS=$WS/$MOD >> $LOG 2>&1
  echo CLASSPATH=$CLASSPATH >> $LOG 2>&1
  echo  >> $LOG 2>&1
  $JAVA_HOME/bin/java -version  >> $LOG 2>&1

  cd $WS/$MOD
  echo rm -rf $WS/build/$DIST >> $LOG 2>&1
  echo rm -rf $WS/dist/$DIST  >> $LOG 2>&1
  rm -rf $WS/build/$DIST >> $LOG 2>&1
  rm -rf $WS/dist/$DIST  >> $LOG 2>&1

  JAVACMD=$JAVA_HOME/bin/java
  export JAVACMD
  echo Building with $ANT_HOME/bin/ant $TARGET 2>&1 >> $LOG
  $ANT_HOME/bin/ant $TARGET 2>&1 >> $LOG

  grep "BUILD SUCCESSFUL" $LOG 
  if [ "$?" != "0" ]; then 
    echo BUILD FAILED. 
    echo ---------- HEAD:
    head -10 $LOG
    echo ---------- TAIL:
    tail -20 $LOG
    echo ----------
    echo 
  fi
  echo ---------- DONE $DIST BUILD `date` ---------- 
}

zip_src() {
  MOD=$1
  ZIPNAME=$2

  cd $WS
  echo 
  echo zip -r  $ZIPNAME $MOD
  zip -r $MOD >/dev/null
  mv zip.zip $ZIPDIR/$ZIPNAME
  echo 
}

zip_dist() {
  DIST=$1
  ARCH=$2

  echo 
  echo Creating $DIST distribution in $ZIPDIR:
  echo $ARCH.tar.gz $ARCH.zip
  cd $WS/dist

  rm -f $ZIPDIR/$ARCH.tar >/dev/null 2>&1
  rm -f $ZIPDIR/$ARCH.tar.gz >/dev/null 2>&1
  tar cvf $ZIPDIR/$ARCH.tar $DIST >/dev/null 2>&1
  gzip $ZIPDIR/$ARCH.tar
  rm $ZIPDIR/$ARCH.zip >/dev/null 2>&1
  zip  -r $DIST >/dev/null
  mv zip.zip  $ZIPDIR/$ARCH.zip
  echo 
}	

fix_tomcat() {

  cp $ANT_HOME/lib/parser.jar $WS/dist/tomcat/lib
  cp $ANT_HOME/lib/jaxp.jar $WS/dist/tomcat/lib
}

## Will build tomcat and zip the result
build_tomcat() {
  SUFIX=$1
  TARGET=$2
  
  ant_build jakarta-tomcat tomcat tomcat-build-$SUFIX.log $TARGET
  zip_dist tomcat tomcat-$SUFIX 
}

## Will count the errors in a watchdog script
count_errors() {
    BASELOG=$1

    grep -v "^OK" $BASELOG.log  | \
      grep -v "The args attribute" | grep -v "In compareWeek" \
      > $BASELOG-errors.log

    ERRORS=`grep FAIL $BASELOG-errors.log | grep -v "FAIL\*\*\*" |wc -l`
    OK=`grep "^OK" $BASELOG.log |wc -l`

    echo 
    echo  LOG=$BASELOG.log
    echo   OK: $OK ERRORS: $ERRORS
    echo 
    echo  Watchdog tail: 
    echo --------------------
    tail -10 $BASELOG.log
    echo  --------------------
}

