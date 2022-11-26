#!/usr/bin/env bash


mvn clean install
mvn clean install -Pjs-compile -f helper
mvn clean install -Pjs-compile -f string

