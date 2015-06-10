#!/bin/bash

HOME=/home/heitor/Projetos/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar br.bireme.tmp.AddPrettyField $1 $2

