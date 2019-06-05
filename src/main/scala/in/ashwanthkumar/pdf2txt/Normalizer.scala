package in.ashwanthkumar.pdf2txt

import org.apache.commons.lang3.StringUtils

import scala.io.Source

object Normalizer {
  private val unicodeReplacements = {
    Source
      .fromInputStream(classOf[Normalizer2].getResourceAsStream("/unicodes.txt"))
      .getLines()
      .map { line =>
        val Array(before, after) = line.split(";", 2)
        before -> after
      }
      .toMap
  }

  def normalizeUnicode(input: String): String = {
    unicodeReplacements.foldLeft(input) {
      case (sofar, (search, replacement)) =>
        StringUtils.replace(sofar, search, replacement)
    }
  }

}
