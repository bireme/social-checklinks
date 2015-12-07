#!/bin/bash

HOME=/home/javaapps/SocialCheckLinks/social-checklinks

if [ "$1" == "LILACS" ]; then
	$HOME/sh/genBrokenUrlLinks_LILACS.sh
fi
