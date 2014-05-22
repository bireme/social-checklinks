#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/Bruma.jar br.bireme.scl.Gizmo2Isis $1 $2 $3
