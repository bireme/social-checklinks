#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/build/web/WEB-INF/lib/Bruma.jar br.bireme.scl.Gizmo2Isis $1 $2 $3 $4 $5
