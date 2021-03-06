import org.jsoup._
import collection.JavaConverters._
import java.io.IOException

object ScrapingDocsThrowsWithCC {
  import language.experimental.saferExceptions

  import colltest5.strawman.collections.*
  import CollectionStrawMan5.*

  def fromMutableBuffer[A](buf: scala.collection.mutable.Buffer[A]): List[A] =
    val l: scala.collection.Iterator[A] = buf.iterator
    var newList = List[A]()
    while (l.hasNext) {
      newList = Cons(l.next(), newList)
    }
    newList

  def javaExceptionWrapper[A, E <: Exception](f: Unit => A): {f} A throws E =
    try {
      f(())
    } catch {case (e: E) => throw e}

  def main(args: Array[String]): Unit = {
    val indexDoc: nodes.Document = Jsoup.connect("https://developer.mozilla.org/en-US/docs/Web/API").get()
    val links: List[nodes.Element] = fromMutableBuffer(indexDoc.select("h2#interfaces").nextAll.select("div.index a").asScala)
    val linkData: List[(String, String, String)] = links.map(link => (link.attr("href"), link.attr("title"), link.text))
    val closures: List[Unit => (String, String, String, String, List[(String, String)]) throws IOException] =
      linkData.map{case (url, tooltip, name) => _ => {
        println("Scraping " + name)
        val doc = javaExceptionWrapper(_ => Jsoup.connect("https://developer.mozilla.org" + url).get())
        val summary = doc.select("article#wikiArticle > p").asScala.headOption match {
          case Some(n) => n.text; case None => ""
        }
        val methodsAndProperties = fromMutableBuffer(doc
        .select("article#wikiArticle dl dt")
        .asScala)
        .map(el => (el.text, el.nextElementSibling match {case null => ""; case x => x.text}))
        (url, tooltip, name, summary, methodsAndProperties)
      }}
    try {
    for (closure <- closures) closure(())
    } catch {case (m: IOException) => println("Closure failed")}
  }
}
