#scalapbc --scala_out=$(pwd)/src/main/scala --proto_path=$(pwd)/../protobuf/ bikes.proto
scalapbc --scala_out=flat_package:src/main/scala-gen src/main/proto/bikes.proto
scalapbc --scala_out=flat_package:src/main/scala-gen src/main/proto/wife.proto