#!/bin/bash

set -e -x

cd "{{tempdir}}/{{project_name}}"
fly -t concourse-lite login --concourse-url http://192.168.100.4:8080
fly -t concourse-lite set-pipeline -c ci/pipeline.yml -l ci/credentials.yml -p {{project_name}} -n
fly -t concourse-lite unpause-pipeline -p {{project_name}}
