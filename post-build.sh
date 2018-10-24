#!/bin/bash
shopt -s extglob

SPIRVCROSSJ_DIR=`pwd`
JAVA_DIR="$SPIRVCROSSJ_DIR/src/main/java/graphics/scenery/spirvcrossj/"

cd $JAVA_DIR
find . -type f -name '*.java' -exec sed -i"" 's/spirv_cross::/Types./g' {} \;
cd $SPIRVCROSSJ_DIR
