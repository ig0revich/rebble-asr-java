```
$ sudo docker pull ig0revich/rebble-asr-java:0.0.1-SNAPSHOT && \
  sudo docker run -it \
  -p 8080:80 \
  -e vosk.server.uri="<Vosk Server Endpoint>" \
  -v <local folder>:/tmp \
  ig0revich/rebble-asr-java:0.0.1-SNAPSHOT
```