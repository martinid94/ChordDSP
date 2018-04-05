#!/bin/bash
cd src/
mkdir ../out/

javac -d ../out/ main/*/*.java
javac -d ../out/ test/*.java
