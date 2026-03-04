@echo off
set JAVA_HOME=D:\Program Files\Android Studio\install\jbr
set PATH=%JAVA_HOME%\bin;D:\Program Files\apache-maven-3.9.12\bin;%PATH%
cd /d D:\SUPERP~2\backend
mvn spring-boot:run
