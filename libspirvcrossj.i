%module libspirvcrossj

%include "typemaps.i"
%include "std_except.i"
%include "std_vector.i"
%include "stdint.i"
%include "std_string.i"
%include "enumtypeunsafe.swg"
%include "cpointer.i"
%include "arrays_java.i"
%include "exception.i"

%javaconst(1);

%typemap(javaimports) EShLanguageMask %{
import static graphics.scenery.spirvcrossj.EShLanguage.*;
%}

// for consistency due to incompatible change in SWIG 3.0.11
// see https://github.com/swig/swig/issues/856
%rename("pushBack") std::vector::push_back;
%rename("empty") std::vector::empty;

%typemap(jni) char **STRING_ARRAY "jobjectArray"
%typemap(jtype) char **STRING_ARRAY "String[]"
%typemap(jstype) char **STRING_ARRAY "String[]"

%typemap(in) char **STRING_ARRAY (jint size) {
  int i = 0;
  size = (*jenv).GetArrayLength($input);
  $1 = (char**)calloc(size, sizeof(char*));
  //std::cout << "Array length: " << size << std::endl;
  /* make a copy of each string */
  for (i = 0; i < size; ++i) {
    jstring j_string = (jstring)(*jenv).GetObjectArrayElement($input, i);
    const char* c_string = (*jenv).GetStringUTFChars(j_string, NULL);
    //std::cout << "in (" << strlen(c_string) << "): >>>" << c_string  << "<<<" << std::endl;
    $1[i] = (char*)calloc(strlen(c_string)+1, sizeof(char));
    strcpy($1[i], c_string);
    //std::cout << "putting null at " << strlen(c_string) << std::endl;
    //std::cout << "out: >>>" << $1[i] << "<<<" << std::endl;
    (*jenv).ReleaseStringUTFChars(j_string, c_string);
    (*jenv).DeleteLocalRef(j_string);
  }
//  std::cout << i << std::endl;
//  $1[i] = 0;
}

// TODO: Fix memleak that results from not directly deallocating this.
// Direct dealloc however leads to premature freeing of the memory, which
// is assumed to be externally managed by glslang.
%typemap(freearg) const char **STRING_ARRAY {
/*    std::cout << "Freeing array" << std:: endl;
  int i;
  for (i=0; i<size$argnum; i++)
#ifdef __cplusplus
    delete[] $1[i];
  delete[] $1;
#else
  free($1[i]);
  free($1);
#endif
  */
}

%typemap(out) char **STRING_ARRAY {
  if ($1) {
    int i;
    jsize len=0;
    jstring temp_string;
    const jclass clazz = JCALL1(FindClass, jenv, "java/lang/String");

    while ($1[len]) len++;
    $result = JCALL3(NewObjectArray, jenv, len, clazz, NULL);
    /* exception checking omitted */

    for (i=0; i<len; i++) {
      temp_string = JCALL1(NewStringUTF, jenv, *$1++);
      JCALL3(SetObjectArrayElement, jenv, $result, i, temp_string);
      JCALL1(DeleteLocalRef, jenv, temp_string);
    }
  }
}

%typemap(javain) char **STRING_ARRAY "$javainput"
%typemap(javaout) char **STRING_ARRAY {
    return $jnicall;
  }

//////
%typemap(jni) std::string *INOUT, std::string *INOUT %{jobjectArray%}
%typemap(jtype) std::string *INOUT, std::string *INOUT "java.lang.String[]"
%typemap(jstype) std::string *INOUT, std::string *INOUT "java.lang.String[]"
%typemap(javain) std::string *INOUT, std::string *INOUT "$javainput"

%typemap(in) std::string *INOUT (std::string strTemp ), std::string *INOUT (std::string strTemp ) {
  if (!$input) {
    SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "array null");
    return $null;
  }
  if (JCALL1(GetArrayLength, jenv, $input) == 0) {
    SWIG_JavaThrowException(jenv, SWIG_JavaIndexOutOfBoundsException, "Array must contain at least 1 element");
    return $null;
  }

  jobject oInput = JCALL2(GetObjectArrayElement, jenv, $input, 0); 
  if ( NULL != oInput ) {
    jstring sInput = static_cast<jstring>( oInput );

    const char * $1_pstr = (const char *)jenv->GetStringUTFChars(sInput, 0); 
    if (!$1_pstr) return $null;
    strTemp.assign( $1_pstr );
    jenv->ReleaseStringUTFChars( sInput, $1_pstr);  
  }

  $1 = &strTemp;
}

%typemap(freearg) std::string *INOUT, std::string *INOUT ""

%typemap(argout) std::string *INOUT, std::string *INOUT
{ 
  jstring jStrTemp = jenv->NewStringUTF( strTemp$argnum.c_str() );
  JCALL3(SetObjectArrayElement, jenv, $input, 0, jStrTemp ); 
}
/////

// ignore these, otherwise there will be duplicates from the parent class
%ignore TLinker::infoSink;
%ignore TCompiler::infoSink;
%ignore TUniformMap::infoSink;

%apply std::string *INOUT { std::string* output_string };
%apply std::string *INOUT { std::string* outputString };

%rename(equals) operator==;
%rename(set) operator=;
%rename(invoke) operator();
%rename(less_than) operator<;
%rename(op_or) operator|;
%rename("%(lowercamelcase)s", %$isfunction, %$not %$ismemberget, %$not %$ismemberset) "";
%rename("%(lowercamelcase)s", %$isvariable) "";

%ignore _ShLink;
%ignore ShLink;
%ignore _ShLinkExt;
%ignore ShLinkExt;

