package endpoints.scalaj.client

import endpoints.algebra
import endpoints.algebra.Documentation

import scala.concurrent.{ExecutionContext, Future}

/**
  * @group interpreters
  */
trait Endpoints extends algebra.Endpoints with Requests with Responses {

  def endpoint[A, B](
    request: Request[A],
    response: Response[B],
    summary: Documentation,
    description: Documentation,
    tags: List[String]
  ): Endpoint[A, B] = {
    Endpoint(request, response)
  }

  case class Endpoint[Req, Resp](request: Request[Req], response: Response[Resp]) {

    /**
      * This method just wraps a call in a Future and is not real async call
      */
    def callAsync(args: Req)(implicit ec: ExecutionContext): Future[Resp] =
      Future {
        concurrent.blocking {
          callUnsafe(args)
        }
      }

    def callUnsafe(args: Req): Resp =
      call(args) match {
        case Left(ex) => throw ex
        case Right(x) => x
      }

    def call(args: Req): Either[Throwable, Resp] = {
      val resp = request(args).asString
      response(resp).toRight(new Throwable(s"Unexpected response status: ${resp.code}"))
        .right.flatMap(_(resp.body))
    }
  }

}
