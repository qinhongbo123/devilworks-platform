#!/bin/bash

if [ $# -lt 3 ]; then
    echo "usage: clientbuild.sh src_dir theme_dir enterprise_id"
    exit -1
fi

#update client code
client_src_dir=`readlink -f $1`
theme_dir=`readlink -f $2`
ENTERPRISE_ID=$3
ENTERPRISE_NAME=$4

echo "client source directory:"$client_src_dir
echo "theme file directory:"$theme_dir
echo "enterprise id:"$ENTERPRISE_ID

cd $1
#svn update
git pull

#copy client code to temp dir
tmp_dir=`mktemp -d`
build_tmp_dir="build"
cd $tmp_dir
\cp $client_src_dir -fr $build_tmp_dir
cd $build_tmp_dir

#copy theme files
if [ -d $theme_dir ]; then
    \cp $theme_dir/res/drawable* -fr ./res/
else
    echo "theme does not exist"
    exit 1
fi


#clean current directory
# svn status |awk '{if($1=="?") {print $2}}' |xargs rm -fr
git pull

#generate build.xml
android update project --name surf-platform -t 1 -p . 

#get client code version
#CLIENT_VERSION=`svn info |grep Revision |awk '{print $2}'`
CLIENT_VERSION=`date -%s`

#replace version in source code
sed -i "s/BUILD_VERSION\ =\ \"68\"/BUILD_VERSION\ =\ \"${CLIENT_VERSION}\"/" src/com/surfing/setting/SettingActivity.java

if [ -n "$ENTERPRISE_NAME" ]; then
    sed -i "s/APP_PublicPlatform/$ENTERPRISE_NAME_Platform/g" res/values/strings.xml
    sed -i "s/APP_PublicPlatform/$ENTERPRISE_NAME信息平台/g" res/values-zh-rCN/strings.xml
fi

if [ $ENTERPRISE_ID -ne "41" ]; then
    echo "remove assets/userinfo.xml"
    rm -frv assets/userinfo.xml
fi

#compile client
ant release

#dir and file name
BIN_DIR="bin"
UNSIGNED_APK_NAME=`find $BIN_DIR -name *apk`
#sign apk
APK_NAME="surf-platform-$CLIENT_VERSION.apk"
echo "client file name:" $UNSIGNED_APK_NAME $APK_NAME 

jarsigner -verbose -keystore ShenTangTech -storepass linuxred -keypass linuxred -signedjar $APK_NAME $UNSIGNED_APK_NAME laworks

#move apk to target directory

DOWNLOAD_DIR="/var/www/tianyi/apk/$ENTERPRISE_ID"

if [ ! -d $DOWNLOAD_DIR ]; then
    mkdir -p $DOWNLOAD_DIR
fi

echo $DOWNLOAD_DIR, "latest version is:" $CLIENT_VERSION
\cp $APK_NAME $DOWNLOAD_DIR


