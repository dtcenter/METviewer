#!/bin/sh

# Define the entrypoint 
SHELL ["/bin/sh", "-c"]
ENTRYPOINT echo "${CATALINA_HOME}/bin/startup.sh && /bin/bash"
