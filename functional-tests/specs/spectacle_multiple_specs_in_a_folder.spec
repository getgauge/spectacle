Spectacle multiple specs in a folder
====================================

tags: java, dotnet, ruby, python, js

* Initialize a project named "multiple_specs_in_folder" without example spec

Document multiple specs in a folder
------------------------------------

* Create "scenario 1" in "Spec 1" within sub folder "specs" with the following steps 

   |step text   |implementation      |
   |------------|--------------------|
   |First step1 |"inside first step" |
   |Second step1|"inside second step"|

* Create "scenario 2" in "Spec 2" within sub folder "specs" with the following steps 

   |step text   |implementation      |
   |------------|--------------------|
   |First step2 |"inside first step" |
   |Second step2|"inside second step"|

* Create "scenario 3" in "Spec 3" within sub folder "specs" with the following steps 

   |step text   |implementation      |
   |------------|--------------------|
   |First step3 |"inside first step" |
   |Second step3|"inside second step"|

* Generate Spectacle Documentation for the current project

* Console should contain "Succesfully converted specs to html"

* Verify Spectacle Documentation

|totalSpecificationsCount |totalScenariosCount |
|-------------------------|--------------------|
|3                        |3                   |
