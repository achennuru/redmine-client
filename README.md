# Redmine Connector
## Fixing the project URL:
Please make sure that src/main/java/com/redmine/easy/RedmineConnector.java URI
URI = "";
## Building
```bash
mvn install
```
* This command creates and executable and self contained jar.
* Use com.enghouse.netboss.easy.CreateTask class to dynamically create tasks.
## Building unix executable.
Execute the following command to build an executable for unix environment.
```bash
./generate-executable
```
# Executing the client
## Unix
```bash
target/redmine-easy
```
## Windows

```bash
target/redmine-easy.exe
```
