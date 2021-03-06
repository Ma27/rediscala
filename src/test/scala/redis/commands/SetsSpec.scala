package redis.commands

import akka.util.ByteString
import redis._



class SetsSpec extends RedisStandaloneServer {

  "Sets commands" should {
    "SADD" in {
      val r = for {
        _ <- redis.del("saddKey")
        s1 <- redis.sadd("saddKey", "Hello", "World")
        s2 <- redis.sadd("saddKey", "World")
        m <- redis.smembers("saddKey")
      } yield {
        s1 shouldBe 2
        s2 shouldBe 0
        m should contain theSameElementsAs(Seq(ByteString("Hello"), ByteString("World")))
      }
      r.futureValue
    }

    "SCARD" in {
      val r = for {
        _ <- redis.del("scardKey")
        c1 <- redis.scard("scardKey")
        _ <- redis.sadd("scardKey", "Hello", "World")
        c2 <- redis.scard("scardKey")
      } yield {
        c1 shouldBe 0
        c2 shouldBe 2
      }
      r.futureValue
    }

    "SDIFF" in {
      val r = for {
        _ <- redis.del("sdiffKey1")
        _ <- redis.del("sdiffKey2")
        _ <- redis.sadd("sdiffKey1", "a", "b", "c")
        _ <- redis.sadd("sdiffKey2", "c", "d", "e")
        diff <- redis.sdiff("sdiffKey1", "sdiffKey2")
      } yield {
        diff should contain theSameElementsAs(Seq(ByteString("a"), ByteString("b")))
      }
      r.futureValue
    }

    "SDIFFSTORE" in {
      val r = for {
        _ <- redis.del("sdiffstoreKey1")
        _ <- redis.del("sdiffstoreKey2")
        _ <- redis.sadd("sdiffstoreKey1", "a", "b", "c")
        _ <- redis.sadd("sdiffstoreKey2", "c", "d", "e")
        diff <- redis.sdiffstore("sdiffstoreKeyDest", "sdiffstoreKey1", "sdiffstoreKey2")
        m <- redis.smembers("sdiffstoreKeyDest")
      } yield {
        diff shouldBe 2
        m should contain theSameElementsAs(Seq(ByteString("a"), ByteString("b")))
      }
      r.futureValue
    }

    "SINTER" in {
      val r = for {
        _ <- redis.del("sinterKey1")
        _ <- redis.del("sinterKey2")
        _ <- redis.sadd("sinterKey1", "a", "b", "c")
        _ <- redis.sadd("sinterKey2", "c", "d", "e")
        inter <- redis.sinter("sinterKey1", "sinterKey2")
      } yield {
        inter should contain theSameElementsAs(Seq(ByteString("c")))
      }
      r.futureValue
    }


    "SINTERSTORE" in {
      val r = for {
        _ <- redis.del("sinterstoreKey1")
        _ <- redis.del("sinterstoreKey2")
        _ <- redis.sadd("sinterstoreKey1", "a", "b", "c")
        _ <- redis.sadd("sinterstoreKey2", "c", "d", "e")
        inter <- redis.sinterstore("sinterstoreKeyDest", "sinterstoreKey1", "sinterstoreKey2")
        m <- redis.smembers("sinterstoreKeyDest")
      } yield {
        inter shouldBe 1
        m should contain theSameElementsAs(Seq(ByteString("c")))
      }
      r.futureValue
    }

    "SISMEMBER" in {
      val r = for {
        _ <- redis.del("sismemberKey")
        _ <- redis.sadd("sismemberKey", "Hello", "World")
        is <- redis.sismember("sismemberKey", "World")
        isNot <- redis.sismember("sismemberKey", "not member")
      } yield {
        is shouldBe true
        isNot shouldBe false
      }
      r.futureValue
    }

    "SMEMBERS" in {
      val r = for {
        _ <- redis.del("smembersKey")
        _ <- redis.sadd("smembersKey", "Hello", "World")
        m <- redis.smembers("smembersKey")
      } yield {
        m should contain theSameElementsAs(Seq(ByteString("Hello"), ByteString("World")))
      }
      r.futureValue
    }

    "SMOVE" in {
      val r = for {
        _ <- redis.del("smoveKey1")
        _ <- redis.del("smoveKey2")
        _ <- redis.sadd("smoveKey1", "one", "two")
        _ <- redis.sadd("smoveKey2", "three")
        isMoved <- redis.smove("smoveKey1", "smoveKey2", "two")
        isNotMoved <- redis.smove("smoveKey1", "smoveKey2", "non existing")
        m <- redis.smembers("smoveKey2")
      } yield {
        isMoved shouldBe true
        isNotMoved shouldBe false
        m should contain theSameElementsAs(Seq(ByteString("three"), ByteString("two")))
      }
      r.futureValue
    }

    "SPOP" in {
      val r = for {
        _ <- redis.del("spopKey")
        _ <- redis.sadd("spopKey", "one", "two", "three")
        pop <- redis.spop("spopKey")
        popNone <- redis.spop("spopKeyNonExisting")
        m <- redis.smembers("spopKey")
      } yield {
        Seq(ByteString("three"), ByteString("two"), ByteString("one")) should contain(pop.get)
        popNone shouldBe empty
        m should contain atLeastOneElementOf (Seq(ByteString("three"), ByteString("two"), ByteString("one")))
      }
      r.futureValue
    }

    "SRANDMEMBER" in {
      val r = for {
        _ <- redis.del("srandmemberKey")
        _ <- redis.sadd("srandmemberKey", "one", "two", "three")
        randmember <- redis.srandmember("srandmemberKey")
        randmember2 <- redis.srandmember("srandmemberKey", 2)
        randmemberNonExisting <- redis.srandmember("srandmemberKeyNonExisting", 2)
        m <- redis.smembers("spopKey")
      } yield {
        Seq(ByteString("three"), ByteString("two"), ByteString("one")) should contain(randmember.get)
        randmember2 should have size 2
        randmemberNonExisting shouldBe empty
      }
      r.futureValue
    }

    "SREM" in {
      val r = for {
        _ <- redis.del("sremKey")
        _ <- redis.sadd("sremKey", "one", "two", "three", "four")
        rem <- redis.srem("sremKey", "one", "four")
        remNothing <- redis.srem("sremKey", "five")
        m <- redis.smembers("sremKey")
      } yield {
        rem shouldBe 2
        remNothing shouldBe 0
        m should contain theSameElementsAs(Seq(ByteString("three"), ByteString("two")))
      }
      r.futureValue
    }

    "SSCAN" in {
      val r = for {
        _ <- redis.sadd("sscan", (1 to 20).map(_.toString):_*)
        scanResult <- redis.sscan[String]("sscan", count = Some(100))
      } yield {
        scanResult.index shouldBe 0
        scanResult.data.map(_.toInt).sorted shouldBe (1 to 20)
      }

      r.futureValue
    }

    "SUNION" in {
      val r = for {
        _ <- redis.del("sunionKey1")
        _ <- redis.del("sunionKey2")
        _ <- redis.sadd("sunionKey1", "a", "b", "c")
        _ <- redis.sadd("sunionKey2", "c", "d", "e")
        union <- redis.sunion("sunionKey1", "sunionKey2")
      } yield {
        union should contain theSameElementsAs(Seq(ByteString("a"), ByteString("b"), ByteString("c"), ByteString("d"), ByteString("e")))
      }
      r.futureValue
    }


    "SUNIONSTORE" in {
      val r = for {
        _ <- redis.del("sunionstoreKey1")
        _ <- redis.del("sunionstoreKey2")
        _ <- redis.sadd("sunionstoreKey1", "a", "b", "c")
        _ <- redis.sadd("sunionstoreKey2", "c", "d", "e")
        union <- redis.sunionstore("sunionstoreKeyDest", "sunionstoreKey1", "sunionstoreKey2")
        m <- redis.smembers("sunionstoreKeyDest")
      } yield {
        union shouldBe 5
        m should contain theSameElementsAs(Seq(ByteString("a"), ByteString("b"), ByteString("c"), ByteString("d"), ByteString("e")))
      }
      r.futureValue
    }
  }
}
