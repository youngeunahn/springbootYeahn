#!/bin/bash
# 실행 시점에만 스카우터 에이전트 옵션 적용
export JAVA_TOOL_OPTIONS="-javaagent:./agent/scouter/scouter.agent.jar -Dscouter.config=./agent/scouter/conf/scouter.conf"

echo "Starting Application with Scouter Agent..."
java -jar target/yeahn-0.0.1-SNAPSHOT.jar
