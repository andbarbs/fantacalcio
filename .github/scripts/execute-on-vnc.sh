#!/bin/bash
# This script starts a VNC server on an available display,
# runs the provided command, and then shuts down the VNC server.

NEW_DISPLAY=1
DONE="no"

# Find the first available X display
while [ "$DONE" == "no" ]
do
  out=$(xdpyinfo -display :${NEW_DISPLAY} 2>&1)
  if [[ "$out" == name* ]] || [[ "$out" == Invalid* ]]
  then
    # Display is in use
    (( NEW_DISPLAY+=1 ))
  else
    # Display is free
    DONE="yes"
  fi
done

echo "Using first available display :${NEW_DISPLAY}"

OLD_DISPLAY=${DISPLAY}
# Start VNC server with a common resolution and color depth, restricted to localhost
vncserver ":${NEW_DISPLAY}" -localhost -geometry 1600x1200 -depth 24
export DISPLAY=:${NEW_DISPLAY}

# Run the command passed as arguments to this script (e.g., "mvn verify")
"$@"
TEST_RESULT="$?"

# Clean up
export DISPLAY=${OLD_DISPLAY}
vncserver -kill ":${NEW_DISPLAY}"
exit $TEST_RESULT
