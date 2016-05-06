#!/bin/bash

set -e -x

cd "{{tempdir}}"
spring init {{basepath}} --dependencies=web -build=maven
