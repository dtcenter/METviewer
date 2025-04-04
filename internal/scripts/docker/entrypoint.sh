#!/bin/sh

# Define the entrypoint 
ENTRYPOINT echo "${CATALINA_HOME}/bin/startup.sh && /bin/bash"
