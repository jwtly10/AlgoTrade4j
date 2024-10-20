#!/bin/bash

while IFS= read -r line; do
    if [[ $line == \#* ]] || [[ -z $line ]]; then
        continue
    fi

    printf "export %s\n" "$line"

done <.env