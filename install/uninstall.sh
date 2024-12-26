#!/bin/bash

RDFLINT_LIB_DIR=$HOME/.local/share/rdflint
RDFLINT_BIN_DIR=$HOME/.local/bin

rm -f $RDFLINT_BIN_DIR/rdflint
if [ -d $RDFLINT_LIB_DIR ]; then
    for f in $(find ${RDFLINT_LIB_DIR} -name rdflint-*.jar); do
        rm $f
    done
    rmdir $RDFLINT_LIB_DIR
fi
