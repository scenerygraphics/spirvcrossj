package graphics.scenery.spirvcrossj;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <Description>
 *
 * @author Ulrik Günther <hello@ulrik.is>
 */
public class TestVulkanToGLSL {

  @Test
  public void convertVulkanToGLSL310() throws IOException, URISyntaxException {
    Loader.loadNatives();

    ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(Paths.get(TestVulkanToGLSL.class.getResource("fullscreen-quad.spv").toURI())));
    IntVec spirv = new IntVec();
    IntBuffer ib = data.asIntBuffer();

    while(ib.hasRemaining()) {
      spirv.add(ib.get());
    }

    System.out.println("Read " + ib.position() + " longs from SPIR-V binary.\n");

    CompilerGLSL compiler = new CompilerGLSL(spirv);

    ShaderResources res = compiler.getShaderResources();
    for(int i = 0; i < res.getUniformBuffers().capacity(); i++) {
      System.err.println(res.getUniformBuffers().get(i).getName());
    }

    CompilerGLSL.Options options = new CompilerGLSL.Options();
    options.setVersion(310);
    options.setEs(false);

    compiler.setOptions(options);

    // output GLSL 3.10 code
    System.out.println("SPIR-V converted to GLSL 3.10:\n\n" + compiler.compile());
  }
}
