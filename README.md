# GIK2QR Project

## Usage

Download the project and navigate to the project folder.

```sh
git clone https://github.com/sebdanielsson/gik2q3-project-nlp
cd gik2q3-project-nlp
```

Install maven, increase the Maven timeout, and download the dependencies.

In the lab environment some repositories are dead and need to be removed. Those include the ius*, ambari*, pgdg*, and mysql* repositories. Delete them with `rm ius*` `rm ambari*` `rm pgdg*`, and `rm mysql*`.

```sh
yum install -y maven
mvn package -Dmaven.wagon.http.timeout=960000
mvn package
```

Download example data.

```sh
curl https://raw.githubusercontent.com/dscape/spell/master/test/resources/big.txt
```

Create the working directories and copy the test file to the Hadoop cluster.

```sh
hdfs dfs -mkdir -p /tmp/test/input
hdfs dfs -mkdir -p /tmp/test/output
hdfs dfs -copyFromLocal ./big.txt /tmp/test/input/big.txt
hdfs dfs -ls /tmp/test/input
hdfs dfs -ls /tmp/test/output
```

Run the program.

```sh
hadoop jar ./target/gik2q3-project-nlp-1.0-SNAPSHOT.jar /tmp/test/input /tmp/test/output
```
