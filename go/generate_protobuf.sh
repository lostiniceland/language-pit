protoc --go_out=$(pwd)/src/gobs/bikes/infrastructure --proto_path=$(pwd)/../protobuf/ bikes.proto

# protoc --go_out=$(pwd)/src/gobs/bikes/infrastructure --proto_path=$(pwd)/../protobuf/ wife.proto