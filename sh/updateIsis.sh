#!/bin/bash

#HOME=/home/javaapps/SocialCheckLinks/social-checklinks
HOME=/home/heitor/Projetos/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/build/web/WEB-INF/lib/Bruma.jar:$HOME/build/web/WEB-INF/lib/mongo-java-driver-2.12.4.jar  br.bireme.scl.UpdateIsis $1 $2 $3 $4 $5
