package in.ashwanthkumar.pdf2txt

import java.io.InputStream
import java.util

import org.apache.commons.lang3.StringUtils
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.text.{PDFTextStripper, TextPosition}

import scala.collection.JavaConverters._
import scala.collection.mutable

case class Token(txt: String, positions: List[TextPosition]) {
  def height = positions.map(pos => pos.getHeight).max
  def right  = positions.map(pos => pos.getXDirAdj + pos.getWidth).max
  def bottom = positions.map(pos => pos.getYDirAdj + pos.getHeight).max
  def top    = positions.map(pos => pos.getYDirAdj).min
  def left   = positions.map(pos => pos.getXDirAdj).min
  def length = StringUtils.length(txt)

  def words = {
    case class State(positionsSoFar: List[TextPosition] = Nil, words: List[Token] = Nil) {
      def add(position: TextPosition) = this.copy(positionsSoFar = positionsSoFar ++ List(position))
      def endOfCurrentWord            = record.reset
      def record = {
        val nonEmptyPositions = removeEmptyPositions
        if (nonEmptyPositions.isEmpty) this
        else
          this.copy(
            words = words ++ List(
              Token(Normalizer.normalizeWord(nonEmptyPositions.map(_.getUnicode).mkString), nonEmptyPositions)
            )
          )
      }
      def reset = this.copy(
        positionsSoFar = Nil
      )
      def isNextLinePos(newPos: TextPosition) = {
        positionsSoFar.nonEmpty && (newPos.getXDirAdj < positionsSoFar.last.getXDirAdj)
      }
      def inferSpace(newPos: TextPosition) = {
        if (positionsSoFar.nonEmpty) {
          // if the new text position is positioned more than the average distance between the tokens so far then we infer it as a space
          val totalWidthSoFar  = positionsSoFar.map(_.getWidth).sum
          val avgWidthPerToken = totalWidthSoFar / positionsSoFar.length
          val last             = positionsSoFar.last
          val endX             = last.getXDirAdj + last.getWidth
          val diff             = newPos.getXDirAdj - endX
          diff > avgWidthPerToken
        } else {
          false
        }
      }
      def removeEmptyPositions =
        positionsSoFar.filter(pos => StringUtils.isNotBlank(StringUtils.trimToNull(pos.getUnicode)))
    }

    val finalState = positions
      .foldLeft(State()) {
        case (state, pos) =>
          if (StringUtils.isWhitespace(pos.getUnicode) || state.isNextLinePos(pos) || state.inferSpace(pos))
            state.endOfCurrentWord.add(pos)
          else state.add(pos)
      }
      .endOfCurrentWord

    finalState.words
  }

  override def toString: String = {
    s"Token: $txt, Length: $length, Height: $height, Right: $right, Bottom: $bottom, Top: $top, Left: $left"
  }
}

object Token {
  def apply(position: TextPosition) = new Token(position.getUnicode, List(position))
}

case class Line(lineNumber: Int, tokens: List[Token]) {
  def addToken(token: Token): Line                               = this.copy(tokens = tokens ++ List(token))
  def addTokens(tokens: List[Token]): Line                       = this.copy(tokens = this.tokens ++ tokens)
  def addToken(txt: String, positions: List[TextPosition]): Line = addToken(Token(txt, positions))

  def length   = StringUtils.length(txt)
  def maxRight = tokens.map(_.right).max
  def minLeft  = tokens.map(_.left).min
  def txt      = StringUtils.trimToEmpty(tokens.map(_.txt).mkString(" "))
}

// NOT THREAD SAFE, DO NOT USE ACROSS PDDocument instances
class TextStripperWithPositions extends PDFTextStripper {
  val _output      = new StringBuffer()
  var currentLine  = 1
  var lineToTokens = mutable.Map[Int, Line]()

  var textPositions = mutable.MutableList[TextPosition]()

  override def writeLineSeparator(): Unit = {
    currentLine = currentLine + 1
    super.writeLineSeparator()
  }

  override def endPage(page: PDPage): Unit = {
    // process all the collected tokens in here
    val lineToken = Token(s"Line $currentLine", textPositions.toList)
    val words     = lineToken.words

    case class State(
        lastLeft: Float = 0.0f,
        lastRight: Float = 0.0f,
        current: List[Token] = Nil,
        currentPage: Int = 1,
        linesToTokens: Map[Int, Line] = Map()
    ) {
      def ++(token: Token) = {
        val lastUpdates = this.copy(
          lastLeft = token.left,
          lastRight = token.right
        )
        if (token.left < lastLeft || token.right < lastRight) {
          // we're probably in the next line since the tokens are going back to the left
          lastUpdates.copy(
            currentPage = currentPage + 1,
            linesToTokens = linesToTokens ++ Map(
              currentPage -> linesToTokens.getOrElse(currentPage, Line(currentPage, Nil)).addTokens(current)
            ),
            current = List(token)
          )
        }
        // TODO(ashwanthkumar): While we check for position across the X-axis, we don't check the position across Y-axis today,
        // so a token that should ideally be part of the previous line or one before that might be aligned properly
        else {
          lastUpdates.copy(
            current = current ++ List(token)
          )
        }
      }

      def markEnd = {
        this.copy(
          linesToTokens = linesToTokens ++ Map(
            currentPage -> linesToTokens.getOrElse(currentPage, Line(currentPage, Nil)).addTokens(current)
          )
        )
      }
    }
    val state = words.foldLeft(State())(_ ++ _).markEnd

    state.linesToTokens.foreach {
      case (key, value) =>
        lineToTokens(key) = value
    }
  }

  // called once per line. line as decided by pdfbox.
  override def writeString(text: String, textPositions: util.List[TextPosition]): Unit = {
    this.textPositions ++= textPositions.asScala.toList
  }

  def tokensByLine = lineToTokens

  def totalLines = lineToTokens.size
}

object TextStripperWithPositions {
  def readFile(input: InputStream) = {
    val parser = new PDFParser(new RandomAccessBufferedFileInputStream(input))
    parser.parse()
    parser.getPDDocument
  }
}
