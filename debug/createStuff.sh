#!/usr/bin/env bash

# debugging script: create many objects, use in multiple terminals to provoke concurrency problems.

export TICKET="ddf02ffd-f6f4-4cc5-817e-d022c6818049@demo"
export QUERY="<BooleanQuery><Clause+occurs='must'><TermQuery+fieldName='name'>test<%2FTermQuery><%2FClause><%2FBooleanQuery>"


set -e

for i in `seq 1 1000`;
do
    curl --header "ticket:${TICKET}" -d preid=12554 http://localhost:8080/cinnamon/cinnamon/legacy?command=version
    echo ""
    curl --header "ticket:${TICKET}" -d query=${QUERY} http://localhost:8080/cinnamon/cinnamon/legacy?command=searchobjects > /tmp/searchResult.xml
    echo ""
done