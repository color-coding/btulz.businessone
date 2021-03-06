#!/bin/sh
echo '*****************************************************************'
echo '              deploy_packages.sh                                 '
echo '                    by niuren.zhu                                '
echo '                          2017.09.07                             '
echo '  说明：                                                         '
echo '    1. 发布jar包到maven仓库。                                    '
echo '    2. 在setting.xml的<servers>节点下添加                        '
echo '          <server>                                               '
echo '              <id>ibas-maven</id>                                '
echo '              <username>用户名</username>                        '
echo '              <password>密码</password>                          '
echo '          </server>                                              '
echo '*****************************************************************'
# 设置参数变量
cd $(dirname $0)
WORK_FOLDER=${PWD}
# 仓库根地址
ROOT_URL=http://maven.colorcoding.org/repository/
# 仓库名称
REPOSITORY=$1
# 设置默认仓库名称
if [ "${REPOSITORY}" = "" ]; then REPOSITORY=maven-releases; fi
# 使用的仓库信息
REPOSITORY_ID=ibas-maven
REPOSITORY_URL=${ROOT_URL}${REPOSITORY}

echo --检查maven运行环境
mvn -v >/dev/null
if [ $? -ne 0 ]; then
  echo 请检查MAVEN是否正常
  exit 1
fi

echo --发布地址：${REPOSITORY_URL}
# 发布父项
if [ -e ${WORK_FOLDER}/pom.xml ]; then
  mvn deploy:deploy-file \
    -Dfile=${WORK_FOLDER}/pom.xml \
    -DpomFile=${WORK_FOLDER}/pom.xml \
    -Durl=${REPOSITORY_URL} \
    -DrepositoryId=${REPOSITORY_ID} \
    -Dpackaging=pom
fi
# 发布子项
while read line; do
  if [ -e ${WORK_FOLDER}/${line}/pom.xml ]; then
    for PACKAGE in $(find release -name "${line}-*.jar"); do
      mvn deploy:deploy-file \
        -Dfile=${PACKAGE} \
        -DpomFile=${WORK_FOLDER}/${line}/pom.xml \
        -Durl=${REPOSITORY_URL} \
        -DrepositoryId=${REPOSITORY_ID} \
        -Dpackaging=jar
    done
  fi
done <${WORK_FOLDER}/compile_order.txt | sed 's/\r//g'

# 发布父项
if [ -e ${WORK_FOLDER}/btulz.transforms.b1/pom.xml ]; then
  mvn deploy:deploy-file \
    -Dfile=${WORK_FOLDER}/btulz.transforms.b1/pom.xml \
    -DpomFile=${WORK_FOLDER}/btulz.transforms.b1/pom.xml \
    -Durl=${REPOSITORY_URL} \
    -DrepositoryId=${REPOSITORY_ID} \
    -Dpackaging=pom
fi
# 发布子项
FILE_POM=btulz.transforms.b1/pom.b188.xml
FILE_JAR=btulz.transforms.b1-0.1.0-8.8.jar
if [ -e ${WORK_FOLDER}/${FILE_POM} ]; then
  if [ -e ${WORK_FOLDER}/release/${FILE_JAR} ]; then
    mvn deploy:deploy-file \
      -Dfile=${WORK_FOLDER}/release/${FILE_JAR} \
      -DpomFile=${WORK_FOLDER}/${FILE_POM} \
      -Durl=${REPOSITORY_URL} \
      -DrepositoryId=${REPOSITORY_ID} \
      -Dpackaging=jar
  fi
fi
FILE_POM=btulz.transforms.b1/pom.b191.xml
FILE_JAR=btulz.transforms.b1-0.1.0-9.1.jar
if [ -e ${WORK_FOLDER}/${FILE_POM} ]; then
  if [ -e ${WORK_FOLDER}/release/${FILE_JAR} ]; then
    mvn deploy:deploy-file \
      -Dfile=${WORK_FOLDER}/release/${FILE_JAR} \
      -DpomFile=${WORK_FOLDER}/${FILE_POM} \
      -Durl=${REPOSITORY_URL} \
      -DrepositoryId=${REPOSITORY_ID} \
      -Dpackaging=jar
  fi
fi
FILE_POM=btulz.transforms.b1/pom.b192.xml
FILE_JAR=btulz.transforms.b1-0.1.0-9.2.jar
if [ -e ${WORK_FOLDER}/${FILE_POM} ]; then
  if [ -e ${WORK_FOLDER}/release/${FILE_JAR} ]; then
    mvn deploy:deploy-file \
      -Dfile=${WORK_FOLDER}/release/${FILE_JAR} \
      -DpomFile=${WORK_FOLDER}/${FILE_POM} \
      -Durl=${REPOSITORY_URL} \
      -DrepositoryId=${REPOSITORY_ID} \
      -Dpackaging=jar
  fi
fi
FILE_POM=btulz.transforms.b1/pom.b193.xml
FILE_JAR=btulz.transforms.b1-0.1.0-9.3.jar
if [ -e ${WORK_FOLDER}/${FILE_POM} ]; then
  if [ -e ${WORK_FOLDER}/release/${FILE_JAR} ]; then
    mvn deploy:deploy-file \
      -Dfile=${WORK_FOLDER}/release/${FILE_JAR} \
      -DpomFile=${WORK_FOLDER}/${FILE_POM} \
      -Durl=${REPOSITORY_URL} \
      -DrepositoryId=${REPOSITORY_ID} \
      -Dpackaging=jar
  fi
fi
FILE_POM=btulz.transforms.b1/pom.b110.xml
FILE_JAR=btulz.transforms.b1-0.1.0-10.jar
if [ -e ${WORK_FOLDER}/${FILE_POM} ]; then
  if [ -e ${WORK_FOLDER}/release/${FILE_JAR} ]; then
    mvn deploy:deploy-file \
      -Dfile=${WORK_FOLDER}/release/${FILE_JAR} \
      -DpomFile=${WORK_FOLDER}/${FILE_POM} \
      -Durl=${REPOSITORY_URL} \
      -DrepositoryId=${REPOSITORY_ID} \
      -Dpackaging=jar
  fi
fi

# 发布工具包集合
if [ -e ${WORK_FOLDER}/release/btulz.transforms.tar ]; then
  mvn deploy:deploy-file \
    -DgroupId=org.colorcoding.tools \
    -DartifactId=btulz.transforms \
    -Durl=${REPOSITORY_URL} \
    -DrepositoryId=${REPOSITORY_ID} \
    -Dfile=${WORK_FOLDER}/release/btulz.transforms.tar \
    -Dpackaging=tar \
    -Dversion=b1-latest
fi
echo --操作完成
