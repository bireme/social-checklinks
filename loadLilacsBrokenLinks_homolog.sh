#!/bin/bash

HOME=/home/heitor/Projetos/SocialChecklinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.11.2.jar:$HOME/lib/Bruma.jar br.bireme.scl.BrokenLinks /home/heitor/temp/LILACS_v8broken.txt /home/heitor/temp/lilacs ts01vm.bireme.br -outFileEncoding=IBM850 -outMstEncoding=IBM850
