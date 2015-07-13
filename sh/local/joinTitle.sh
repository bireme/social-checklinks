#!/bin/bash

HOME=/home/heitor/Projetos/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/build/web/WEB-INF/lib/Bruma.jar br.bireme.scl.JoinTitle $1 $2 $3 $4 $5 $6 $7 $8 $9 ${10}
