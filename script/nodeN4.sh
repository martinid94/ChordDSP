#!/bin/bash/

if [ -d "ring/N4" ]; then
	rm -r ring/N4
fi
mkdir ring/N4
cd out
echo "nodo N4"

(cat ../script/command && cat) | java main.demo.InternalNodeMain localhost 5715 ../ring/N4/
