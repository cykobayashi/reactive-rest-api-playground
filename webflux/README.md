## Requirements

1. Java - 1.8.x
2. Maven - 3.x.x
3. MongoDB - 3.x.x

## Steps to Setup

**2. Build and run the app using maven**

```bash
mvn package
java -jar target/webflux-demo-0.0.1-SNAPSHOT.jar
```

Alternatively, you can run the app without packaging it using -

```bash
mvn spring-boot:run
```

The server will start at <http://localhost:8080>