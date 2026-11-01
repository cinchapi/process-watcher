#!/usr/bin/env bash
# Sets the version in each build.gradle's pom.version line to $1, the version
# that version.sh is setting, and keeps a -SNAPSHOT suffix where the line has
# one.
set -euo pipefail
new=$1
v='[0-9]+\.[0-9]+\.[0-9]+'
find . -name build.gradle -not -path './.agents/*' -not -path './.git/*' |
  while IFS= read -r file; do
    sed -i.bak -E \
      -e "s/pom\.version = '$v'/pom.version = '$new'/g" \
      -e "s/pom\.version = '$v-SNAPSHOT'/pom.version = '$new-SNAPSHOT'/g" \
      "$file"
    rm "$file.bak"
  done
