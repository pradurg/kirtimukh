#
# Copyright (c) 2020 Pradeep A. Dalvi <prad@apache.org>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#!/usr/bin/env bash

#Default release version
. .version
RELEASE_VERSION=${1:-$RELEASE_VERSION}
MAVEN_PROFILE=""

tag_release() {
  # Tag release
  git tag -l $RELEASE_VERSION
  git tag -a -f -m "Tagging production build $RELEASE_VERSION"

  echo "Pushing tag $RELEASE_VERSION to repo origin"
  git push $RELEASE_VERSION
}

commit_version() {
  # Once deploy is successful, Commit release version
  echo "git commit" $RELEASE_VERSION
  git --version
  git commit . -m "Preparing version release $RELEASE_VERSION"
}

merge_to_main() {
  # Push changes to the main branch
  echo "Pushing HEAD to branch main of origin repository"
  git push HEAD:main
}

deploy_artifacts() {
  if [ $NON_REMOTE_DEPLOY ]; then
    mvn clean deploy $MAVEN_PROFILE -DskipTests=true -B
  else
    mvn clean deploy $MAVEN_PROFILE --settings .travis/maven-settings.xml -DskipTests=true -B
  fi
}

prepare_version() {
  echo "Preparing to release version:" $RELEASE_VERSION

  # Update pom release version
  mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$RELEASE_VERSION
}

prepare_next_snapshot() {
  NEXT_SNAPSHOT_VERSION="$(echo $RELEASE_VERSION | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g')-SNAPSHOT"
  echo "Updating to next Snapshot version: " $NEXT_SNAPSHOT_VERSION
  mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$NEXT_SNAPSHOT_VERSION
  echo "RELEASE_VERSION=$NEXT_SNAPSHOT_VERSION" >.version
  git commit . -m "Preparing next snapshot version $NEXT_SNAPSHOT_VERSION"
}

gpg_signing_ready() {
  if [ $NON_REMOTE_DEPLOY ]; then
    echo "No gpg ops required for local"
  else
    openssl aes-256-cbc -K $encrypted_f094dd62560a_key -iv $encrypted_f094dd62560a_iv -in .travis/gpg.asc.enc -out .travis/gpg.asc -d
    gpg --fast-import .travis/gpg.asc
  fi
}

gpg_cleanup() {
  rm -f .travis/gpg.asc
}

finalise_snapshot() {
  git push origin
}

if [[ "$RELEASE_VERSION" == *-SNAPSHOT ]]; then
  echo "Working on snapshot version: " $RELEASE_VERSION
  deploy_artifacts
elif ! git diff-index --quiet HEAD; then
  prepare_version
  MAVEN_PROFILE="-Prelease"

  gpg_signing_ready
  # Deploy release version
  echo "Deploying changes for version:" $RELEASE_VERSION
  if deploy_artifacts; then
    gpg_cleanup
    commit_version
    merge_to_main
    tag_release
    prepare_next_snapshot
    finalise_snapshot
  else
    gpg_cleanup
  fi
fi
