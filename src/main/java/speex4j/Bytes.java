package speex4j;

public class Bytes {

  public static byte[] shortToByte(short number) {
    int temp = number;
    byte[] b = new byte[2];
    for (int i = 0; i < b.length; i++) {
      b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
      temp = temp >> 8; // 向右移8位
    }
    return b;
  }

  public static short byteToShort(byte[] b) {
    short s = 0;
    short s0 = (short) (b[0] & 0xff);// 最低位
    short s1 = (short) (b[1] & 0xff);
    s1 <<= 8;
    s = (short) (s0 | s1);
    return s;
  }

  public static byte[] toByteArray(short[] inputData) {
    int len = inputData.length * 2;
    byte[] ret = new byte[len];

    for (int i = 0; i < len; i += 2) {
      ret[i] = (byte) (inputData[i / 2] & 0xff);
      ret[i + 1] = (byte) ((inputData[i / 2] >> 8) & 0xff);
    }
    return ret;
  }

  protected static short[] toShortArray(byte[] inputData) {
    int len = inputData.length / 2;
    short[] ret = new short[len];

    for (int i = 0; i < len; i++) {
      ret[i] = (short) ((inputData[i * 2 + 1] << 8) & 0xffff | (inputData[i * 2] & 0x00ff));
    }
    return ret;
  }

  public static byte[] shortArray2ByteArray(short[] b) {
    byte[] rebt = new byte[b.length * 2];
    int index = 0;
    for (int i = 0; i < b.length; i++) {
      short st = b[i];
      byte[] bt = shortToByte(st);
      rebt[index] = bt[0];
      rebt[index + 1] = bt[1];
      index += 2;
    }
    return rebt;
  }

  public static byte[] concat(byte[]...arrays)
  {
    // Determine the length of the result array
    int totalLength = 0;
    for (int i = 0; i < arrays.length; i++)
    {
      totalLength += arrays[i].length;
    }

    // create the result array
    byte[] result = new byte[totalLength];

    // copy the source arrays into the result array
    int currentIndex = 0;
    for (int i = 0; i < arrays.length; i++)
    {
      System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
      currentIndex += arrays[i].length;
    }

    return result;
  }
}
