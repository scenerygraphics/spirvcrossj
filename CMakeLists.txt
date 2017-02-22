cmake_minimum_required(VERSION 2.8)
project(SPIRVCrossj)
enable_testing()

FIND_PACKAGE(SWIG 3.0 REQUIRED)
INCLUDE(${SWIG_USE_FILE})

FIND_PACKAGE(JNI REQUIRED)
FIND_PACKAGE (Threads REQUIRED)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/src/natives")


if(${CMAKE_GENERATOR} MATCHES "Makefile")
  if(${CMAKE_CURRENT_SOURCE_DIR} STREQUAL ${CMAKE_CURRENT_BINARY_DIR})
    message(FATAL_ERROR "Build out of tree to avoid overwriting Makefile")
  endif()
endif()

set(SPIRVCROSS_DIR "${CMAKE_CURRENT_SOURCE_DIR}/SPIRV-cross")
set(GLSLANG_DIR "${CMAKE_CURRENT_SOURCE_DIR}/glslang")

add_subdirectory("${GLSLANG_DIR}")

set(CMAKE_SWIG_OUTDIR "${CMAKE_CURRENT_SOURCE_DIR}/src/main/java/graphics/scenery/spirvcrossj")
set(CMAKE_SWIG_FLAGS -package graphics.scenery.spirvcrossj)

SET_SOURCE_FILES_PROPERTIES(libspirvcrossj.i PROPERTIES CPLUSPLUS ON)
#SET_PROPERTY(SOURCE libspirvcrossj.i PROPERTY CPLUSPLUS ON)

#FILE(GLOB GLSLANG_CODEGEN_SOURCES ${GLSLANG_DIR}/glslang/GenericCodeGen/*.cpp)
#FILE(GLOB GLSLANG_MI_SOURCES ${GLSLANG_DIR}/glslang/MachineIndependent/*.cpp)
#FILE(GLOB GLSLANG_PREPROCESSOR_SOURCES ${GLSLANG_DIR}/glslang/MachineIndependent/preprocessor/*.cpp)
#FILE(GLOB GLSLANG_HLSL_SOURCES ${GLSLANG_DIR}/hlsl/*.cpp)
#FILE(GLOB GLSLANG_SPIRV_SOURCES ${GLSLANG_DIR}/SPIRV/*.cpp)
#FILE(GLOB GLSLANG_OGLDLL_SOURCES ${GLSLANG_DIR}/OGLCompilersDLL/*.cpp)
#
#if(WIN32)
#    FILE(GLOB GLSLANG_MD_SOURCES ${GLSLANG_DIR}/glslang/OSDependent/Windows/*.cpp)
#elseif(UNIX)
#    FILE(GLOB GLSLANG_MD_SOURCES ${GLSLANG_DIR}/glslang/OSDependent/Unix/*.cpp)
#endif(WIN32)

SWIG_ADD_MODULE(spirvcrossj java libspirvcrossj.i 
    ${SPIRVCROSS_DIR}/GLSL.std.450.h
	${SPIRVCROSS_DIR}/spirv_cross.cpp
	${SPIRVCROSS_DIR}/spirv_glsl.cpp
	${SPIRVCROSS_DIR}/spirv_cpp.cpp
	${SPIRVCROSS_DIR}/spirv_msl.cpp
	${SPIRVCROSS_DIR}/spirv_cfg.cpp
    ${GLSLANG_DIR}/glslang/Public/ShaderLang.h
    ${GLSLANG_DIR}/StandAlone/ResourceLimits.h)
#	${GLSLANG_MI_SOURCES}
#	${GLSLANG_PREPROCESSOR_SOURCES}
#	${GLSLANG_CODEGEN_SOURCES}
#	${GLSLANG_MD_SOURCES}
#	${GLSLANG_SPIRV_SOURCES}
#	${GLSLANG_HLSL_SOURCES}
#	${GLSLANG_OGLDLL_SOURCES})

set(GLSLANG_LIBRARIES
    glslang
    SPIRV
    SPVRemapper
    glslang-default-resource-limits)

SWIG_LINK_LIBRARIES(spirvcrossj ${GLSLANG_LIBRARIES})
SWIG_LINK_LIBRARIES(spirvcrossj ${CMAKE_THREAD_LIBS_INIT})

target_include_directories(spirvcrossj PUBLIC "${GLSLANG_DIR}/glslang/Include")
target_include_directories(spirvcrossj PUBLIC "${GLSLANG_DIR}/glslang/Public")
target_include_directories(spirvcrossj PUBLIC "${GLSLANG_DIR}/hlsl")
target_include_directories(spirvcrossj PUBLIC "${GLSLANG_DIR}/SPIRV")
target_include_directories(spirvcrossj PUBLIC "${GLSLANG_DIR}/OGLCompilersDLL")
target_include_directories(spirvcrossj PUBLIC "${GLSLANG_DIR}/glslang/OSDependent")

target_include_directories(spirvcrossj PUBLIC ${JNI_INCLUDE_DIRS})
target_include_directories(spirvcrossj PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/SPIRV-cross")

# To specify special debug or optimization options, use
# -DCMAKE_CXX_COMPILE_FLAGS
# However, we require the C++11 dialect.
if (NOT "${MSVC}")
  set(spirv-compiler-options -std=c++11 -Wall -Wextra -pthread -Wshadow)
  target_compile_options(spirvcrossj PRIVATE ${spirv-compiler-options})
  set_source_files_properties( ${swig_generated_file_fullname}
                             PROPERTIES COMPILE_FLAGS ${spirv-compiler-options})
endif(NOT "${MSVC}")

if ("${MSVC}")
	set_target_properties( spirvcrossj PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY} )
	set_target_properties( spirvcrossj PROPERTIES LIBRARY_OUTPUT_DIRECTORY_DEBUG ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
	set_target_properties( spirvcrossj PROPERTIES LIBRARY_OUTPUT_DIRECTORY_RELEASE ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
endif ( "${MSVC}" )
