Social Network Simulation in Neo4j
======

Simulation of a social network in Switzerland:

- 2485 municipalities -[:ARE_IN]-> 139 regions -[:ARE_IN]-> 26 cantons

- 8039060 inhabitants -[:LIVE_IN]-> 2485 municipalities

- ~35% of the inhabitants are social (http://en.wikipedia.org/wiki/Facebook_statistics)

- each social inhabitant is -[:FRIEND_OF]-> ~90 inhabitants (std. dev. 9)

TO-DO
======

- each social inhabitant is -[:INTERESTED_IN]-> ~15 activities (std. dev. 5)

- each social inhabitant -[:POSTED]-> ~20 updates




