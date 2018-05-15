#scalapbc --scala_out=$(pwd)/src/main/scala --proto_path=$(pwd)/../protobuf/ bikes.proto
scalapbc --scala_out=flat_package:build/generated/source/proto/main/scala src/main/proto/bikes.proto
scalapbc --scala_out=flat_package:build/generated/source/proto/main/scala src/main/proto/wife.proto