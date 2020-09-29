#!/bin/bash

rm -rf /opt/assessment-tool/files/*
rm -rf /opt/assessment-tool/lucene/*
mysql -uroot -p -e "drop database assessments; create database assessments;"

