#!/bin/bash

#HOME=/home/javaapps/SocialCheckLinks/social-checklinks
HOME=/home/heitor/Projetos/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/lib/Bruma.jar br.bireme.scl.BrokenLinks $HOME/${1}_v8broken.txt $HOME/$1 localhost -outFileEncoding=ISO-8859-1 -outMstEncoding=ISO-8859-1 