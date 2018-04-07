#!/bin/bash/

cd out
echo "nodo esterno"

(cat ../script/insert && cat) | java main.demo.ExternalNodeMain ../external/
