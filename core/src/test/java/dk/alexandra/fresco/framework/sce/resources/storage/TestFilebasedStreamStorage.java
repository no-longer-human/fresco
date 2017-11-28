package dk.alexandra.fresco.framework.sce.resources.storage;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class TestFilebasedStreamStorage {

  StreamedStorage storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());

  /**
   * Removes any remaining files generated by the tests in this class.
   */
  @AfterClass
  public static void removeDanglingFile() {
    try {
      Files.delete(Paths.get("test-obj"));
      Files.delete(Paths.get("foo"));
      Files.delete(Paths.get("bar"));
    } catch (IOException e) {
      // Nevermind - file is likely not present then.
    }
  }



  @Test
  public void testPutAndGetObject() {
    String myObj = "This is a test";
    storage.putObject("test-obj", "test-key", myObj);
    String s = storage.getObject("test-obj", "test-key");
    storage.shutdown();
    Assert.assertThat(s, Is.is(myObj));
  }


  @Test
  public void testPutAndGetNext() throws NoMoreElementsException {
    String myObj = "This is a test";
    boolean success = storage.putNext("test-obj", myObj);
    Assert.assertTrue(success);
    String s = storage.getNext("test-obj");
    storage.shutdown();
    Assert.assertThat(s, Is.is(myObj));
  }

  @Test(expected = MPCException.class)
  public void testEmptyFile() throws NoMoreElementsException {
    try {
      (new FileOutputStream("foo")).close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    storage.getNext("foo");
    fail("Should not be reachable");
  }

  @Test(expected = MPCException.class)
  public void testWriteAfterClose() throws NoMoreElementsException {
    String myObj1 = "Test1";
    String myObj2 = "Test2";
    boolean success1 = storage.putNext("bar", myObj1);
    Assert.assertTrue(success1);
    storage.shutdown();
    storage.putNext("bar", myObj2);
    fail("Should not be reachable.");
  }


  private static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  @Test(expected = MPCException.class)
  public void testClassNotFound() throws NoMoreElementsException {
    try {
      // A representation of a class we no longer know
      String stringRep =
          "aced000573720038646b2e616c6578616e6472"
          + "612e66726573636f2e6672616d65776f726b"
          + "2e7363652e7265736f7572"
          + "6365732e73746f726167652e4861636b"
          + "1e61559c1df507160200007870";
      FileOutputStream fos = new FileOutputStream("foo");
      fos.write(hexStringToByteArray(stringRep));
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    storage.getNext("foo");
    fail("Should not be reachable");
  }

  @Test(expected = MPCException.class)
  public void testGetNonExisting2() throws NoMoreElementsException {
    storage.getNext("test-obj-2");
    Assert.fail();
  }

  @Test(expected = MPCException.class)
  public void testGetNonExisting() throws NoMoreElementsException {
    storage.getNext("test-obj-2");
    Assert.fail();
  }

  @Test(expected = MPCException.class)
  public void testPutBadName() throws NoMoreElementsException {
    storage.putNext("\0", "data");
    Assert.fail();
  }

  @Test(expected = NoMoreElementsException.class)
  public void testMultipleReadWrites() throws NoMoreElementsException {
    String myObj = "This is a test";
    boolean success = storage.putNext("test-obj", myObj);
    Assert.assertTrue(success);
    success = storage.putNext("test-obj", "This-is-second-test");
    Assert.assertTrue(success);
    Serializable s = storage.getNext("test-obj");
    Assert.assertThat(s, Is.is(myObj));
    s = storage.getNext("test-obj");
    Assert.assertThat(s, Is.is("This-is-second-test"));
    storage.getNext("test-obj");

    Assert.fail();
  }



}
