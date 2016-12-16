%module libspirvcrossj

%include "typemaps.i"
%include "std_vector.i"
%include "stdint.i"
%include "std_string.i"
%include "enums.swg"

%rename(equals) operator==;
%rename(set) operator=;
%rename(invoke) operator();
%rename(less_than) operator<;
%rename(op_or) operator|;
%rename("%(lowercamelcase)s", %$isfunction, %$not %$ismemberget, %$not %$ismemberset) "";
%rename("%(lowercamelcase)s", %$isvariable) "";

%naturalvar SPIRConstant;
%naturalvar SPIRType;

%{
    #include "spirv.hpp"
    #include "spirv_cfg.hpp"
    #include "spirv_cross.hpp"
    #include "spirv_common.hpp"
    #include "spirv_glsl.hpp"
    #include "spirv_cpp.hpp"
    #include "spirv_msl.hpp"

    using namespace spirv_cross;
%}

%include "SPIRV-cross/spirv.hpp"
%include "SPIRV-cross/spirv_cfg.hpp"
%include "SPIRV-cross/spirv_cross.hpp"
%include "SPIRV-cross/spirv_glsl.hpp"
%include "SPIRV-cross/spirv_cpp.hpp"
%include "SPIRV-cross/spirv_msl.hpp"

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
