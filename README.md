# cordapp-java

## Network Setup

* clone the repository
* Run command `./gradlew build` from directory to build the gradle project
* Run command `./gradlew deployNodes` from directory to build corda 5 nodes
* Switch to the directory build/nodes and run `./runnodes` to start 5 corda nodes
* Run `./gradlew runSellerServer` to start api-server for seller
* Run `./gradlew runBuyerServer` to start api-server for buyer
* Run `./gradlew runBankServer` to start api-server for bank

