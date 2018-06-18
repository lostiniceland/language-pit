#! /bin/bash
# use -e "http_proxy=http://<user>:<password>@<proxy>:<port>" when behind a corporate proxy with authentication. Ruby will then use this for outbound connections
docker run --interactive --tty --rm --user $UID --volume $(pwd):/app hiptest/hiptest-publisher  "$@"
