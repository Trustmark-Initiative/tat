# Trustmark Assessment Tool (TAT)
This repository holds the source for the Trustmark Assessment Tool. 

## How to Use the TAT

If you are interested in trying out the TAT, we recommmended that you deploy it using the [Trustmark Tools Deployer](https://github.com/Trustmark-Initiative/trustmark-tools-deploy). You can quickly setup a local version of the tool to experiment with, and you can use that project to deploy an Internet facing version as well. For more information about how to use the TAT, please see the [TAT User Guide](https://github.com/Trustmark-Initiative/tat/wiki).

## How to Build

1. Download and Install Java11 SDK.

2. You must have a built copy of the https://github.com/Trustmark-Initiative/tmf-api available in your local Maven repository.

3. You must have built a copy of the https://github.com/Trustmark-Initiative/tmf-shared-views in your local Maven repository.

4. You can build the software with the included gradlew wrapper: './gradlew clean build'

5. You can run the software with docker, see the deployer for details: https://github.com/Trustmark-Initiative/trustmark-tools-deploy

