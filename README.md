# Project For COMP-3940

This is the tomcat server for AI generation of images and questions for quizzes.

[Picogen AI](https://picogen.io/) is used for text to image generation

## Setup 

1. Requires tomcat to be installed as well as Java.
2. Clone this repo into `PATH/TO/tomcat/webapps`
3. Get your own API key from Picogen and create a secrets.properties file in `/WEB-INF`
4. Enter `api.key=<INSERT API KEY HERE>` in the secrets.properties file
