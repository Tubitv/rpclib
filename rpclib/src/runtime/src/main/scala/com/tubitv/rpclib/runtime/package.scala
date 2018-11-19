package com.tubitv.rpclib

import io.grpc.stub.StreamObserver

package object runtime {

  type GrpcOperator[I, O] = StreamObserver[O] => StreamObserver[I]

}
