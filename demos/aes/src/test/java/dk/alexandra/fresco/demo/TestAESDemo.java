/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.helpers.ResourcePoolHelper;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


public class TestAESDemo {

  @Test
  public void testAESDemo() throws Exception {
    int noPlayers = 2;
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<Integer>(noPlayers);
    for (int i = 1; i <= noPlayers; i++) {
      ports.add(9000 + i * 10);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
    for (int playerId : netConf.keySet()) {
      TestThreadConfiguration<ResourcePoolImpl, SequentialBinaryBuilder> ttc =
          new TestThreadConfiguration<ResourcePoolImpl, SequentialBinaryBuilder>();
      ttc.netConf = netConf.get(playerId);
      ProtocolSuite<ResourcePoolImpl, SequentialBinaryBuilder> suite =
          new DummyBooleanProtocolSuite();
      ProtocolEvaluator<ResourcePoolImpl> evaluator = new SequentialEvaluator<ResourcePoolImpl>();
      boolean useSecureConnection = false;
      ttc.sceConf = new TestSCEConfiguration<ResourcePoolImpl, SequentialBinaryBuilder>(suite,
          NetworkingStrategy.KRYONET, evaluator, ttc.netConf, useSecureConnection);
      conf.put(playerId, ttc);
    }

    TestThreadFactory f = new TestThreadFactory<ResourcePoolImpl, SequentialBinaryBuilder>() {
      @Override
      public TestThread<ResourcePoolImpl, SequentialBinaryBuilder> next(
          TestThreadConfiguration<ResourcePoolImpl, SequentialBinaryBuilder> conf) {
        return new TestThread<ResourcePoolImpl, SequentialBinaryBuilder>() {

          @Override
          public void test() throws Exception {

            Boolean[] input = null;
            if (conf.netConf.getMyId() == 2) {
              // 128-bit AES plaintext block
              input = ByteArithmetic.toBoolean("00112233445566778899aabbccddeeff");
            } else if (conf.netConf.getMyId() == 1) {
              // 128-bit key
              input = ByteArithmetic.toBoolean("000102030405060708090a0b0c0d0e0f");
            }

            AESDemo app = new AESDemo(conf.netConf.getMyId(), input);

            List<Boolean> aesResult = secureComputationEngine.runApplication(app,
                ResourcePoolCreator.createResourcePool(conf.sceConf));

            // Verify output state.
            String expected = "69c4e0d86a7b0430d8cdb78070b4c55a"; // expected cipher
            boolean[] actualBoolean = new boolean[aesResult.size()];
            int i = 0;
            for (Boolean b : aesResult) {
              actualBoolean[i++] = b;
            }
            String actual = ByteArithmetic.toHex(actualBoolean);
            Assert.assertEquals(expected, actual);

          }
        };
      }
    };

    TestThreadRunner.run(f, conf);
    ResourcePoolHelper.shutdown();
  }


}