//%apply char **STRING_ARRAY { char **s }
%apply char **STRING_ARRAY { const char* const* s }
%apply char **STRING_ARRAY { const char* const* names }

%typemap(freearg) char **STRING_ARRAY {
    cout << "Freeing the string array" << endl;
}

%apply int *INOUT { const int* l }

%pointer_functions(int, IntPointer);
%pointer_functions(std::string, StringPointer);


%naturalvar spirv_cross::SPIRConstant;
%naturalvar spirv_cross::EProfile;
%naturalvar spirv_cross::TBuiltInResource;

#ifndef SPIRV_CROSS_DEPRECATED(reason)
#define SPIRV_CROSS_DEPRECATED(reason)
#endif


// exception handling for SPIRV-cross

namespace std 
{
  %ignore runtime_error;
  struct runtime_error {};
}

// Allow C++ exceptions to be handled in Java
%typemap(throws, throws="java.lang.Exception") spirv_cross::CompilerError {
  jclass excep = jenv->FindClass("java/lang/Exception");
  if (excep)
    jenv->ThrowNew(excep, $1.what());
  return $null;
}

// Force the CompilerError Java class to extend java.lang.Exception
%typemap(javabase) spirv_cross::CompilerError "java.lang.Exception";

%exception {
  try {
      $action
  } catch(spirv_cross::CompilerError) {
      SWIG_exception(SWIG_RuntimeError, "SPIRV-cross compiler error in $decl");
  }
}

%{
    #include <stdexcept>
    #include "spirv.hpp"
    #include "spirv_cfg.hpp"
    #include "spirv_cross.hpp"
    #include "spirv_cross_util.hpp"
    #include "spirv_parser.hpp"
    #include "spirv_cross_parsed_ir.hpp"
    #include "spirv_common.hpp"
    #include "spirv_glsl.hpp"
    #include "spirv_cpp.hpp"
    #include "spirv_msl.hpp"
    #include "spirv_reflect.hpp"

    #include "ShHandle.h" 
    #include "revision.h" 
    #include "ShaderLang.h" 
    #include "../../StandAlone/ResourceLimits.h"
    #include "../MachineIndependent/Versions.h"
    #include "GlslangToSpv.h" 
    #include "GLSL.std.450.h" 
    #include "disassemble.h"
    #include "SPVRemapper.h"

    using namespace spirv_cross;
    using namespace spirv_cross::ParserIR;
    using namespace spirv_cross::ParsedIR;
    using namespace glslang;
%}

%ignore spirv_cross::CompilerMSL::Options::set_msl_version;
%ignore spirv_cross::CompilerMSL::get_options;
%ignore spirv_cross::Compiler::get_member_decoration_string;
%ignore spirv_cross::SPIRAccessChain;
%ignore spirv_cross::SPIRBlock;
%ignore spirv_cross::IVariant;
%ignore spirv_cross::Variant;
%ignore spirv_cross::Meta;

%ignore spirv_cross::SPIRConstantOp::clone;
%ignore spirv_cross::SPIRUndef::clone;
%ignore spirv_cross::SPIRExtension::clone;
%ignore spirv_cross::SPIRExpression::clone;
%ignore spirv_cross::SPIRFunction::clone;
%ignore spirv_cross::SPIRFunctionPrototype::clone;
%ignore spirv_cross::SPIRConstant::clone;
%ignore spirv_cross::SPIRVariable::clone;
%ignore spirv_cross::SPIRCombinedImageSampler::clone;
%ignore spirv_cross::SPIRType::clone;

%include "SPIRV-cross/spirv.hpp"
%include "SPIRV-cross/spirv_common.hpp"
%include "SPIRV-cross/spirv_cfg.hpp"
%include "SPIRV-cross/spirv_cross.hpp"
%include "SPIRV-cross/spirv_cross_util.hpp"
%include "SPIRV-cross/spirv_glsl.hpp"
%include "SPIRV-cross/spirv_cpp.hpp"
%include "SPIRV-cross/spirv_parser.hpp"
%include "SPIRV-cross/spirv_cross_parsed_ir.hpp"
%include "SPIRV-cross/spirv_msl.hpp"
%include "SPIRV-cross/spirv_reflect.hpp"

// glslang
%include "glslang/glslang/Include/ShHandle.h" 
%include "glslang/glslang/Include/revision.h" 
%include "glslang/glslang/Public/ShaderLang.h" 
%include "glslang/glslang/MachineIndependent/Versions.h"
%include "glslang/StandAlone/ResourceLimits.h"
%include "glslang/SPIRV/GlslangToSpv.h" 
%include "glslang/SPIRV/GLSL.std.450.h" 
%include "glslang/SPIRV/disassemble.h"
%include "glslang/SPIRV/SPVRemapper.h"

using namespace std;
using namespace spv;
using namespace spirv_cross;

namespace std {
    %template(StringVec) std::vector<std::string>;
    %template(IntVec) std::vector<uint32_t>;
    %template(ResourceVec) std::vector<spirv_cross::Resource>;
    %template(BufferRangeVec) std::vector<spirv_cross::BufferRange>;
    %template(CombinedImageSamplerVec) std::vector<spirv_cross::CombinedImageSampler>;
    %template(MSLResourceBindingVec) std::vector<spirv_cross::MSLResourceBinding>;
    %template(MSLVertexAttrVec) std::vector<spirv_cross::MSLVertexAttr>;
    %template(SpecializationConstantVec) std::vector<spirv_cross::SpecializationConstant>;
    %template(PlsRemapVec) std::vector<spirv_cross::PlsRemap>;
}
