package speex4j;

import com.sun.jna.*;
import java.io.*;
import java.util.concurrent.atomic.*;
import org.slf4j.*;

public class SpeexEncoder implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpeexEncoder.class);

  private AtomicBoolean stopped = new AtomicBoolean(false);

  private Pointer state;

  public SpeexEncoder() {
    this(Mode.WIDE_BAND, 8, 16000);
  }

  public SpeexEncoder(Mode mode, int quality, int samplingRate) {
    state = Speex.INSTANCE.create_encoder(mode.getCode(), quality, samplingRate);
  }

  public byte[] encode(byte[] pcm) {
    int len = pcm.length;
    int size = ((len - 1) / 640 + 1) * 71;
    short[] data = Bytes.toShortArray(pcm);
    byte[] spx = new byte[size];
    int spxSize = Speex.INSTANCE.encode(state, data, data.length, spx);
    LOGGER.debug("size: {}, spxSize: {}", size, spxSize);
    return spx;
  }

  @Override
  public void close() {
    if (stopped.getAndSet(true)) {
      return;
    }
    LOGGER.debug("close");
    Speex.INSTANCE.destroy_encoder(state);
  }
}
