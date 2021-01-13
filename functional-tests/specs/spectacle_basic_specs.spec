Spectacle spec with scenarios
===============================

tags: java, dotnet, ruby, python, js

* Initialize a project named "spec_with_scenarios" without example spec

Basic spec with one scenario
-------------------------------------

* Create a scenario "Sample scenario" in specification "Basic spec execution" with the following steps with implementation 

   |step text               |implementation                                          |
   |------------------------|--------------------------------------------------------|
   |First step              |"inside first step"                                     |
   |Second step             |"inside second step"                                    |
   |Third step              |"inside third step"                                     |
   |Step with "two" "params"|"inside step with parameters : " + param0 + " " + param1|

* Generate Spectacle Documentation for the current project

* Console should contain "Succesfully converted specs to html"

* Verify Spectacle Documentation

|totalSpecificationsCount |totalScenariosCount |
|-------------------------|--------------------|
|1                        |1                   |


Basic spec with multiple scenarios
-------------------------------------------

* Create a scenario "Sample scenario" in specification "Basic spec execution" with the following steps with implementation 

   |step text          |implementation     |
   |-------------------|-------------------|
   |First Scenario step|"inside first step"|

* Create a scenario "second scenario" in specification "Basic spec execution" with the following steps with implementation 

   |step text           |implementation      |
   |--------------------|--------------------|
   |Second Scenario step|"inside second step"|

* Create a scenario "third scenario" in specification "Basic spec execution" with the following steps with implementation 

   |step text          |implementation     |
   |-------------------|-------------------|
   |Third Scenario step|"inside third step"|

* Generate Spectacle Documentation for the current project

* Console should contain "Succesfully converted specs to html"

* Verify Spectacle Documentation

|totalSpecificationsCount |totalScenariosCount |
|-------------------------|--------------------|
|1                        |3                   |
