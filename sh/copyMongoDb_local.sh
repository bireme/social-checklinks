#!/bin/bash

HOME=../

java -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/lib/Bruma.jar br.bireme.scl.CopyMongoDb $1 $2 $3 $4 $5 $6 $7 $8