#!/bin/sh
OUR_DIR=`dirname $0`
rm -f deploy.sh
cd $OUR_DIR/src
zip -r ../deploy.zip MyBot.clj ants_ai

