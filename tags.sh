#!/bin/sh

# Generate a TAGS file for the project

#find . \! -name '.*' -name '*.clj' | xargs etags --regex='/[ \t\(]*def[a-z]* \([a-z-!]+\)/\1/' --regex='/[ \t\(]*ns \([a-z.]+\)/\1/'
#find . \! -name '.*' -name '*.clj' | xargs etags --lang=lisp

find . -name '*.clj' | xargs etags --regex=@./clojure.tags

# find . \! -name '.*' -name '*.clj' \
#     | xargs etags \
#     --regex='/[ \t\(]*def[a-zA-Z!$%&*+\-.\/:<=>?@^_~]*[ \n\t]+\(\^{[^}]*}[ \n\t]+\|\)\([a-zA-Z!$%&*+\-.\/:<=>?@^_~]+\)/\2/s' \
#     --regex='/[ \t\(]*ns \([a-z.]+\)/\1/'
