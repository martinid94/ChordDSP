#!/bin/bash/

if [ -d "ring" ]; then
	rm -r ring
fi

mkdir ring
mkdir ring/N5
cd out
echo "nodo N5"
cat | java main.demo.RingMain localhost 4444 ../ring/N5/
