#!/bin/bash

# Define environment variables to retrieve
variables=(
"ACCESS_TOKEN"
"REALM"
"RUM_TOKEN"
"HEC_TOKEN"
"HEC_URL"
 )

# Iterate through the list of variables and print their values
for var in "${variables[@]}"; do
    value=$(printenv "$var")
    echo "$var = $value"
done