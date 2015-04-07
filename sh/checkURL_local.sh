#!/bin/bash

HOME=/home/heitor/Projetos/SocialCheckLinks/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes br.bireme.scl.CheckUrl $1
