#!/bin/bash

SERVER=homolog    # test homolog production
HOME=/home/javaapps/SocialCheckLinks/social-checklinks

echo "Bloqueia escrita na interface web do Social Check Links"
$HOME/sh/$SERVER/setReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = true</h1>" out > gout
if [ ! -s gout ] 
then
    #envia email
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Bloqueia escrita na interface web do Social Check Links" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    rm out gout    
    exit 1
fi	
rm out gout

echo "Espera por 5 minutos para garantir o termino de todas as correcoes iniciadas"
sleep 5m

echo "Apaga gizmo anterior"
if [ -e $HOME/Gv8broken.giz ]; then
    rm $HOME/Gv8broken.giz
fi

echo "Gera gizmo a partir do Social Check Links"
$HOME/sh/$SERVER/gizmoLilacs.sh
if [ $? -ne 0 ]; then    	
    #envia email
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Gera gizmo a partir do Social Check Links" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi

echo "Transfere gizmo para serverabd2"
scp $HOME/Gv8broken.giz $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/SocialCheckLinks
if [ $? -ne 0 ]; then
    #envia email
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere gizmo para serverabd2" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi

echo "Executa gizmo na LILACS no servidor serverABD"
ssh $TRANSFER@serverabd2.bireme.br /home/lilacs/www/bases/lildbi/dbcertif/lilacs/SocialCheckLinks/trocaURL.sh

echo "Transfere a LILACS do serverABD para serverOFI$"
scp $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.mst $HOME
if [ $? -ne 0 ]; then
    #envia email
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a LILACS do serverABD para serverOFI$" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi
scp $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.xrf $HOME
if [ $? -ne 0 ]; then
    #envia email
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a LILACS do serverABD para serverOFI$" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi

echo "Inicia pocesso de checagem de links"
/usr/local/bireme/java/FisChecker/genv8notfound.sh LILACS lil

echo "Alimenta os links quebrados no Social Check Links"
$HOME/sh/$SERVER/loadLilacsBrokenLinks.sh
if [ $? -ne 0 ]; then
    #envia email
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Alimenta os links quebrados no Social Check Links" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi

echo "Desbloqueia escrita na interface web do Social Check Links"
$HOME/sh/$SERVER/resetReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = false</h1>" out > gout
if [ ! -s gout ]
then
    #envia email
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Desbloqueia escrita na interface web do Social Check Links" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    rm out gout
    exit 1
fi
rm out gout

echo "fim!"
