#!/usr/bin/env python3

import sys
import os
import hashlib
import subprocess

LONG_MAX = (1 << 64) - 1
DEBUG = True

def usage(msg=None):
    if msg:
        print(msg)
    print("Usage: {0} <gcc/g++ command>".format(sys.argv[0]))
    sys.exit(1)

def debug(s):
    if DEBUG:
        print("{} {}".format(sys.argv[0], s))

compiler_command = sys.argv[1:]
if len(compiler_command) == 0:
    usage()



# Hash all the source files and traverse any directories recursively
sha1 = hashlib.sha1()


# Hash the build command too
sha1.update(" ".join(compiler_command).encode())
debug("Compiler command hashed: {}".format(sha1.hexdigest()))


preprocess_command = [*compiler_command, "-E"]

# Remove any output file (needs to write to stdout)
try:
    index = compiler_command.index("-o")
    del preprocess_command[index:index+2]
except ValueError:
    usage("Missing output compiler flag")

# Preprocess the source file(s)
debug("Preprocess cmd: {}".format(preprocess_command))
data = subprocess.check_output(preprocess_command)

# Hash the content
sha1.update(data)
debug("Content hashed: {}".format(sha1.hexdigest()))

# Set the SOURCE_DATE_EPOCH environment variable to the hash value
os.environ["SOURCE_DATE_EPOCH"] = str(int(sha1.hexdigest(), base=16) % LONG_MAX)
debug("SOURCE_DATE_EPOCH: {}".format(os.environ["SOURCE_DATE_EPOCH"]))

# Run the compiler with the environement variable set
subprocess.run(compiler_command)
