#!/bin/bash/

if [ -z "$1" ]; then
	gnome-terminal -e "sh script/nodeN5B.sh"
elif [ "$1" = "N4" ]; then
	gnome-terminal -e "sh script/nodeN4.sh"
elif [ "$1" = "N3" ]; then
	gnome-terminal -e "sh script/nodeN3.sh"
elif [ "$1" = "N2" ]; then
	gnome-terminal -e "sh script/nodeN2.sh"
elif [ "$1" = "N1" ]; then
	gnome-terminal -e "sh script/nodeN1.sh"
elif [ "$1" = "external" ]; then
	gnome-terminal -e "sh script/externalNode.sh"
fi
