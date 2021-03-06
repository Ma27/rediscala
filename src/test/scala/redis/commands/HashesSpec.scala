package redis.commands

import redis._

import akka.util.ByteString

class HashesSpec extends RedisStandaloneServer {

  "Hashes commands" should {
    "HDEL" in {
      val r = for {
        _ <- redis.hset("hdelKey", "field", "value")
        d <- redis.hdel("hdelKey", "field", "fieldNonexisting")
      } yield {
        d shouldBe 1
      }
      r.futureValue
    }

    "HEXISTS" in {
      val r = for {
        _ <- redis.hset("hexistsKey", "field", "value")
        exist <- redis.hexists("hexistsKey", "field")
        notExist <- redis.hexists("hexistsKey", "fieldNotExisting")
      } yield {
        exist shouldBe true
        notExist shouldBe false
      }
      r.futureValue
    }

    "HGET" in {
      val r = for {
        _ <- redis.hset("hgetKey", "field", "value")
        get <- redis.hget("hgetKey", "field")
        get2 <- redis.hget("hgetKey", "fieldNotExisting")
      } yield {
        get shouldBe Some(ByteString("value"))
        get2 shouldBe None
      }
      r.futureValue
    }

    "HGETALL" in {
      val r = for {
        _ <- redis.hset("hgetallKey", "field", "value")
        get <- redis.hgetall("hgetallKey")
        get2 <- redis.hgetall("hgetallKeyNotExisting")
      } yield {
        get shouldBe Map("field" -> ByteString("value"))
        get2 shouldBe Map.empty
      }
      r.futureValue
    }

    "HINCRBY" in {
      val r = for {
        _ <- redis.hset("hincrbyKey", "field", "10")
        i <- redis.hincrby("hincrbyKey", "field", 1)
        ii <- redis.hincrby("hincrbyKey", "field", -1)
      } yield {
        i shouldBe 11
        ii shouldBe 10
      }
      r.futureValue
    }

    "HINCRBYFLOAT" in {
      val r = for {
        _ <- redis.hset("hincrbyfloatKey", "field", "10.5")
        i <- redis.hincrbyfloat("hincrbyfloatKey", "field", 0.1)
        ii <- redis.hincrbyfloat("hincrbyfloatKey", "field", -1.1)
      } yield {
        i shouldBe 10.6
        ii shouldBe 9.5
      }
      r.futureValue
    }

    "HKEYS" in {
      val r = for {
        _ <- redis.hset("hkeysKey", "field", "value")
        keys <- redis.hkeys("hkeysKey")
      } yield {
        keys shouldBe Seq("field")
      }
      r.futureValue
    }

    "HLEN" in {
      val r = for {
        _ <- redis.hset("hlenKey", "field", "value")
        hLength <- redis.hlen("hlenKey")
      } yield {
        hLength shouldBe 1
      }
      r.futureValue
    }

    "HMGET" in {
      val r = for {
        _ <- redis.hset("hmgetKey", "field", "value")
        hmget <- redis.hmget("hmgetKey", "field", "nofield")
      } yield {
        hmget shouldBe Seq(Some(ByteString("value")), None)
      }
      r.futureValue
    }

    "HMSET" in {
      val r = for {
        _ <- redis.hmset("hmsetKey", Map("field" -> "value1", "field2" -> "value2"))
        v1 <- redis.hget("hmsetKey", "field")
        v2 <- redis.hget("hmsetKey", "field2")
      } yield {
        v1 shouldBe Some(ByteString("value1"))
        v2 shouldBe Some(ByteString("value2"))
      }
      r.futureValue
    }

    "HMSET update" in {
      val r = for {
        _ <- redis.hdel("hsetKey", "field")
        set <- redis.hset("hsetKey", "field", "value")
        update <- redis.hset("hsetKey", "field", "value2")
        v1 <- redis.hget("hsetKey", "field")
      } yield {
        set shouldBe true
        update shouldBe false
        v1 shouldBe Some(ByteString("value2"))
      }
      r.futureValue
    }

    "HMSETNX" in {
      val r = for {
        _ <- redis.hdel("hsetnxKey", "field")
        set <- redis.hsetnx("hsetnxKey", "field", "value")
        doNothing <- redis.hsetnx("hsetnxKey", "field", "value2")
        v1 <- redis.hget("hsetnxKey", "field")
      } yield {
        set shouldBe true
        doNothing shouldBe false
        v1 shouldBe Some(ByteString("value"))
      }
      r.futureValue
    }

    "HSCAN" in {
      val initialData = (1 to 20).grouped(2).map(x => x.head.toString -> x.tail.head.toString).toMap
      val r = for {
        _ <- redis.del("hscan")
        _ <- redis.hmset("hscan", initialData)
        scanResult <- redis.hscan[String]("hscan", count = Some(300))
      } yield {
        scanResult.data.values.toList.map(_.toInt).sorted shouldBe (2 to 20 by 2)
        scanResult.index shouldBe 0
      }
      r.futureValue
    }

    "HVALS" in {
      val r = for {
        _ <- redis.hdel("hvalsKey", "field")
        emp <- redis.hvals("hvalsKey")
        _ <- redis.hset("hvalsKey", "field", "value")
        some <- redis.hvals("hvalsKey")
      } yield {
        emp shouldBe empty
        some shouldBe Seq(ByteString("value"))
      }
      r.futureValue
    }
  }
}
