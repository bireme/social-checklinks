#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.1.jar br.bireme.scl.Gizmo ts01vm.bireme.br $HOME/Gv8broken.giz -encoding=IBM850
