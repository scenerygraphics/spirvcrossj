# Java bindings for SPIRV-cross and glslang

[![Linux/macOS Build Status](https://travis-ci.org/scenerygraphics/spirvcrossj.svg?branch=master)](https://travis-ci.org/scenerygraphics/spirvcrossj)

[![Windows Build status](https://ci.appveyor.com/api/projects/status/6m5efeddoaqvc9b3/branch/master?svg=true)](https://ci.appveyor.com/project/skalarproduktraum/spirvcrossj/branch/master)

[SPIRV-cross](https://github.com/KhronosGroup/SPIRV-cross) is a nifty library from the Khronos Group to enable GLSL shader reflection and conversion of SPIR-V binaries to different GLSL version. 

[glslang](https://github.com/KhronosGroup/glslang) is the Khronos Group GLSL reference compiler and validator, providing the other direction.

This repo provides a Java wrapper for both of them.

## Gradle:

First of all, you need to have `mavenCentral`:

    repositories {
        ..
        mavenCentral()
        ..
    }
    
Then, under `dependencies` section, exploiting the lwjgl native `switch`:

    dependencies {
        ..
        ext.spirvCrossVersion = "0.4.0"
        compile "graphics.scenery:spirvcrossj:$spirvCrossVersion"

        switch (OperatingSystem.current()) {
            case OperatingSystem.WINDOWS:
                ext.lwjglNatives = "natives-windows"
                break
            case OperatingSystem.LINUX:
                ext.lwjglNatives = "natives-linux"
                break
            case OperatingSystem.MAC_OS:
                ext.lwjglNatives = "natives-macos"
                break
        }
        runtime "graphics.scenery:spirvcrossj:$spirvCrossVersion:$lwjglNatives"
        ..
    }
    
Don't forget to add at the top of your `build.gradle`:

`import org.gradle.internal.os.OperatingSystem`

You can take a look to a `build.gradle` [here](https://github.com/java-opengl-labs/Vulkan/blob/master/build.gradle)

## Building

To build the wrapper, you need the following prerequisites:
* CMake, version 2.8 or higher.
* SWIG, version 3.0 or higher.
* a working build environment, meaning Visual Studio on Windows, autotools and gcc/clang on OSX/Linux.
* Maven for building the JAR

By default, a debug build is done. To do a release build, add `--config Release` in the `cmake --build` step.

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
cmake --build .

# build Maven artifact
cd ../java-wrapper
mvn install
```

### Windows

Please note that 32bit builds are not supported.

```
# get SPIRV-cross source code, only needed after initial clone
git submodule init
git submodule update

# do CMAKE build
mkdir build
cd build
cmake -G "Visual Studio 14 2015 Win64" ..
cmake --build .

# build Maven artifact
cd ../java-wrapper
mvn install
```

## Running the tests

You can run the JUnit tests e.g. in Eclipse or IntelliJ after executing the steps above. The loader will pick up either the newly generated JNI libraries, or load the ones from the JAR. Alternatively, the tests can be executed via Maven, as `mvn test`.

In all cases, two tests are performed:

* for SPIRV-cross: converting from SPIRV to GLSL 3.10, taking a provided SPIRV binary, removing decorations and converting it back to GLSL 3.10 (`TestVulkanToGLSL.java`).
* for glslang: taking in GLSL text files, and compiling them to SPIRV with Vulkan semantics (`TestGLSLToVulkan.java`).

The input files for these tests are taken from the tests in the repositories of glslang and SPIRV-cross and are expected to compile.

## Usage

See the tests in `java-wrapper/src/main/java/graphics/scenery/spirvcrossj/` for how to use the library. For both SPIRV-cross and glslang, the syntax is very similar to the original C++ version.
