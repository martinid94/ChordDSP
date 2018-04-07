#!/bin/bash/

if [ -d "ring/N1" ]; then
	rm -r ring/N1
fi
mkdir ring/N1
cd out
echo "nodo N1"

(cat ../script/command && cat) | java main.demo.InternalNodeMain localhost 4015 ../ring/N1/
