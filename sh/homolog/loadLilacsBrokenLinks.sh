#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.11.2.jar:$HOME/lib/Bruma.jar br.bireme.scl.BrokenLinks /usr/local/bireme/java/FisChecker/output/LILACS_out/bases/LILACS_v8broken.txt $HOME/LILACS hm02vm.bireme.br -outFileEncoding=IBM850 -outMstEncoding=IBM850 
