#!/bin/bash

################################################################################
# Prerequisitos:
# - ter a base de dados Isis - LILACS no diretório $HOME
# - ter a base de dados MongoDb - SocialCheckLinks com a coleção HistoryBrokenLinks
################################################################################


SERVER=production    # test homolog production
HOME=/home/javaapps/SocialCheckLinks/social-checklinks


echo "Transfere a LILACS do serverABD para serverOFI$"
scp $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.mst $HOME
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a LILACS do serverABD para serverOFI$" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi
scp $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.xrf $HOME
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a LILACS do serverABD para serverOFI$" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi

echo "Inicia pocesso de checagem de links"
/usr/local/bireme/java/FisChecker/genv8notfound.sh LILACS lil

echo "Alimenta os links quebrados no Social Check Links"
$HOME/sh/$SERVER/loadLilacsBrokenLinks.sh
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Alimenta os links quebrados no Social Check Links" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    exit 1
fi

echo "Desbloqueia escrita na interface web do Social Check Links"
$HOME/sh/$SERVER/resetReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = false</h1>" out > gout
if [ ! -s gout ]
then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Desbloqueia escrita na interface web do Social Check Links" -t lilacsdb@bireme.org -cc marcelo.bottura@bireme.org fabio.brito@bireme.org -s esmeralda.bireme.br
    rm out gout
    exit 1
fi
rm out gout

echo "fim!"
