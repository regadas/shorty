FROM gcr.io/google_appengine/openjdk8
VOLUME /tmp
ADD target/scala-2.11/shorty-gae-assembly-1.0.jar app.jar
ENTRYPOINT [ "java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
