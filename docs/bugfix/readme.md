# Documentation for bugfixes

Currently known: AssetPipelineFilter.groovy is broken. Tomcat8 will not find the images for the administrator GUI.
Install the version of https://github.com/dewarim/grails-asset-pipeline locally via 'grails maven-install' before 
 building cinnamon.war
(until the plugin is merged into the master branch).
