#!/usr/bin/env bash

# debugging script: create many objects, use in multiple terminals to provoke concurrency problems.

export TICKET="d857c7cd-35db-4ba9-9225-b80ed35c2615@demo"
export TICKET="9f3d8fcf-d14c-49f9-8193-0b1adf2739da@localhost"
export QUERY="<BooleanQuery><Clause+occurs='must'><TermQuery+fieldName='name'>test<%2FTermQuery><%2FClause><%2FBooleanQuery>"


set -e

# send start/stop signal to collect data for flamegraphs
echo start | nc 127.0.0.1 9999

for i in `seq 1 1000`;
do
    curl --header "ticket:${TICKET}" -d preid=12554 http://localhost:8080/cinnamon/cinnamon/legacy?command=version
    echo ""
    curl --header "ticket:${TICKET}" -d query=${QUERY} http://localhost:8080/cinnamon/cinnamon/legacy?command=searchobjects > /tmp/searchResult.xml
    echo ""
done

echo stop | nc 127.0.0.1 9999