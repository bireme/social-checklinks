#!/bin/bash

################################################################################
# Data: 01/06/2014
# Pre requisitos:
# - ter a base de dados MongoDb - 'SocialCheckLinks' com a coleção 'HistoryBrokenLinks'
################################################################################


SERVER=production    # test homolog production
HOME=/home/javaapps/SocialCheckLinks/social-checklinks
ISIS=/usr/local/bireme/cisis/5.7e/linux64/isis1660
NOW=$(date +"%Y%m%d")
NOW2=$(date +"%Y%m%d-%T")

echo "Inicio da execucao: ${NOW2}"
echo
echo "Inicio da execucao: ${NOW2}" >> execution.log

echo "Muda para diretorio de trabalho: $HOME"
cd $HOME

echo "Bloqueia escrita na interface web do Social Check Links"
sh/$SERVER/setReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = true</h1>" out > gout
if [ ! -s gout ]
then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Bloqueia escrita na interface web do Social Check Links." -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
    rm out gout
    exit 1
fi
rm out gout

echo "Espera 5 minutos para garantir o termino de todas as correcoes iniciadas"
sleep 5m

echo "Apaga gizmo anterior"
if [ -e Gv8broken.giz ]; then
    rm Gv8broken.giz
fi

echo "Gera gizmo a partir do Social Check Links"
sh/$SERVER/gizmo.sh
if [ $? -ne 0 ]; then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Gera gizmo a partir do Social Check Links." -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
    exit 1
fi

if [ -s Gv8broken.giz ]; then
    echo "Armazena em backup o arquivo gizmo se nao vazio"
    tar -czpvf history/Gv8broken_${NOW}.tgz Gv8broken.giz
fi

while IFS="|" read db server user path proc lilG4
do
    echo "Transfere a base $db de $server:$path/$db.{mst,xrf} para o diretorio corrente"
    scp -p $user@$server:$path/$db.{mst,xrf} .
    if [ $? -ne 0 ]; then
        sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Transfere a base $db de $server:$path para o diretorio corrente." -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
        exit 1
    fi

    echo "Armazena em backup a base antes de aplicar o gizmo"
    $ISIS/mx $db create=${db}_url count=0 -all now
    $ISIS/mx $db "proc=$proc" -all tell=10000 now append=${db}_url

    tar -czpvf history/${db}_${NOW}.tgz ${db}_url.mst ${db}_url.xrf
    rm ${db}_url.{mst,xrf}
done < config.txt

if [ -s Gv8broken.giz ]; then
    echo "Executa gizmo nas bases de dados se gizmo nao vazio"
    sh/gizmo2Isis.sh Gv8broken.giz other
    if [ $? -ne 0 ]; then
        sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Executa gizmo nas bases de dados." -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
        exit 1
    fi

    echo "Move bases de dados"
    rm *.{mst,xrf}
    mv other/*.{mst,xrf} .
fi

while IFS="|" read db server user path proc lilG4
do
    echo "Inicia processo de checagem de links da base $db"
    /usr/local/bireme/java/FisChecker/genv8notfound.sh $db $lilG4

    echo "Testa se o numero de links quebrados [$db] esta na margem de 10% comparado com a verificacao anterior"
    val2=$(wc -l /usr/local/bireme/java/FisChecker/output/${db}_out/bases/${db}_v8broken.txt | grep -Po "^\d+")
    if [ -f ${db}_broken.txt ]; then
        val1=$(cat ${db}_broken.txt)
    else 
        val1=$val2
    fi

    res=$(echo $val1 $val2 | bc sh/insideLimits.bc)

    if [ $res -eq 0 ]; then
        sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Numero de links quebrados [$val2] verificados para a base de dados [$db] variou em mais de 10% desde a ultima checagem." -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
        exit 1
    fi
    echo $val2 > ${db}_broken.txt

    echo "Alimenta os links quebrados no Social Check Links"
    sh/$SERVER/loadBrokenLinks.sh ${db}
    if [ $? -ne 0 ]; then
       sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Alimenta os links quebrados no Social Check Links." -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
       exit 1
   fi
done < config.txt

echo "Desbloqueia escrita na interface web do Social Check Links"
sh/$SERVER/resetReadOnlyMode.sh > out
grep -o "<h1>Read Only Mode = false</h1>" out > gout
if [ ! -s gout ]
then
    sendemail -f appofi@bireme.org -u "Social Check Links Error - `date '+%Y%m%d'`" -m "Desbloqueia escrita na interface web do Social Check Links." -t lilacsdb@bireme.org -cc botturam@paho.org britofa@paho.org -s esmeralda.bireme.br -xu appupdate -xp bir@2012#
    rm out gout
    exit 1
fi
rm out gout

echo "Retorna para diretorio original"
cd -

END=$(date +"%Y%m%d-%T")
echo
echo "Fim da execuçao: $END"
echo "Fim da execuçao: $END" >> execution.log
