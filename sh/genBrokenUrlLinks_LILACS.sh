#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks
MONGO_HOST=mongodb.bireme.br
LILACS_OFI=/bases/lilG4/lil.lil/LILACS

cd $HOME

echo "Transfere a base title de /home/lilacs/update/title/ para o diretorio corrente"
scp -p transfer@serverabd.bireme.br:/home/lilacs/update/title/title.{mst,xrf} .
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a base title de transfer@serverabd:/home/lilacs/update/title/title para o diretorio corrente." -t lilacsdb@bireme.org -cc ofi@bireme.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
    exit 1
fi

echo "Copia a base LILACS de ${LILACS_OFI} para diretorio corrente."
cp ${LILACS_OFI}.{mst,xrf} .

echo "Traz campo 930 da title para LILACS -> LILACSe"
sh/joinTitle.sh title 150 930 LILACS 30 930 LILACSe --inEncoding=ISO-8859-1 --mongoHost=$MONGO_HOST
rm title.{mst,xrf}

echo "Retira registros novos que ainda nao entraram no ultimo processamento"
NEXTMFN=$($LINDG4/mx $LILACS_OFI +control now | grep -Po "^\d+ +")
TO=$(expr $NEXTMFN - 1)
$LINDG4/mx LILACSe to=$TO create=LILACS -all now
rm LILACSe.{mst,xrf}

echo "Gera os links a serem verificados (LILACS_urls.txt)"
$ISIS/mx LILACS "pft=(if s(mpu,v8^*,mpl) = 'INTERNET' and p(v8^i) then  'LILACS|', v2[1],'|',v8^i/ else if p(v8^u) then 'LILACS|', v2[1],'|',v8^u/ fi fi)" lw=0 -all now > LILACS_urls.txt

cd - 

