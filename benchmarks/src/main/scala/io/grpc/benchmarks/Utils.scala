package io.grpc.benchmarks

import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.benchmarks.proto.messages.{Payload, PayloadType, SimpleRequest, SimpleResponse}

/**
  * Scala implementation of grpc-java/benchmarks/Utils.java. We skip what is not needed
  */
object Utils {

  def makeResponse(request: SimpleRequest): SimpleResponse = {
    if (request.responseSize > 0)  {
      if (!PayloadType.COMPRESSABLE.equals(request.responseType)) {
        throw Status.INTERNAL.augmentDescription("Error creating payload.").asRuntimeException()
      }

      val payload = Payload(
        `type` = request.responseType,
        body = ByteString.copyFrom(new Array[Byte](request.responseSize))
      )

      SimpleResponse(payload = Some(payload))
    } else {
      SimpleResponse.defaultInstance
    }
  }
}
