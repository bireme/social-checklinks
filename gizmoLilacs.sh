#!/bin/bash

HOME=/usr/local/bireme/java/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.11.2.jar br.bireme.scl.Gizmo ts01vm.bireme.br /bases/lilG4/lil.lil/LILACS_v8broken.giz -encoding=IBM850
