#!/bin/bash

(
    set -x;
    mkdir -p /opt/assessment-tool/files /opt/assessment-tool/lucene
    mysql -u root -p -e 'DROP DATABASE assessments;'
    mysql -u root -p -e 'DROP USER admin@localhost;'
    mysql -u root -p < `dirname "$0"`/setup.sql
)

