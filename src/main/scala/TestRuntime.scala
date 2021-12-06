package com.idemia.zio

import zio._
import zio.internal.Platform

trait TestRuntime extends Runtime[ZEnv] {
  override val platform: Platform = Platform.default
  override val environment: ZEnv  = Runtime.unsafeFromLayer(ZEnv.live, platform).environment
}