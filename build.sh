#!/bin/sh
SPIRVCROSSJ_DIR=`pwd`
git submodule update
cd $SPIRVCROSSJ_DIR/glslang && git apply ../fix_tokenizer.patch

cd $SPIRVCROSSJ_DIR
mkdir build
rm -rf build/*
cd build
cmake ..
cmake --build .

cd $SPIRVCROSSJ_DIR
mvn clean package
