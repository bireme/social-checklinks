#!/bin/bash

HOME=/home/javaapps/SocialChecklinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.11.2.jar
 br.bireme.scl.Gizmo mongodb.bireme.br /bases/lilG4/lil.lil/Gv8broken.giz -encoding=IBM850
