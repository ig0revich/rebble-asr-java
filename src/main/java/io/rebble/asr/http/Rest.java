package io.rebble.asr.http;

import com.fasterxml.jackson.databind.*;
import ie.corballis.sox.*;
import io.rebble.asr.websockets.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import javax.servlet.http.*;
import lombok.*;
import lombok.extern.log4j.*;
import org.apache.commons.io.*;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.*;
import speex4j.*;

@Log4j2
@RestController
public class Rest {

    @Value("${vosk.server.uri}")
    private String voskServerUri;

    @AllArgsConstructor
    public static class Word {
        public final String word;
    }

    @AllArgsConstructor
    public static class Words {
        public final List<List<Word>> words;
    }

    @Autowired
    private ObjectMapper mapper;

    @GetMapping(path = "/heartbeat")
    public ResponseEntity<String> heatbeat() {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(path = "/NmspServlet/")
    public ResponseEntity<MultiValueMap<String, Object>> speechToText(
        @RequestParam MultiValueMap<String, Part> input
    ) {

        log.debug(input.keySet());
        log.debug("RequestData = {}",
            IOUtils.toString(input.getFirst("RequestData").getInputStream(), StandardCharsets.UTF_8.name()));
        log.debug("DictParameter = {}",
            IOUtils.toString(input.getFirst("DictParameter").getInputStream(), StandardCharsets.UTF_8.name()));

        long dlm = new Date().getTime();

        String spxFileRaw         = "/tmp/sample-" + dlm + "-raw.spx";
        String spxFileHeaderbyte  = "/tmp/sample-" + dlm + "-headerbyte.spx";
        String wavFile16k         = "/tmp/sample-" + dlm + "-16k.wav";
        String wavFile8k          = "/tmp/sample-" + dlm + "-8k.wav";

        try (OutputStream spxHeaderbyte = new FileOutputStream(new File(spxFileHeaderbyte));
             OutputStream spxRaw        = new FileOutputStream(new File(spxFileRaw));
             OutputStream wav16         = new FileOutputStream(new File(wavFile16k))
        ) {
            List<Part> parts = input.get("ConcludingAudioParameter");

            byte[][] data = new byte[parts.size()][];
            int idx = 0;
            for (Part part : parts) {
                byte[] bytes = IOUtils.toByteArray(part.getInputStream());

                spxRaw.write(bytes);

                bytes = ArrayUtils.add(bytes, 0, Integer.valueOf(bytes.length).byteValue());
                data[idx] = bytes;

                spxHeaderbyte.write(bytes);

                idx++;
            }

            try (SpeexDecoder decoder = new SpeexDecoder()) {
                wav16.write(SpeexUtils.pcm2wav(decoder.decode(Bytes.concat(data))));
            }

            // sox <source wav file> -r 8000 -b 16 <target wav file>
            new Sox("/usr/bin/sox")
                .inputFile(wavFile16k)
                .sampleRate(8000)
                .bits(16)
                .outputFile(wavFile8k)
                .verbose(6)
                .execute();

            String transcription = (String) mapper.readValue(
                new VoskServerClient(voskServerUri)
                    .getTranscription(
                        Files.readAllBytes(
                            new File(wavFile8k).toPath()
                        )
                    )
                , Map.class
            ).get("text");

            if (StringUtils.isEmpty(transcription)) {
                transcription = "unknown";
            }

            return prepareOutput(transcription);
        }
    }

    private ResponseEntity<MultiValueMap<String, Object>> prepareOutput(String text) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("QueryResult", new Words(Arrays.asList(Arrays.asList(new Word(text + "\\*no-space-before")))));
        return new ResponseEntity<>(form, HttpStatus.OK);
    }

}
