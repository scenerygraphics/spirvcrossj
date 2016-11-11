%module libspirvcrossj

%include "typemaps.i"
%include "std_vector.i"
%include "stdint.i"
%include "std_string.i"

%rename(equals) operator==;
%rename(set) operator=;
%rename(invoke) operator();
%rename(less_than) operator<;
%rename("%(undercase)s", %$isfunction, %$not %$ismemberget, %$not %$ismemberset) "";

%naturalvar ShaderResources::uniform_buffers;

%{
    #include "spirv.hpp"
    #include "spirv_cross.hpp"
    #include "spirv_common.hpp"
    #include "spirv_glsl.hpp"
    #include "spirv_cpp.hpp"
    #include "spirv_msl.hpp"

    #include "include/spirv_cross/image.hpp"

    using namespace spirv_cross;
%}

%include "SPIRV-cross/spirv_cross.hpp"
%include "SPIRV-cross/spirv_glsl.hpp"
%include "SPIRV-cross/spirv_cpp.hpp"
%include "SPIRV-cross/spirv_msl.hpp"

using namespace std;
using namespace spirv_cross;

namespace std {
    %template(IntVec) std::vector<uint32_t>;
    %template(ResourceVec) std::vector<spirv_cross::Resource>;
}


