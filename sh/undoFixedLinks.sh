#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/build/web/WEB-INF/lib/Bruma.jar br.bireme.scl.UndoUpdate $1 $2 $3 $4 $5
