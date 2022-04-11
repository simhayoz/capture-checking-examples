Compiled cask 3.0.0 manually using `./mill cask\[3.0.0\].assembly`, on jar compilation using `scalac -Ycc -classpath cask.jar tests/run-custom-args/captures/colltest5/TestRequests.scala`:

```scala
-- Error: cask/src-3/cask/router/Macros.scala:270:22 ---------------------------
The expression's type {*} sig.T -> sig.V is not allowed to capture the root capability `*`.
This usually means that a capability persists longer than its allowed lifetime.
```

--> backing to cask 2.13.5

Cannot make it work using 2.13.5 due to other errors


-----------------------------------------------

Works with cask.jar, does not for for cask_3.jar (with error above) difference:

```diff
@@ -266,9 +266,10 @@ object Macros {
                   ctx,
                   (sig.default match {
                     case None => None
-                    case Some(getter) =>
-                      val value = getter.asInstanceOf[Cls => Any](clazz)
-                      Some(value)
+                    case Some(_) =>
+                      // val value = getter.asInstanceOf[Cls => Any](clazz)
+                      // Some(value)
+                      None
                   }),
                   sig
                 )
```

Not a feasible solution as it will limit the functionalities of the application.

```scala
[[syntax trees at end of                        cc]] // tests/run-custom-args/captures/colltest5/websockets/TestWebSockets.scala
package <empty> {
  final lazy module val TestWebSockets: TestWebSockets = new TestWebSockets()
  @CaptureChecked @SourceFile(
    "tests/run-custom-args/captures/colltest5/websockets/TestWebSockets.scala"
  ) final module class TestWebSockets() extends cask.MainRoutes() {
    private[this] type $this = TestWebSockets.type
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[TestWebSockets.type])
    @websocket("/connect/:userName") def showUserProfile(userName: String):
      cask.WebsocketResult
     =
      {
        if userName.!=("haoyi") then
          cask.endpoints.WebsocketResult.Response[? String](
            cask.package.Response.apply[? String]("", 403,
              cask.package.Response.apply$default$3[String]
            , cask.package.Response.apply$default$4[String])
          )(
            {
              def $anonfun(s: ? String): ? cask.model.Response.Data =
                cask.model.Response.Data.WritableData[? String](s)(
                  {
                    def $anonfun(s: String): ? geny.Writable =
                      geny.Writable.StringWritable(s)
                    closure($anonfun)
                  }
                )
              closure($anonfun)
            }
          )
         else
          cask.package.WsHandler.apply(
            {
              {
                def $anonfun(channel: ? cask.endpoints.WsChannelActor):
                  ? castor.Actor[? cask.util.Ws.Event]
                 =
                  {
                    cask.package.WsActor.apply(
                      {
                        final class $anon() extends
                          scala.runtime.AbstractPartialFunction
                        [cask.util.Ws.Event, Unit](), Serializable {
                          override final def isDefinedAt(x: cask.util.Ws.Event)
                            :
                          Boolean =
                            matchResult1[<notype>]:
                              {
                                case val x1:
                                  (x : cask.util.Ws.Event) @unchecked @unchecked
                                 =
                                  x:(x : cask.util.Ws.Event) @unchecked:

                                      (x : cask.util.Ws.Event) @unchecked
                                        @unchecked

                                if x1.$isInstanceOf[cask.util.Ws.Text] then
                                  {
                                    case val x6: ? cask.util.Ws.Text =
                                      cask.package.Ws.Text.unapply(
                                        x1.$asInstanceOf[cask.util.Ws.Text]
                                      )
                                    case val x7: ? String = x6._1
                                    if "".==(x7) then return[matchResult1] true
                                       else
                                    ()
                                    case val data: ? String = x7
                                    return[matchResult1] true
                                  }
                                 else ()
                                return[matchResult1] false
                              }
                          override final def applyOrElse[
                            A1 <: cask.util.Ws.Event
                          , B1 >: Unit](x: A1, default: A1 => B1): B1 =
                            matchResult2[ >: Unit]:
                              {
                                case val x8: (x : A1) @unchecked @unchecked =
                                  x:(x : A1) @unchecked:
                                    (x : A1) @unchecked @unchecked
                                if x8.$isInstanceOf[cask.util.Ws.Text] then
                                  {
                                    case val x13: ? cask.util.Ws.Text =
                                      cask.package.Ws.Text.unapply(
                                        x8.$asInstanceOf[cask.util.Ws.Text]
                                      )
                                    case val x14: ? String = x13._1
                                    if "".==(x14) then
                                      return[matchResult2]
                                        {
                                          channel.send(
                                            cask.package.Ws.Close.apply(
                                              cask.package.Ws.Close.
                                                $lessinit$greater$default$1
                                            ,
                                              cask.package.Ws.Close.
                                                $lessinit$greater$default$2
                                            )
                                          )(
                                            sourcecode.FileName.apply(
                                              "TestWebSockets.scala"
                                            ):sourcecode.FileName
                                          ,
                                            sourcecode.Line.apply(9):
                                              sourcecode.Line
                                          )
                                        }
                                     else ()
                                    case val data: ? String = x14
                                    return[matchResult2]
                                      {
                                        channel.send(
                                          cask.package.Ws.Text.apply(
                                            userName.+(" ").+(data)
                                          )
                                        )(
                                          sourcecode.FileName.apply(
                                            "TestWebSockets.scala"
                                          ):sourcecode.FileName
                                        ,
                                          sourcecode.Line.apply(11):
                                            sourcecode.Line
                                        )
                                      }
                                  }
                                 else ()
                                return[matchResult2] default.apply(x)
                              }
                        }
                        new

                            scala.runtime.AbstractPartialFunction[
                              cask.util.Ws.Event
                            , Unit]
                           with Serializable {...}
                        ()
                      }
                    )(TestWebSockets.actorContext, TestWebSockets.log)
                  }
                closure($anonfun)
              }
            }
          )(TestWebSockets.actorContext, TestWebSockets.log)
      }
    {
      val Routes_this: (TestWebSockets : TestWebSockets.type) = TestWebSockets
      {
        {
          val x$proxy1:

              cask.router.RoutesEndpointsMetadata[
                (Routes_this : (TestWebSockets : TestWebSockets.type))
              ]

           =
            cask.router.RoutesEndpointsMetadata.apply[
              (Routes_this : (TestWebSockets : TestWebSockets.type))
            ](
              List.apply[

                  cask.router.EndpointMetadata[
                    (Routes_this : (TestWebSockets : TestWebSockets.type))
                  ]

              ](
                [
                  {
                    val entrypoint:

                        cask.router.EntryPoint[
                          (Routes_this : (TestWebSockets : TestWebSockets.type))
                        , cask.Request]

                     =
                      cask.router.EntryPoint.apply[
                        (Routes_this : (TestWebSockets : TestWebSockets.type))
                      , cask.Request]("showUserProfile",
                        List.apply[

                            List[
                              cask.router.ArgSig[Any,
                                (Routes_this :
                                  (TestWebSockets : TestWebSockets.type)
                                )
                              , Any, cask.Request]
                            ]

                        ](
                          [
                            List.apply[

                                cask.router.ArgSig[Any,
                                  (Routes_this :
                                    (TestWebSockets : TestWebSockets.type)
                                  )
                                , Any, cask.Request]

                            ](
                              [
                                cask.router.ArgSig.apply[Any,

                                    (Routes_this :
                                      (TestWebSockets : TestWebSockets.type)
                                    )

                                , Any, cask.Request]("userName", "String", None
                                  ,
                                None)(
                                  cask.endpoints.QueryParamReader.StringParam.
                                    asInstanceOf
                                  [cask.router.ArgReader[Any, Any, cask.Request]
                                    ]
                                )
                               :

                                  cask.router.ArgSig[Any,
                                    (Routes_this :
                                      (TestWebSockets : TestWebSockets.type)
                                    )
                                  , Any, cask.Request]

                              ]
                            )
                           :

                              List[
                                cask.router.ArgSig[Any,
                                  (Routes_this :
                                    (TestWebSockets : TestWebSockets.type)
                                  )
                                , Any, cask.Request]
                              ]

                          ]
                        )
                      , None,
                        {
                          def $anonfun(
                            clazz:

                                (Routes_this :
                                  (TestWebSockets : TestWebSockets.type)
                                )

                          , ctx: cask.Request, argss: Seq[Map[String, Any]],
                            sigss:

                                Seq[
                                  Seq[
                                    cask.router.ArgSig[Any, ?, ?, cask.Request]
                                  ]
                                ]

                          ): cask.router.Result[Any] =
                            {
                              val parsedArgss:

                                  Seq[
                                    Seq[
                                      Either[Seq[cask.router.Result.ParamError]
                                        ,
                                      Any]
                                    ]
                                  ]

                               =
                                sigss.zip[Map[String, Any]](argss).map[

                                    Seq[
                                      Either[Seq[cask.router.Result.ParamError]
                                        ,
                                      Any]
                                    ]

                                ](
                                  {
                                    def $anonfun(
                                      x$1:
                                        (
                                          Seq[
                                            cask.router.ArgSig[Any, ?, ?,
                                              cask.Request
                                            ]
                                          ] @uncheckedVariance
                                        , Map[String, Any])
                                    ):

                                        Seq[
                                          Either[
                                            Seq[cask.router.Result.ParamError]
                                          , Any]
                                        ]

                                     =
                                      matchResult5[<notype>]:
                                        {
                                          case val x19:

                                              (x$1 : (
                                                Seq[
                                                  cask.router.ArgSig[Any, ?, ?,
                                                    cask.Request
                                                  ]
                                                ] @uncheckedVariance
                                              , Map[String, Any]))

                                           = x$1
                                          if x19.ne(null) then
                                            {
                                              case val sigs:

                                                  ?
                                                    Seq[
                                                      ?
                                                        cask.router.ArgSig[Any,
                                                          ?
                                                        , ?, {} cask.Request]
                                                    ]

                                               = x19._1
                                              case val args:
                                                ? Map[? String, Any]
                                               = x19._2
                                              return[matchResult5]
                                                {
                                                  sigs.map[

                                                      Either[
                                                        Seq[
                                                          cask.router.Result.
                                                            ParamError
                                                        ]
                                                      , Any]

                                                  ](
                                                    {
                                                      def $anonfun(
                                                        x$1:

                                                            cask.router.ArgSig[
                                                              Any
                                                            , ?, ?, cask.Request
                                                              ]

                                                      ):

                                                          Either[
                                                            Seq[
                                                              cask.router.Result
                                                                .
                                                              ParamError
                                                            ]
                                                          , Any]

                                                       =
                                                        matchResult4[<notype>]:
                                                          {
                                                            case val x18:

                                                                (x$1 :
                                                                  cask.router.
                                                                    ArgSig
                                                                  [Any, ?, ?,
                                                                    cask.Request
                                                                  ]
                                                                )

                                                             = x$1
                                                            case val sig:

                                                                ?
                                                                  cask.router.
                                                                    ArgSig
                                                                  [Any, ?, ?,
                                                                    {}
                                                                      cask.
                                                                        Request
                                                                  ]

                                                             = x18
                                                            return[matchResult4]

                                                            {
                                                              cask.router.
                                                                Runtime
                                                              .makeReadCall[Any
                                                                ,
                                                              cask.Request](args
                                                                ,
                                                              ctx,
                                                                {
                                                                  def $anonfun()
                                                                    :
                                                                  Option[Any] =
                                                                    matchResult3
                                                                      [
                                                                    <notype>]:
                                                                      {
                                                                        case val

                                                                        x15:

                                                                            (sig
                                                                              .
                                                                            default
                                                                               :
                                                                            Option
                                                                              [
                                                                            sig.
                                                                              T
                                                                             =>
                                                                              sig
                                                                                .
                                                                              V
                                                                            ]
                                                                            )

                                                                         =
                                                                          sig.
                                                                            default
                                                                        if
                                                                          None.
                                                                            ==
                                                                          (x15)
                                                                         then
                                                                          return[
                                                                            matchResult3
                                                                          ]
                                                                            {
                                                                              None
                                                                            }
                                                                         else ()
                                                                        if
                                                                          x15.
                                                                            $isInstanceOf
                                                                          [

                                                                              Some
                                                                                [
                                                                              sig
                                                                                .
                                                                              T
                                                                                =>

                                                                                sig
                                                                                  .
                                                                                V
                                                                                ]

                                                                          ]
                                                                         then
                                                                          {
                                                                            case

                                                                            val
                                                                              getter
                                                                            :

                                                                                {
                                                                                  *
                                                                                }

                                                                                (
                                                                                  x$0
                                                                                :
                                                                                  {
                                                                                    }

                                                                                    sig
                                                                                      .
                                                                                    T
                                                                                  )
                                                                                ->

                                                                                sig
                                                                                  .
                                                                                V

                                                                             =
                                                                              x15
                                                                                .
                                                                              $asInstanceOf
                                                                                [

                                                                                Some
                                                                                  [
                                                                                sig
                                                                                  .
                                                                                T

                                                                                =>

                                                                                sig
                                                                                  .
                                                                                V
                                                                                  ]

                                                                              ].
                                                                                value
                                                                            return[
                                                                              matchResult3
                                                                            ]
                                                                              {
                                                                                val


                                                                                  value

                                                                                  :

                                                                                  Any

                                                                                   =
                                                                                getter
                                                                                  .
                                                                                asInstanceOf
                                                                                  [

                                                                                  (
                                                                                    Routes_this
                                                                                   :
                                                                                    (
                                                                                      TestWebSockets
                                                                                     :
                                                                                      TestWebSockets
                                                                                        .type
                                                                                    )
                                                                                  )

                                                                                  ->

                                                                                  Any

                                                                                  ]
                                                                                .
                                                                                  apply
                                                                                  (
                                                                                clazz
                                                                                  )
                                                                                Some
                                                                                  .
                                                                                apply
                                                                                  [

                                                                                  Any

                                                                                  ]
                                                                                (
                                                                                  value
                                                                                )
                                                                              }
                                                                          }
                                                                         else ()
                                                                        throw
                                                                          new

                                                                              MatchError

                                                                          (x15)
                                                                      }
                                                                  closure(
                                                                    $anonfun
                                                                  :
                                                                    () ?->
                                                                      Option[Any
                                                                        ]

                                                                  )
                                                                }
                                                              , sig)
                                                            }
                                                          }
                                                      closure($anonfun)
                                                    }
                                                  )
                                                }
                                            }
                                           else ()
                                          throw new MatchError(x19)
                                        }
                                    closure($anonfun)
                                  }
                                )
                              cask.router.Runtime.validateLists(parsedArgss).map
                                [
                              Any](
                                {
                                  {
                                    def $anonfun(validated: Seq[Seq[Any]]): Any
                                       =
                                    {
                                      val result: Any =
                                        TestWebSockets.showUserProfile(
                                          validated.apply(0).apply(0).
                                            asInstanceOf
                                          [String]
                                        )
                                      cask.internal.Conversion.create[
                                        ? cask.endpoints.WebsocketResult
                                      , ? cask.endpoints.WebsocketResult](
                                        $conforms[
                                          ? cask.endpoints.WebsocketResult
                                        ]
                                      ).asInstanceOf[
                                        cask.internal.Conversion[Any, Any]
                                      ].f.apply(result)
                                    }
                                    closure($anonfun)
                                  }
                                }
                              )
                            }
                          closure($anonfun)
                        }
                      )
                    cask.router.EndpointMetadata.apply[
                      (Routes_this : (TestWebSockets : TestWebSockets.type))
                    ](Nil,
                      new cask.websocket("/connect/:userName",
                        cask.endpoints.websocket.$lessinit$greater$default$2
                      )
                    , entrypoint)
                  }
                 :

                    cask.router.EndpointMetadata[
                      (Routes_this : (TestWebSockets : TestWebSockets.type))
                    ]

                ]
              )
            )
          {
            Routes_this.cask$main$Routes$$inline$metadata0_=(x$proxy1)
          }
        }
        ()
      }:Unit
    }
  }
}
```