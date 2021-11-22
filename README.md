
# spirvcrossj -- JVM bindings for SPIRV-cross and glslang

## 🚧🚧 spirvcrossj is retired now, please use
[lwjgl's](https://www.lwjgl.org) shaderc and
SPIRVcross bindings 🚧🚧

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/graphics.scenery/spirvcrossj/badge.svg)](https://maven-badges.herokuapp.com/maven-central/graphics.scenery/spirvcrossj) Â· [![Join the chat at https://gitter.im/scenerygraphics/SciView](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scenerygraphics/SciView?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) Â· [![Linux/macOS Build Status](https://travis-ci.org/scenerygraphics/spirvcrossj.svg?branch=master)](https://travis-ci.org/scenerygraphics/spirvcrossj) Â· [![Windows Build status](https://ci.appveyor.com/api/projects/status/6m5efeddoaqvc9b3/branch/master?svg=true)](https://ci.appveyor.com/project/skalarproduktraum/spirvcrossj/branch/master)

[SPIRV-cross](https://github.com/KhronosGroup/SPIRV-cross) is a nifty library from the Khronos Group to enable GLSL shader reflection and conversion of SPIR-V binaries to different GLSL version. 

[glslang](https://github.com/KhronosGroup/glslang) is the Khronos Group GLSL reference compiler and validator, providing the other direction.

spirvcrossj provides a wrapper for Java and other JVM languages for both of SPIRV-cross and glslang.

## Usage

### Maven

In the `profiles` section of your `pom.xml`, add the following profiles:

```xml
<profile>
    <id>spirvcrossj-natives-linux</id>
    <activation>
        <os>
            <family>unix</family>
        </os>
    </activation>
    <properties>
        <spirvcrossj.natives>natives-linux</spirvcrossj.natives>
    </properties>
</profile>
<profile>
    <id>spirvcrossj-natives-macos</id>
    <activation>
        <os>
            <family>mac</family>
        </os>
    </activation>
    <properties>
        <spirvcrossj.natives>natives-macos</spirvcrossj.natives>
    </properties>
</profile>
<profile>
    <id>spirvcrossj-natives-windows</id>
    <activation>
        <os>
            <family>windows</family>
        </os>
    </activation>
    <properties>
        <spirvcrossj.natives>natives-windows</spirvcrossj.natives>
    </properties>
</profile>
```
In case you are using lwjgl3, you can also use the `lwjgl-natives-linux`, etc. profiles that are already defined. Replace any usages of `spirvcrossj.natives` with `lwjgl.natives`, then.

Finally, add the following dependencies:

```xml
<dependency>
    <groupId>graphics.scenery</groupId>
    <artifactId>spirvcrossj</artifactId>
    <version>0.8.0-1.1.106.0</version>
</dependency>

<dependency>
    <groupId>graphics.scenery</groupId>
    <artifactId>spirvcrossj</artifactId>
    <version>${spirvcrossj.version}</version>
    <classifier>${spirvcrossj.natives}</classifier>
    <scope>runtime</scope>
</dependency>
```

### Gradle

First of all, you need to have `mavenCentral`:

```groovy
repositories {
    ..
    mavenCentral()
    ..
}
```

Then, under `dependencies` section, exploiting the lwjgl native `switch`:
```groovy
    dependencies {
        ..
        ext.spirvCrossVersion = "0.8.0-1.1.106"
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
```

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
```bash
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

__Please note that 32bit builds are not supported.__

```bash
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

* for SPIRV-cross: converting from SPIRV to GLSL 3.10, taking a provided SPIRV binary, removing decorations and converting it back to GLSL 3.10 ([`TestVulkanToGLSL.java`](src/test/java/graphics/scenery/spirvcrossj/TestVulkanToGLSL.java)).
* for glslang: taking in GLSL text files, and compiling them to SPIRV with Vulkan semantics ([`TestGLSLToVulkan.java`](src/test/java/graphics/scenery/spirvcrossj/TestGLSLToVulkan.java)).

The input files for these tests are taken from the tests in the repositories of glslang and SPIRV-cross and are expected to compile.

## Usage

See the [tests](src/test/java/graphics/scenery/spirvcrossj) for how to use the library. For both SPIRV-cross and glslang, the syntax is very similar to the original C++ version.
