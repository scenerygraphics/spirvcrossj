#!/bin/bash
shopt -s extglob

SPIRVCROSSJ_DIR=`pwd`
JAVA_DIR="$SPIRVCROSSJ_DIR/src/main/java/graphics/scenery/spirvcrossj/"

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     FIND_CMD=find;;
    Darwin*)    FIND_CMD=find;;
    CYGWIN*)    FIND_CMD=/bin/find;;
    MINGW*)     FIND_CMD=/bin/find;;
    MSYS*)      FIND_CMD=/bin/find;;
    *)          FIND_CMD=find
esac

echo "Using find ($FIND_CMD) on $unameOut."

echo "Patching wrapped classes..."
cd $JAVA_DIR
$FIND_CMD . -type f -name '*.java' -exec sed -i".bak" 's/spirv_cross::/Types./g' {} \;
rm $JAVA_DIR/*.bak
cd $SPIRVCROSSJ_DIR

echo "Continueing build..."
