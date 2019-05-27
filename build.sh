#!/bin/bash
shopt -s extglob

SPIRVCROSSJ_DIR=`pwd`
JAVA_DIR="$SPIRVCROSSJ_DIR/src/main/java/graphics/scenery/spirvcrossj/"

echo "Cleaning old wrapper files ..."
cd $JAVA_DIR
rm -- !(Loader.java)
cd $SPIRVCROSSJ_DIR

echo "Initialising glslang ..."
git submodule update
cd $SPIRVCROSSJ_DIR/glslang && git apply ../fix-tokenizer.patch
python ./update_glslang_sources.py

cd $SPIRVCROSSJ_DIR/SPIRV-cross && git apply ../fix-small-vector.patch

echo "Building now ..."

cd $SPIRVCROSSJ_DIR
mvn -B clean
mkdir -p target/build
rm -rf target/build/*
cd target/build
cmake -DCMAKE_BUILD_TYPE=Release ../..
cmake --build . -- -j4 

cd $SPIRVCROSSJ_DIR
./post-build.sh
mvn -B package
