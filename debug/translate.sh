#!/usr/bin/env bash

# debugging script: try to translate an object

export TICKET="ddf02ffd-f6f4-4cc5-817e-d022c6818049@demo"
export OSD_ID=48428
export ATTR_VALUE=53
set -e

# check translation:
curl --header "ticket:${TICKET}" -d attribute=/sysMeta/object/language/id -d attribute_value=${ATTR_VALUE} \
-d object_relation_type_id=959 -d root_relation_type_id=960 -d source_id=${OSD_ID} \
http://localhost:8080/cinnamon/cinnamon/legacy?command=checktranslation

# create translation
curl --header "ticket:${TICKET}" -d attribute=/sysMeta/object/language/id -d attribute_value=${ATTR_VALUE} \
-d object_relation_type_id=959 -d root_relation_type_id=960 -d source_id=${OSD_ID} \
-d target_folder_id=47869 \
http://localhost:8080/cinnamon/cinnamon/legacy?command=createtranslation

# create version
echo ""
curl --header "ticket:${TICKET}" -d preid=${OSD_ID} http://localhost:8080/cinnamon/cinnamon/legacy?command=version
echo ""
echo "please enter new version id:"
read new_version

# create translation for new version
echo ""
curl --header "ticket:${TICKET}" -d attribute=/sysMeta/object/language/id -d attribute_value=${ATTR_VALUE} \
-d object_relation_type_id=959 -d root_relation_type_id=960 -d source_id=${new_version} \
-d target_folder_id=47869 \
http://localhost:8080/cinnamon/cinnamon/legacy?command=createtranslation

# check translation for new version
echo ""
curl --header "ticket:${TICKET}" -d attribute=/sysMeta/object/language/id -d attribute_value=${ATTR_VALUE} \
-d object_relation_type_id=959 -d root_relation_type_id=960 -d source_id=${new_version} \
http://localhost:8080/cinnamon/cinnamon/legacy?command=checktranslation
echo ""


