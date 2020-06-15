# iFogSimWithDataConsistencyManagement

This is an extension of iFogSim that makes it possible to simulate and evaluate replica placement and data consistency management strategies in context of Fog computing and IoT. This extension uses the external tool Cplex to compute the data placement for the iFogStor Strategy.  

This extension involves several data placement strategies: 

* For one replica placement : iFogStor
* For serveral replica placement : Exact, iFogStorS, iFogStorP

The path  of the main class is: src/org/fog/examples/DataPlacement.java

Next, various configurations and setups to reuse this extension are shown : 
1- Clone this repository in your machine.  
2- Install Cplex: there is a free acadimique version.  
3- Add the cplex.jar as an external Jar and then modify the native link access in the external Jar.  
4- In order to accelerate simulations, there is a parallel computation of all shortests paths exiting between Fog nodes. This parallel computation is enabled by making the variable in line 169 of the main class `parallel = true`. If there somme erreurs araise when calling the lib libFloydWarshall.so please switch the aforementioned variable to `false`. 
