package graphics.scenery.spirvcrossj;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * <Description>
 *
 * @author Ulrik G?nther <hello@ulrik.is>
 */
public class TestGLSLToVulkan {

  @Test
  public void convertGLSLToVulkan() throws IOException, URISyntaxException, InterruptedException {
    Loader.loadNatives();

    libspirvcrossj.initializeProcess();

    Boolean compileFail = false;
    Boolean linkFail = false;
    final TProgram program = new TProgram();
    final TShader shader = new TShader(EShLanguage.EShLangVertex);
    final SWIGTYPE_p_TBuiltInResource resources = libspirvcrossj.getDefaultTBuiltInResource();
    final String[] names = {"dummy.vert"};

    final String dummyShader[] = {
            "#version 450\n" +
                    "\n" +
                    "layout(location = 0) out vec2 textureCoord;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    textureCoord = vec2((gl_VertexIndex << 1) & 2, gl_VertexIndex & 2);\n" +
                    "    gl_Position = vec4(textureCoord * 2.0f - 1.0f, 0.0f, 1.0f);\n" +
                    "}"

    };

    System.out.println("Shader code (" + dummyShader[0].length() + " bytes):\n" + dummyShader[0] + "\n--------------");

    final TShader.Includer inc = new TShader.ForbidIncluder();

    final String[] output = new String[5];
    final int[] sizes = {0};
    sizes[0] = -1;
    shader.setStringsWithLengths(dummyShader, sizes, 1);

    /*if(shader.preprocess(resources, 450, EProfile.ECoreProfile,
            false, true, EShMessages.EShMsgDefault, output, inc)) {
      System.out.println("Preprocessing done.");
    } else {
      System.out.println("Debug log: " + shader.getInfoDebugLog());
      System.out.println("Info log: " + shader.getInfoLog());
      throw new RuntimeException("Preprocessing failed.");
    }*/

    shader.setAutoMapBindings(true);

    int messages = EShMessages.EShMsgDefault;
    messages |= EShMessages.EShMsgVulkanRules;
    messages |= EShMessages.EShMsgSpvRules;

    if(!shader.parse(resources, 450, false, messages)) {
      compileFail = true;
    }

    System.out.println("Info log: " + shader.getInfoLog());
    System.out.println("Debug log: " + shader.getInfoDebugLog());

    if(compileFail) {
      throw new RuntimeException("Compilation failed");
    }
    System.out.println("----------------");
    System.out.println("Linking program...");

    program.addShader(shader);

    if(!program.link(EShMessages.EShMsgDefault)) {
      System.err.println("Linking failed!");
      linkFail = true;
    }

    if(!program.mapIO()) {
      System.err.println("IO mapping failed!");
      linkFail = true;
    }

    System.err.println(program.getInfoLog());
    System.err.println(program.getInfoDebugLog());

    if(linkFail || compileFail) {
      throw new RuntimeException("Linking failed!");
    }

//    program.buildReflection();
//    program.dumpReflection();

    final IntVec spirv = new IntVec();

    libspirvcrossj.glslangToSpv(program.getIntermediate(EShLanguage.EShLangVertex), spirv);

    System.out.println("Generated " + spirv.capacity() + " bytes of SPIRV bytecode.");

    libspirvcrossj.finalizeProcess();
  }
}
