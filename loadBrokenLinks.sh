#!/bin/bash

HOME=/usr/local/bireme/java/social-checklinks

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.11.2.jar:$HOME/lib/Bruma.jar br.bireme.scl.BrokenLinks $1 $2 $3 $4 $5 $6 $7 $8
