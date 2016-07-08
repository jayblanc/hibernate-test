# hibernate-test

This project show a very strange behaviour of Hibernate ORM : 

Create a persistent object A
Set some properties (String) on A : A.setName("toto")
Persist A and commit

Load A using id based find
Change some properties values on A but DO NOT MERGE A
Create new persistent object B and copy the properties (including modifed ones) of A in B : B.setName(A.getName())
Persiste B and commit

Change reference of A with B : A = B
Merge A 


This usecase cause the Entity A and B to be persisted with same values even if A has not been really merged because of switching reference !



