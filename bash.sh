#!/bin/bash

# Run the Java program and capture its output
output=$(java -jar LsPlus.jar)

# Extract the path from the output
new_dir=$(echo "$output" | grep "^PATH:" | sed 's/^PATH://')

# Optionally, display the output for debugging
echo "Java program output:"
echo "$output"

# Change to the directory if a path was found
if [ -n "$new_dir" ]; then
    cd "$new_dir" || {
        echo "Failed to change directory to $new_dir"
        exit 1
    }
    echo "Directory changed to: $PWD"
else
    echo "No path found in the Java program output."
    exit 1
fi
