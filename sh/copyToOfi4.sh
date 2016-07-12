#!/bin/bash

JAVA_HOME=/home/users/heitor.barbieri/jdk1.8.0_65
SBT_HOME=/home/users/heitor.barbieri/bin
PATH=$JAVA_HOME/bin:$SBT_HOME:$PATH
CHECK_LINKS=/home/javaapps/sbt-projects/CheckLinks
NOW=$(date +"%Y%m%d-%T")

cd $CHECK_LINKS
$SBT_HOME/sbt "run-main org.bireme.murl.MongoLastBrokenApp mongodb.bireme.br LILACS brokenLinks.txt ISO8859-1"
scp brokenLinks.txt transfer@serverofi4.bireme.br:/bases/lilG4/lil.lil/LILACS_v8broken_${NOW}.txt

rm brokenLinks.txt
cd - 
