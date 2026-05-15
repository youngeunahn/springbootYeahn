#!/bin/bash
set -e

SCOUTER_AGENT="./agent/scouter/scouter.agent.jar"
SCOUTER_CONF="./agent/scouter/conf/scouter.conf"

if [ -f "$SCOUTER_AGENT" ] && [ -n "$SCOUTER_COLLECTOR_IP" ]; then
  mkdir -p ./agent/scouter/conf
  cat > "$SCOUTER_CONF" <<EOF
obj_name=springbootYeahn-prod
net_collector_ip=$SCOUTER_COLLECTOR_IP
net_collector_udp_port=${SCOUTER_COLLECTOR_UDP_PORT:-6100}
net_collector_tcp_port=${SCOUTER_COLLECTOR_TCP_PORT:-6100}
profile_sql_escape_enabled=false
profile_connection_open_enabled=true
trace_db_jdbc_allow_full_stacktrace=true
EOF
  export JAVA_TOOL_OPTIONS="-javaagent:$SCOUTER_AGENT -Dscouter.config=$SCOUTER_CONF"
  echo "Starting Application with Scouter Agent..."
else
  echo "Starting Application without Scouter Agent. Check SCOUTER_COLLECTOR_IP and $SCOUTER_AGENT."
fi

java -jar target/yeahn-0.0.1-SNAPSHOT.jar
