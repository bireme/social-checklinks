# Seta ambiente para executar as aplicações
PATH=$PATH:/usr/local/bireme/procs:/usr/local/bin:/usr/bin:/bin:.
export PATH

. /usr/local/bireme/misc/profil_isis
. /usr/local/bireme/misc/profil_java

NOW=$(date +"%Y%m%d-%T")
HOME=/home/javaapps/SocialCheckLinks/social-checklinks

$HOME/sh/SocialCheckLinks.sh &> $HOME/history/nohup/nohup_$NOW

