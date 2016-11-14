cmake_minimum_required(VERSION 2.8)
project(SPIRVCrossj)
enable_testing()

FIND_PACKAGE(SWIG 3.0 REQUIRED)
INCLUDE(${SWIG_USE_FILE})

FIND_PACKAGE(JNI)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/java-wrapper/src/natives")


if(${CMAKE_GENERATOR} MATCHES "Makefile")
  if(${CMAKE_CURRENT_SOURCE_DIR} STREQUAL ${CMAKE_CURRENT_BINARY_DIR})
    message(FATAL_ERROR "Build out of tree to avoid overwriting Makefile")
  endif()
endif()

set(SPIRVCROSS_DIR "${CMAKE_CURRENT_SOURCE_DIR}/SPIRV-cross")
set(CMAKE_SWIG_OUTDIR "${CMAKE_CURRENT_SOURCE_DIR}/java-wrapper/src/main/java/is/ulrik/spirvcrossj")
set(CMAKE_SWIG_FLAGS -package is.ulrik.spirvcrossj)

SET_SOURCE_FILES_PROPERTIES(libspirvcrossj.i PROPERTIES CPLUSPLUS ON)

SWIG_ADD_MODULE(spirvcrossj java libspirvcrossj.i 
    ${SPIRVCROSS_DIR}/GLSL.std.450.h
	${SPIRVCROSS_DIR}/spirv_common.hpp
	${SPIRVCROSS_DIR}/spirv.hpp
	${SPIRVCROSS_DIR}/spirv_cross.hpp
	${SPIRVCROSS_DIR}/spirv_cross.cpp
	${SPIRVCROSS_DIR}/spirv_glsl.cpp
	${SPIRVCROSS_DIR}/spirv_glsl.hpp
	${SPIRVCROSS_DIR}/spirv_cpp.hpp
	${SPIRVCROSS_DIR}/spirv_cpp.cpp
	${SPIRVCROSS_DIR}/spirv_msl.hpp
	${SPIRVCROSS_DIR}/spirv_msl.cpp)

target_include_directories(spirvcrossj PUBLIC ${JNI_INCLUDE_DIRS})
target_include_directories(spirvcrossj PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/SPIRV-cross")

# To specify special debug or optimization options, use
# -DCMAKE_CXX_COMPILE_FLAGS
# However, we require the C++11 dialect.
if (NOT "${MSVC}")
  set(spirv-compiler-options -std=c++11 -Wall -Wextra -Wnoerror -Wshadow)
  target_compile_options(spirvcrossj PRIVATE ${spirv-compiler-options})
  set_source_files_properties( ${swig_generated_file_fullname}
                             PROPERTIES COMPILE_FLAGS ${spirv-compiler-options})
endif(NOT "${MSVC}")

if ("${MSVC}")
	set_target_properties( spirvcrossj PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY} )
	set_target_properties( spirvcrossj PROPERTIES LIBRARY_OUTPUT_DIRECTORY_DEBUG ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
	set_target_properties( spirvcrossj PROPERTIES LIBRARY_OUTPUT_DIRECTORY_RELEASE ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
endif ( "${MSVC}" )
