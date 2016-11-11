# Java bindings for SPIRV-cross

[SPIRV-cross](https://github.com/KhronosGroup/SPIRV-cross) is a nifty library from the Khronos Group to enable GLSL shader reflection and conversion of SPIR-V binaries to different GLSL version. This repo provides a Java wrapper for it.

## Building

To build the wrapper, you need the following prerequisites:
* CMake, version 2.8 or higher.
* SWIG, version 3.0 or higher.
* a working build environment, meaning Visual Studio on Windows, autotools and gcc/clang on OSX/Linux.
* Maven for building the JAR

### OSX/Linux
On OSX and Linux, you can simply run `build.sh` or follow these steps:
```
# get SPIRV-cross source code, only needed after initial clone
git submodule init
git submodule update

# do CMAKE build
mkdir build
cd build
cmake ..
make

# build Maven artifact
cd ../java-wrapper
mvn install
```

### Windows

```
# get SPIRV-cross source code, only needed after initial clone
git submodule init
git submodule update

# do CMAKE build
mkdir build
cd build
cmake -G "Visual Studio 14 2015 Win64" ..
devenv /build

# build Maven artifact
cd ../java-wrapper
mvn install
```


## Running the tests

You can run the (at the moment) single JUnit test e.g. in Eclipse or IntelliJ after executing the steps above. Please be aware that the working directory has to be set to the `build/` directory for JNI to pick up the native library. I'll add a loader lateron.

## Usage

See the tests in `java-wrapper/src/test/java/is/ulrik/spirvcrossj/` for how to use the library. The syntax is very similar to the original C++ version.