#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.1.jar:$HOME/lib/Bruma.jar br.bireme.scl.BrokenLinks $1 $2 $3 $4 $5 $6 $7 $8
