#!/bin/sh

VM_OPTIONS="-Xms128M -Xmx1024M"
JAR="filecrypt.jar"

java -jar ${VM_OPTIONS} ${JAR} $*

