package in.ashwanthkumar.htmldiff

import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

import scala.io.Source

class Html2TxtTest extends FlatSpec {
  "HtmlDiff" should "get the main content using aria convention" in {
    val sampleHtml =
      """
        |<html>
        |<body>
        |Content above Main.
        |<div role="main">
        |Main Content
        |</div>
        |Content below Main.
        |</body>
        |</html>
        |""".stripMargin
    val diff = Html2Txt(sampleHtml)
    diff.toTxt should be("Main Content")
  }

  it should "fall back to getting the whole body if main content by aria convention is not found" in {
    val sampleHtml =
      """
        |<html>
        |<body>
        |Content above Main.
        |<div>
        |Main Content
        |</div>
        |Content below Main.
        |</body>
        |</html>
        |""".stripMargin
    val expected =
      """Content above Main.
        |Main Content
        |Content below Main.""".stripMargin

    val diff = Html2Txt(sampleHtml)
    diff.toTxt should be(expected)
  }

  it should "convert the own text of the element as well" in {
    val sampleHtml =
      """
        |<p>Hello <b>World</b></p>
        |""".stripMargin
    val diff = Html2Txt(sampleHtml)
    diff.toTxt should be("Hello World")
  }

  it should "convert an <img> to a placeholder on text" in {
    val sampleHtml =
      """
        |<body><img src=".../foo.jpg" /></body>
        |""".stripMargin
    val diff = Html2Txt(sampleHtml)
    diff.toTxt should be("[AVALARA_IMAGE_STRIPPED]")
  }

  it should "convert the input to txt" in {
    val input                = getClass.getResourceAsStream("/html/wiki_latest.html")
    val url                  = "https://en.wikipedia.org/wiki/Steve_Wozniak"
    val expectedOutputStream = getClass.getResourceAsStream("/html/wiki_latest.txt")
    val expectedOutput       = Source.fromInputStream(expectedOutputStream).getLines().mkString("\n")

    val diff = Html2Txt(input, url)
    diff.toTxt should be(expectedOutput)
  }

  it should "convert the toc section with just <ul><li> to txt" in {
    val input                = getClass.getResourceAsStream("/html/wiki_toc_section.html")
    val url                  = "https://en.wikipedia.org/wiki/Steve_Wozniak"
    val expectedOutputStream = getClass.getResourceAsStream("/html/wiki_toc_section.txt")
    val expectedOutput       = Source.fromInputStream(expectedOutputStream).getLines().mkString("\n")

    val diff = Html2Txt(input, url)
    diff.toTxt should be(expectedOutput)
  }
}
