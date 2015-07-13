#!/bin/bash

#HOME=/home/javaapps/SocialCheckLinks/social-checklinks
HOME=/home/heitor/Projetos/SocialCheckLinks/social-checklinks

java -ea -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/build/web/WEB-INF/lib/Bruma.jar br.bireme.scl.ShowBrokenLinks $1 $2 $3 $4 $5 $6
