%module libspirvcrossj

%include "typemaps.i"
%include "std_vector.i"
%include "stdint.i"
%include "std_string.i"
%include "enums.swg"
%include "cpointer.i"
%include "arrays_java.i"
%include "various.i"

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

%typemap(jstype) std::string* OUTPUT "String[]"
%typemap(jtype) std::string* OUTPUT "String[]"
%typemap(jni) std::string* OUTPUT "jobjectArray"
%typemap(javain)  std::string* OUTPUT "$javainput"
%typemap(in) std::string* OUTPUT (std::string *temp) {
  if (!$input) {
    SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "array null");
    return $null;
  }
  if (JCALL1(GetArrayLength, jenv, $input) == 0) {
    SWIG_JavaThrowException(jenv, SWIG_JavaIndexOutOfBoundsException, "Array must contain at least 1 element");
  }
  $1 = &temp;
}
%typemap(argout) std::string* OUTPUT {
  jstring jvalue = JCALL1(NewStringUTF, jenv, temp$argnum.c_str()); 
  JCALL3(SetObjectArrayElement, jenv, $input, 0, jvalue);
}

%apply char **STRING_ARRAY { char **s }
%apply char **STRING_ARRAY { const char* const* s }
%apply char **STRING_ARRAY { const char* const* names }
%apply std::string *OUTPUT { std::string* output_string }

%apply int *IN { const int* l }

%pointer_functions(int, IntPointer);
%pointer_functions(std::string, StringPointer);

// for consistency due to incompatible change in SWIG 3.0.11
// see https://github.com/swig/swig/issues/856
%rename("add") std::vector::push_back;
%rename("empty") std::vector::empty;

%naturalvar SPIRConstant;
%naturalvar SPIRType;
%naturalvar EProfile;
%naturalvar TBuiltInResource;

%{
    #include "spirv.hpp"
    #include "spirv_cfg.hpp"
    #include "spirv_cross.hpp"
    #include "spirv_common.hpp"
    #include "spirv_glsl.hpp"
    #include "spirv_cpp.hpp"
    #include "spirv_msl.hpp"

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
%}

%include "SPIRV-cross/spirv.hpp"
%include "SPIRV-cross/spirv_cfg.hpp"
%include "SPIRV-cross/spirv_cross.hpp"
%include "SPIRV-cross/spirv_glsl.hpp"
%include "SPIRV-cross/spirv_cpp.hpp"
%include "SPIRV-cross/spirv_msl.hpp"

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
