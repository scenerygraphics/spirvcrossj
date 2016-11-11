package is.ulrik.spirvcrossj;

import org.junit.Test;

import java.io.IOException;
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
  public void convertVulkanToGLSL310() throws IOException {
    System.loadLibrary("spirvcrossj");

    ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(Paths.get("/Users/ulrik/Code/ClearVolume/scenery/src/main/resources/scenery/backends/vulkan/shaders/DefaultDeferred.frag.spv")));
    IntVec spirv = new IntVec();
    IntBuffer ib = data.asIntBuffer();

    while(ib.hasRemaining()) {
      spirv.push_back(ib.get());
    }

    System.err.println("Read " + ib.position() + " longs");

    CompilerGLSL compiler = new CompilerGLSL(spirv);

    ShaderResources res = compiler.get_shader_resources();
    for(int i = 0; i < res.getUniform_buffers().capacity(); i++) {
      System.err.println(res.getUniform_buffers().get(i).getName());
    }

    CompilerGLSL.Options options = new CompilerGLSL.Options();
    options.setVersion(310);
    options.setEs(false);

    compiler.set_options(options);

    // output GLSL 3.10 code
    System.err.println(compiler.compile());
  }
}
