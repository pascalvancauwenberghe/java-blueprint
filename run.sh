clear
mvn clean package
java -jar -Dserver.port=8083 target/asyncblueprint-0.0.1-SNAPSHOT.jar
