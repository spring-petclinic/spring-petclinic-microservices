#!/bin/bash

cd make-it-crash-be
docker build -t make-it-crash-be .
cd ..

cd make-it-crash-fe
docker build -t make-it-crash-fe .
cd ..

docker run -d -p 8081:8081 make-it-crash-be
docker run -d -p 8082:80 make-it-crash-fe
