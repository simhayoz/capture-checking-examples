// package server

// import server.websocket.WebSocketFrame

// import java.util.concurrent.ConcurrentLinkedQueue
// import annotation.capability

// @capability class HttpRoutes(val queueCapability: {*} ConcurrentLinkedQueue[WebSocketFrame], val pf: PartialFunction[Request, {queueCapability} Response]) {
//   /**
//    * Add default not found case for partial function
//    *
//    * @return the HttpRoutes
//    */
//   def orNotFound: HttpRoutes = { // Only works with ()
//     HttpRoutes.of(this.queueCapability,
//       this.pf.orElse({
//         case r => NotFound(f"Not Found: ${r.uri}")
//       }
//     ))
//   }
// }

// object HttpRoutes {
//   /**
//    * Update the HttpRoutes from a partial function
//    *
//    * @param pf a partial function from request to response
//    * @return the HttpRoutes
//    */
//   def of(queueCapability: {*} ConcurrentLinkedQueue[WebSocketFrame], pf: PartialFunction[Request, {queueCapability} Response]): HttpRoutes =
//     HttpRoutes(queueCapability, pf)
// }