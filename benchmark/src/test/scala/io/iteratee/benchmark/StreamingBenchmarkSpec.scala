package io.iteratee.benchmark

import org.scalatest.FlatSpec
import scala.Predef.intWrapper

class StreamingBenchmarkSpec extends FlatSpec {
  val benchmark: StreamingBenchmark = new StreamingBenchmark
  val taken = (0 until 10000).toVector

  "The streaming benchmark" should "correctly gather elements using io.iteratee.modules.id" in {
    assert(benchmark.takeLongs0II === taken)
  }

  it should "correctly gather elements using io.iteratee.scalaz" in {
    assert(benchmark.takeLongs1IT === taken)
  }

  it should "correctly gather elements using io.iteratee.twitter" in {
    assert(benchmark.takeLongs2IR === taken)
  }

  it should "correctly gather elements using scalaz-stream" in {
    assert(benchmark.takeLongs3S === taken)
  }

  it should "correctly gather elements using scalaz-iteratee" in {
    assert(benchmark.takeLongs4Z === taken)
  }

  it should "correctly gather elements using play-iteratee" in {
    assert(benchmark.takeLongs5P === taken)
  }

  it should "correctly gather elements using the collections library" in {
    assert(benchmark.takeLongs6C === taken)
  }

  it should "correctly gather elements using fs2" in {
    assert(benchmark.takeLongs7F === taken)
  }
}
