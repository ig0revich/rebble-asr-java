package io.rebble.asr.websockets;

import com.neovisionaries.ws.client.*;
import java.util.*;
import java.util.concurrent.*;

public class VoskServerClient {

    private final String uri;

    class Holder {
        String text;
    }

    public VoskServerClient(String uri) {
        this.uri = uri;
    }

    public String getTranscription(byte[] data) {

        Holder holder = new Holder();

        CountDownLatch latch = new CountDownLatch(1);

        WebSocketFactory factory;
        WebSocket ws = null;
        try {
            factory = new WebSocketFactory();
            ws = factory.createSocket(uri);
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) {
                    System.out.println(message);
                    if(message.contains("\"result\"")){
                        holder.text = message;
                        latch.countDown();
                    }
                }
            });
            ws.connect();

            for (int i = 0; i < data.length; i += 8000) {
                byte[] subdata = Arrays.copyOfRange(data, i, i + 8000);
                ws.sendBinary(subdata);
            }
            ws.sendText("{\"eof\" : 1}");

            latch.await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(null!=ws) {
                ws.disconnect();
            }
        }

        return holder.text;
    }

}