#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. HEAD, GROUPER_UI_1_3_1, etc"
  echo "e.g. buildGrouperUi.sh HEAD"
  echo
  exit 1
fi  

cd /tmp
if [ ! -d /home/mchyzer/tmp/grouperUi ]; then
  /bin/mkdir /home/mchyzer/tmp/grouperUi
  /bin/chmod g+w /home/mchyzer/tmp/grouperUi
fi

cd /home/mchyzer/tmp/grouperUi

export buildDir=/home/mchyzer/tmp/grouperUi/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

export CVSROOT=/home/cvs/i2mi

/usr/bin/cvs export -r $1 grouper-ui

cd $buildDir/grouper-ui

/home/mchyzer/ant/bin/ant distPackage

echo
echo "result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
