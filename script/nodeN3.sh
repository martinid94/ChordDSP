#!/bin/bash/

mkdir ring/N3
cd out
echo "nodo N3"

(cat ../script/command && cat) | java main.demo.InternalNodeMain localhost 5782 ../ring/N3/
