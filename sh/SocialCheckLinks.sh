#!/bin/bash


SERVER=homolog    # test homolog production
HOME=/home/javaapps/SocialChecklinks/social-checklinks

echo "Bloqueia escrita na interface web do Social Check Links"
$HOME/sh/$SERVER/setReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = true</h1>" out > gout
if [ ! -s gout ] 
then
    #envia email
    rm out gout    
    exit 1
fi	
rm out gout

echo "Espera por 5 minutos para garantir o termino de todas as correcoes iniciadas"
sleep 5m

echo "Gera gizmo a partir do Social Check Links"
$HOME/sh/$SERVER/gizmoLilacs.sh
if [ $? -ne 0 ]; then    	
    #envia email
    exit 1
fi

echo "Transfere gizmo para serverabd2"
scp $HOME/Gv8broken.giz $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/SocialCheckLinks
if [ $? -ne 0 ]; then
    #envia email
    exit 1
fi

echo "Executa gizmo na LILACS no servidor serverABD"
ssh $TRANSFER@serverabd2.bireme.br /home/lilacs/www/bases/lildbi/dbcertif/lilacs/SocialCheckLinks/trocaURL.sh

echo "Espera por 5 minutos para o processamento do serverABD terminar"
sleep 5m

echo "Transfere a LILACS do serverABD para serverOFI$"
scp $HOME/Gv8broken.giz $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.mst $HOME
if [ $? -ne 0 ]; then
    #envia email
    exit 1
fi
scp $HOME/Gv8broken.giz $TRANSFER@serverabd2.bireme.br:/home/lilacs/www/bases/lildbi/dbcertif/lilacs/LILACS.xrf $HOME
if [ $? -ne 0 ]; then
    #envia email
    exit 1
fi

echo "fim!"
