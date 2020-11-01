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

echo "Preparing to release version:" $RELEASE_VERSION

# Update pom release version
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$RELEASE_VERSION
if ! git diff-index --quiet HEAD; then
  # Deploy release version
  echo "Deploying changes for version:" $RELEASE_VERSION
  if mvn clean install deploy; then
    # Once deploy is successful
    if [[ "$RELEASE_VERSION" == *-SNAPSHOT ]]; then
      echo "Working on snapshot version"
    else
      # Commit release version
      echo "git commit" $RELEASE_VERSION
      git commit . -m "Preparing release $RELEASE_VERSION"
    fi
  fi
fi
