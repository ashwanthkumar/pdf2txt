package in.ashwanthkumar.pdf2txt

import org.apache.pdfbox.pdmodel.PDDocument
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class TextStripperWithPositionsTest extends FlatSpec {
  "TextStripperWithPositions" should "process 3 colum layout without any lines separating them" in {
    val pdfDocument =
      TextStripperWithPositions.readFile(getClass.getResourceAsStream("/pdfs/3_column_layout_with_header_footer.pdf"))
    pdf2txt(pdfDocument)
  }

  it should "process 4 column layout with lines separating them" in {
    val pdfDocument =
      TextStripperWithPositions.readFile(
        getClass.getResourceAsStream("/pdfs/4_column_layout_with_markers_header_footer.pdf")
      )
    pdf2txt(pdfDocument)
  }

  it should "process page wide tables" in {
    val pdfDocument =
      TextStripperWithPositions.readFile(getClass.getResourceAsStream("/pdfs/large_table_with_header.pdf"))
    pdf2txt(pdfDocument)
  }

  def pdf2txt(pdfDocument: PDDocument): Unit = {
    val stripper = new TextStripperWithPositions()
    stripper.setSortByPosition(true)
    stripper.getText(pdfDocument)
    println(stripper.totalLines)

    pdfDocument.close()

    println(s"Total Lines: ${stripper.totalLines}")
    val lines = stripper.lineToTokens.values.toList.sortBy(_.lineNumber)

    val maxTxtLengthLine = lines.maxBy(_.length)
    println("Max Txt Length Line")
    println(maxTxtLengthLine)
    println(s"Max Length: ${maxTxtLengthLine.length}, Token: ${maxTxtLengthLine.txt}")
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val leftMostLine = lines.minBy(_.minLeft)
    println("Left Most Line")
    println(leftMostLine)
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val rightMostLine = lines.maxBy(_.maxRight)
    println("Right Most Line")
    println(rightMostLine)
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val leftPaddingToLineWithMostText = math.abs(leftMostLine.minLeft - maxTxtLengthLine.minLeft)
    println(s"leftPaddingToLineWithMostText: $leftPaddingToLineWithMostText")
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val spacingRatio = ((rightMostLine.maxRight + leftPaddingToLineWithMostText) / maxTxtLengthLine.maxRight) * 1.05f //* 1.5f
    println(s"Spacing Ratio: $spacingRatio")
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val maxTxtLengthInOutput = math.round(maxTxtLengthLine.length * spacingRatio)
    println(s"Max Text Length in page: $maxTxtLengthInOutput")
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val documentMatrix = Array.fill(lines.length, maxTxtLengthInOutput * 2)(' ')

    case class State(lastWroteIndex: Int = -1)
    lines.foreach { line =>
      var lastWroteIndex = -1
      line.tokens
        .flatMap(_.words)
        .foreach { token =>
          val diffInLeftFromLeftMost       = math.abs(token.left - leftMostLine.minLeft)
          val ratioOfDiffAcrossLargestLine = diffInLeftFromLeftMost / rightMostLine.maxRight
          var columnForTheToken            = math.round(maxTxtLengthInOutput * ratioOfDiffAcrossLargestLine)
          if (columnForTheToken <= lastWroteIndex) {
            columnForTheToken = lastWroteIndex + 1
          }
          lastWroteIndex = columnForTheToken + token.length
//          println(
//            s"Line: ${line.lineNumber}, Token: ${token.txt}, Height: ${token.height}, Left: ${token.left}, Right: ${token.right}, Top: ${token.top}, Bottom: ${token.bottom}, ratioOfDiffAcrossLargestLine: ${ratioOfDiffAcrossLargestLine}, diffInLeftFromLeftMost: ${diffInLeftFromLeftMost}, Column in O/P: ${columnForTheToken}, lastWroteIndex: $lastWroteIndex"
//          )
          token.txt.toCharArray.zipWithIndex.foreach {
            case (c, index) =>
              documentMatrix(line.lineNumber - 1)(columnForTheToken + index) = c
          }
        }
      println(documentMatrix(line.lineNumber - 1).mkString)
    }
  }

}
