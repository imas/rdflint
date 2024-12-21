#!/bin/bash

RDFLINT_LIB_DIR=$HOME/.local/share/rdflint
RDFLINT_BIN_DIR=$HOME/.local/bin

RDFLINT_VERSION=$(curl -s https://jitpack.io/api/builds/com.github.imas/rdflint/latestOk | jq -r .version)
DOWNLOAD_URL=https://jitpack.io/com/github/imas/rdflint/${RDFLINT_VERSION}/rdflint-${RDFLINT_VERSION}.jar

mkdir -p $RDFLINT_LIB_DIR
mkdir -p $RDFLINT_BIN_DIR
curl -s $DOWNLOAD_URL -o $RDFLINT_LIB_DIR/rdflint-${RDFLINT_VERSION}.jar

echo "#!/bin/sh
exec java -jar ${RDFLINT_LIB_DIR}/rdflint-${RDFLINT_VERSION}.jar \"\$@\"
" > ${RDFLINT_BIN_DIR}/rdflint
chmod +x ${RDFLINT_BIN_DIR}/rdflint
