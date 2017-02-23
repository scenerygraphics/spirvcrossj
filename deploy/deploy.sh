#!/usr/bin/env bash

if [ "$TRAVIS" == 'true' ] && [ "$TRAVIS_BRANCH" == 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    echo "Starting Travis deploy..."
elif [ "$APPVEYOR" == 'True' ] && [ "$APPVEYOR_REPO_BRANCH" == 'master' ] && [ "$APPVEYOR_PULL_REQUEST_TITLE" == '' ]; then
    echo "Starting Appveyor deploy..."
else
    echo "Not deploying, as we are on a feature branch or PR."
    exit
fi

REPOSITORY_ID="ossrh"
SNAPSHOT_REPOSITORY="https://oss.sonatype.org/content/repositories/snapshots"
RELEASE_REPOSITORY="https://oss.sonatype.org/service/local/staging/deploy/maven2/"

VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.version |grep -Ev '(^\[|Download\w+:)'`
PLATFORM=`mvn help:active-profiles | grep platform | awk '{print $2}'`

if [ "$PLATFORM" == "platform-osx" ]
then
    CLASSIFIER="natives-macos"
elif [ "$PLATFORM" == "platform-linux" ]
then
    CLASSIFIER="natives-linux"
elif [ "$PLATFORM" == "platform-windows" ]
then
    CLASSIFIER="natives-windows"
fi

if [[ "$VERSION" == *"SNAPSHOT"* ]]
then
    echo "Configuring for SNAPSHOT deployment"
    REPOSITORY=$SNAPSHOT_REPOSITORY
    RELEASE_OPTS="-P release,sign"
else
    echo "Configuring for release deployment"
    REPOSITORY=$RELEASE_REPOSITORY
    RELEASE_OPTS="-P release,sign"
fi

echo "Deploying version $VERSION with classifier $CLASSIFIER to Sonatype..."

mvn $RELEASE_OPTS deploy --settings settings.xml
mvn gpg:sign-and-deploy-file -DgroupId=graphics.scenery -Dversion=$VERSION \
-DartifactId=spirvcrossj \
-Dfile=target/spirvcrossj-$VERSION-$CLASSIFIER.jar \
-DrepositoryId=$REPOSITORY_ID \
-Durl=$REPOSITORY \
-Dclassifier=$CLASSIFIER -DgeneratePom=false $RELEASE_OPTS --settings settings.xml
