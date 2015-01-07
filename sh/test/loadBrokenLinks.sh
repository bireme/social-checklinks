#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/lib/Bruma.jar br.bireme.scl.BrokenLinks /usr/local/bireme/java/FisChecker/output/${1}_out/bases/${1}_v8broken.txt $HOME/$1 ts01vm.bireme.br -outFileEncoding=IBM850 -outMstEncoding=IBM850 
