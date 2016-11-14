#!/bin/sh
mkdir build
cd build
cmake ..
make
cd ../java-wrapper
mvn package
