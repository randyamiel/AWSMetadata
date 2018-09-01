#!/bin/bash
# TODO: write a master shell script that runs the sub-routines such as runLambda etc..
#       or we could use a config file.

if [ $# -eq 0 ]; then
    echo "AWS VPC Machine name needs to be passed in as argument 1"
    exit 1
fi
java -cp .:target/AWSMetadata.jar com.bauenecp.DeleteNatGateway $1
