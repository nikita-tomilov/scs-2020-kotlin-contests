#!/bin/bash
rm -rf ./compiled-tasks
mkdir -p ./compiled-tasks

echo "%%%%%%%% CONTEST C1 %%%%%%%%"
echo ""
/usr/bin/env python3 build-tasks-for-contest.py c1 ./statements ./compiled-tasks ./src
