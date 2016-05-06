#!/bin/bash

set -e -x

cd "{{tempdir}}"
spring init --dependencies=web -build=maven {{basepath}}
