#!/bin/bash

################################################################################
# Prerequisitos:
# - ter a base de dados Isis - LILACS no diretório $HOME
# - ter a base de dados MongoDb - SocialCheckLinks com a coleção HistoryBrokenLinks
################################################################################


SERVER=production    # test homolog production
HOME=/home/javaapps/SocialCheckLinks/social-checklinks
NOW=$(date +"%Y%m%d")

echo "Bloqueia escrita na interface web do Social Check Links"
$HOME/sh/$SERVER/setReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = true</h1>" out > gout
if [ ! -s gout ] 
then
     sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Bloqueia escrita na interface web do Social Check Links" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
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
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Gera gizmo a partir do Social Check Links" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
    exit 1
fi

echo "Transfere a LILACS do serverABD para serverOFI$"
scp $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.mst $HOME
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a LILACS do serverABD para serverOFI$" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
    exit 1
fi
scp $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.xrf $HOME
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a LILACS do serverABD para serverOFI$" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
    exit 1
fi

echo "Cria arquivo com versoes anteriores"
if [ -e $HOME/LILACSurl.mst ]; then
    rm $HOME/LILACSurl.mst
fi
if [ -e $HOME/LILACSurl.xrf ]; then
    rm $HOME/LILACSurl.xrf
fi

mx LILACS "proc='d*' if p(v8) then '<2>'v2'</2>',('<8>'v8'</8>') else 'd.' fi" -all now append=$HOME/LILACSurl
tar -czpvf $HOME/history/LILACS_$NOW.tgz $HOME/LILACSurl.mst $HOME/LILACSurl.xrf $HOME/Gv8broken.giz

echo "Executa gizmo na LILACS"
$HOME/sh/gizmo2Isis.sh $HOME/Gv8broken.giz $HOME/cipar.par $HOME/other
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Executa gizmo na LILACS" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
    exit 1
fi

mv $HOME/LILACS.mst $HOME/LILACS_$NOW.mst
mv $HOME/LILACS.xrf $HOME/LILACS_$NOW.xrf
mv $HOME/other/LILACS.mst $HOME/LILACS.mst
mv $HOME/other/LILACS.xrf $HOME/LILACS.xrf

echo "Inicia pocesso de checagem de links"
/usr/local/bireme/java/FisChecker/genv8notfound.sh LILACS lil

echo "Testa se o numero de links quebrados esta na margem de 10% comparado com a verificacao anterior"
val1=$(cat $HOME/broken.txt)
val2=$(wc -l /usr/local/bireme/java/FisChecker/output/LILACS_out/bases/LILACS_v8broken.txt | grep -Po "^\d+")
res=$(echo $val1 $val2 | bc $HOME/sh/insideLimits.bc)

if [ $res -eq 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Numero de links quebrados [$val2] verificados variou em mais de 10% desde a ultima checagem" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
    exit 1
fi
echo $val2 > $HOME/broken.txt

echo "Alimenta os links quebrados no Social Check Links"
$HOME/sh/$SERVER/loadLilacsBrokenLinks.sh
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Alimenta os links quebrados no Social Check Links" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
    exit 1
fi

echo "Desbloqueia escrita na interface web do Social Check Links"
$HOME/sh/$SERVER/resetReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = false</h1>" out > gout
if [ ! -s gout ]
then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Desbloqueia escrita na interface web do Social Check Links" -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br
    rm out gout
    exit 1
fi
rm out gout

echo "fim!"
