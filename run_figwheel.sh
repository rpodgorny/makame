#!/bin/sh
set -e -x
exec clj -m figwheel.main -b dev -r
