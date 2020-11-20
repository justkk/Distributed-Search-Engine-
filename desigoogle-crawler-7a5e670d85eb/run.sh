cd hw1/555-hw1/
mvn clean install
mvn deploy:deploy-file -Dfile=target/homework-1-8.8-SNAPSHOT.jar -DpomFile=pom.xml -Durl=file:$PWD/../../maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true
cd ../../