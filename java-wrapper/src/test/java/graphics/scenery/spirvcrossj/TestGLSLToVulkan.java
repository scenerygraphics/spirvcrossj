package graphics.scenery.spirvcrossj;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * <Description>
 *
 * @author Ulrik Günther <hello@ulrik.is>
 */
public class TestGLSLToVulkan {

  @Test
  public void convertGLSLToVulkan() throws IOException, URISyntaxException {
    Loader.loadNatives();

    libspirvcrossj.initializeProcess();

    final TProgram program = new TProgram();
    final TShader shader = new TShader(EShLanguage.EShLangVertex);
    final SWIGTYPE_p_TBuiltInResource resources = libspirvcrossj.getDefaultTBuiltInResource();
    final String[] names = {"dummy.vert"};

    final String[] dummyShader = {"#version 450\n" +
            "#extension GL_ARB_separate_shader_objects: enable\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "\n" +
            "}\n"};

    System.out.println("Shader code: " + dummyShader[0]);


    shader.setAutoMapBindings(true);

    final TShader.Includer inc = new TShader.ForbidIncluder();

    final int[] sizes = {dummyShader[0].length()};
    shader.setStringsWithLengthsAndNames(dummyShader, null, names, 1);

    /*if(shader.preprocess(resources, 400, EProfile.ECoreProfile, false, false, EShMessages.EShMsgDefault, null, inc)) {
      System.out.println("Preprocessing done.");
    } else {
      System.out.println("Debug log: " + shader.getInfoDebugLog());
      System.out.println("Info log: " + shader.getInfoLog());
      throw new RuntimeException("Preprocessing failed.");
    }*/

    if(!shader.parse(resources, 330, false, EShMessages.EShMsgDefault)) {
      System.out.println("Debug log: " + shader.getInfoDebugLog());
      System.out.println("Info log: " + shader.getInfoLog());
      throw new RuntimeException("Parsing shader failed!");
    }

    System.out.println("Linking program...");

    program.addShader(shader);
    program.link(EShMessages.EShMsgDefault);

    System.out.println("shader code: " + dummyShader[0]);
  }
}
