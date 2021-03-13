#!/bin/bash

SCRIPT_DIR=$(realpath $(dirname ${BASH_SOURCE[0]}))
PROJECT_DIR=$(realpath $SCRIPT_DIR/..)

DOCS_DIR=$PROJECT_DIR/docs

# Clean previous docs
rm -rf $DOCS_DIR
mkdir -p $DOCS_DIR

# Generate new kdoc
cd $PROJECT_DIR
./gradlew lib:dokka

# Copy fresly generated kdoc
cp -r $PROJECT_DIR/lib/build/dokka $DOCS_DIR/dokka

# Commit docs
git add $DOCS_DIR
git commit $DOCS_DIR -m "docs(dokka): update dokka"