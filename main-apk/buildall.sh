#!/bin/bash

#build all client apk for all enterprises

DATABASE_NAME=tianyi_test_new
id_query="select enterprise_id from tianyi_enterprises where enterprise_id > 1 and enterprise_status != 0"

for enterprise_id in `mysql -e "$id_query" -u root -pRed1linux $DATABASE_NAME |tail -n +2`;
do
    name_query="select enterprise_name from tianyi_enterprises where enterprise_id=$enterprise_id"
    enterprise_name=`mysql -e "$name_query" -u root -pRed1linux $DATABASE_NAME|tail -n +2`
    echo now build apk for enterprise id = $enterprise_id
    echo enterprise name is: $enterprise_name
    build_cmd="bash clientbuild.sh . . $enterprise_id $enterprise_name"
    $build_cmd
    if [ $? -ne 0 ];then
        echo build failed for $enterprise_id, $enterprise_name
    fi
done
