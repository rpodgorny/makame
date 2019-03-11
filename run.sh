#!/bin/sh
set -e -x
exec clj -m backend.core -r
