#!/bin/sh
java -Dserver.port=$1 -jar ./target/zk-demo-0.0.1-SNAPSHOT.jar -z127.0.0.1
