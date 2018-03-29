#!/bin/bash
shopt -s extglob

SPIRVCROSSJ_DIR=`pwd`
JAVA_DIR="$SPIRVCROSSJ_DIR/src/main/java/graphics/scenery/spirvcrossj/"

cd $JAVA_DIR
rm -- !(Loader.java)
cd $SPIRVCROSSJ_DIR

git submodule update
cd $SPIRVCROSSJ_DIR/glslang && git apply ../fix-tokenizer.patch
cd $SPIRVCROSSJ_DIR/SPIRV-cross && git apply ../fix-bitset-constructor.patch

cd $SPIRVCROSSJ_DIR
mkdir -p build
rm -rf build/*
cd build
cmake ..
cmake --build .

cd $SPIRVCROSSJ_DIR
mvn clean package
