#!/bin/bash

#HOME=/home/javaapps/SocialCheckLinks/social-checklinks
HOME=/home/heitor/Projetos/SocialCheckLinks/social-checklinks

java -ea -cp $HOME/build/web/WEB-INF/classes:$HOME/lib/mongo-java-driver-2.12.4.jar:$HOME/lib/javax.mail.jar:$HOME/lib/SendEmail.jar br.bireme.scl.SendEmailToCcs $1 $2 $3 $4 $5 $6 $7
