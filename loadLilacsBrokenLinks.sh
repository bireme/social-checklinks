#!/bin/bash

HOME=/usr/local/bireme/java/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.11.2.jar:$HOME/lib/Bruma.jar br.bireme.scl.BrokenLinks /bases/lilG4/lil.lil/LILACS_v8broken.txt /bases/lilG4/lil.lil/lilacs ts01vm.bireme.br -outFileEncoding=IBM850 -outMstEncoding=IBM850
