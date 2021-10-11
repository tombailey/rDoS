FROM adoptopenjdk/openjdk16:alpine

COPY ./src /usr/src/myapp

WORKDIR /usr/src/myapp

RUN javac com/example/Main.java

CMD ["java", "com/example/Main"]
