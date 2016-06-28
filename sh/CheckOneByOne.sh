#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks
CHECK_LINKS=/home/javaapps/sbt-projects/CheckLinks

#$CHECK_LINKS_SERVER=heitor.barbieri@ts30vm.bireme.br
#ssh ${CHECK_LINKS_SERVER} "${CHECK_LINKS}/CheckOneByOne.sh"

${CHECK_LINKS}/CheckOneByOne.sh

