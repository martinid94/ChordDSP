#!/bin/bash/

mkdir ring/N2
cd out
echo "nodo N2"

(cat ../script/command && cat) | java main.demo.InternalNodeMain localhost 6112 ../ring/N2
