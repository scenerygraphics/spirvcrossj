package graphics.scenery.spirvcrossj;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <Description>
 *
 * @author Ulrik Günther <hello@ulrik.is>
 */
public class TestVulkanToGLSL {
  static {
    try {
      Loader.loadNatives();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test public void convertVulkanToGLSL310() throws IOException, URISyntaxException {
    ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(Paths.get(TestVulkanToGLSL.class.getResource("fullscreen-quad.spv").toURI())));
    IntVec spirv = new IntVec();
    IntBuffer ib = data.asIntBuffer();

    while(ib.hasRemaining()) {
      spirv.pushBack(ib.get());
    }

    System.out.println("Read " + ib.position() + " opcodes from SPIR-V binary.\n");

    CompilerGLSL compiler = new CompilerGLSL(spirv);
    CompilerGLSL.Options options = new CompilerGLSL.Options();
    options.setVersion(310);
    options.setEs(false);

    compiler.setOptions(options);

    // output GLSL 3.10 code
    System.out.println("SPIR-V converted to GLSL 3.10:\n\n" + compiler.compile());
  }

  @Test public void listUniformBuffers() throws IOException, URISyntaxException {
    BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("spvFileList.txt")));
    List<String> spvFileList = in.lines().collect(Collectors.toList());
    in.close();

    for(String filename: spvFileList) {
      ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(Paths.get(TestVulkanToGLSL.class.getResource(filename).toURI())));
      IntVec spirv = new IntVec();
      IntBuffer ib = data.asIntBuffer();

      while (ib.hasRemaining()) {
        spirv.pushBack(ib.get());
      }

      System.out.println("Read " + ib.position() + " opcodes from SPIR-V binary " + filename + ".\n");

      CompilerGLSL compiler = new CompilerGLSL(spirv);

      ShaderResources res = compiler.getShaderResources();
      for (int i = 0; i < res.getUniformBuffers().capacity(); i++) {
        System.err.println(res.getUniformBuffers().get(i).getName());
      }
    }
  }
}
