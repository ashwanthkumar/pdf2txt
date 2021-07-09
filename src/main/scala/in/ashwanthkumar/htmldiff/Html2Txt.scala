package in.ashwanthkumar.htmldiff

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.trimToEmpty
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Node, TextNode}
import org.jsoup.select.Elements

import java.io.InputStream
import scala.collection.JavaConverters._

class Html2Txt(document: Document) {
  private val NEWLINE     = "\n"
  private val EMPTY       = ""
  private val WHITE_SPACE = " "
  private val TAB         = "\t"
  private val blockTags   = List("div", "ul", "li", "tr", "td", "th", "br", "img", "h1", "h2", "h3", "h4", "h5", "h6")

  // ARIA Main role - https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Roles/Main_role
  def mainArea: Elements = {
    val es = document.select("[role=main]")
    if (es.isEmpty) {
      document.select("body")
    } else {
      es
    }
  }

  def trimUntilEmpty(text: String): String = {
    val trimmedText = trimToEmpty(text)
    // if there was a space, we would like to preserve it and not trim it away.
    if (StringUtils.isEmpty(trimmedText)) WHITE_SPACE
    else trimmedText
  }

  def processElement(element: Node): String = {
    element match {
      case txt: TextNode                                  => txt.text()
      case _ if element.nodeName().toLowerCase() == "img" => "[AVALARA_IMAGE_STRIPPED]".concat(NEWLINE)
      case _                                              => processContainer(element)
    }
  }

  def processContainer(node: Node): String = {
    val tagName           = node.nodeName().toLowerCase
    val blockWrapperStart = if (blockTags.contains(tagName)) NEWLINE else EMPTY
    val blockWrapperEnd   = if (blockTags.contains(tagName)) NEWLINE else EMPTY
    val text = node
      .childNodes()
      .asScala
      .map(processElement)
      .map(s => s.replaceAll(s"$WHITE_SPACE+", WHITE_SPACE))
      .mkString

    s"${blockWrapperStart}${text}${blockWrapperEnd}"
  }

  def toTxt: String = {
    val text = mainArea.asScala
      .map(processContainer)
      .mkString

    // strip off multiple empty lines or lines with just a blank space
    text
      .split(NEWLINE)
      .map(StringUtils.trimToEmpty)
      .filterNot(StringUtils.isWhitespace)
      .mkString(NEWLINE)
  }
}

object Html2Txt {
  def apply(in: InputStream, url: String): Html2Txt = {
    new Html2Txt(Jsoup.parse(in, "UTF-8", url).normalise())
  }

  def apply(html: String): Html2Txt = {
    new Html2Txt(Jsoup.parse(html))
  }
}
