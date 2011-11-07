#!/bin/sh
OUR_DIR=`dirname $0`
rm -f $OUR_DIR/deploy.zip
cd $OUR_DIR/src
zip -r $OUR_DIR/deploy.zip MyBot.clj ants_ai

