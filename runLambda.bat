IF [%1] == [] GOTO MissingArgs
java -cp .:target/AWSMetadata.jar com.bauenecp.DeleteNatGateway %1
:MissingArgs
ECHO "AWS VPC Machine name needs to be passed in as argument 1"
