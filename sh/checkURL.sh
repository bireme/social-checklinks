#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes br.bireme.scl.CheckUrl $1
