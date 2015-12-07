#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/lib/httpcore-4.4.3.jar:$HOME/lib/httpclient-4.5.1.jar:$HOME/lib/commons-logging-1.2.jar:$HOME/build/web/WEB-INF/lib/Bruma.jar br.bireme.scl.BrokenLinks $HOME/$1_2brk.txt $HOME/$1 hm02vm.bireme.br -outFileEncoding=IBM850 -outMstEncoding=IBM850 $2
