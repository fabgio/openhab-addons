ECHO ON
CALL mvn spotless:apply clean package
COPY target\*-SNAPSHOT.jar Z:
EXIT