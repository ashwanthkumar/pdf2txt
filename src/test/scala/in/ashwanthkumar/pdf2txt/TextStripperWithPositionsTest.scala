package in.ashwanthkumar.pdf2txt

import org.scalatest.FlatSpec
import scala.collection.JavaConverters._

class TextStripperWithPositionsTest extends FlatSpec {
  "TextStripperWithPositions" should "capture the current line that we're writing" in {
    val pdfDocument =
      TextStripperWithPositions.readFile(getClass.getResourceAsStream("/pdfs/3_column_layout_with_header_footer.pdf"))

    val page = pdfDocument.getPages.get(0)

    val stripper = new TextStripperWithPositions()
    stripper.setSortByPosition(true)
    val docTxt = stripper.getText(pdfDocument)
//    println(docTxt)
    println(stripper.totalLines)

    pdfDocument.close()

    println(s"Total Lines: ${stripper.totalLines}")
    val lines = stripper.lineToTokens.values.toList.sortBy(_.lineNumber)
//    lines.flatMap(line => line.tokens.map(t => (line.lineNumber, t))).foreach(println)

    val maxTxtLengthLine = lines.maxBy(_.length)
    println("Max Txt Length Line")
    println(maxTxtLengthLine)
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val leftMostLine = lines.minBy(_.minLeft)
    println("Left Most Line")
    println(leftMostLine)
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val rightMostLine = lines.maxBy(_.maxRight)
    println("Right Most Line")
    println(rightMostLine)
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val spacingRatioByLeft = leftMostLine.maxRight / maxTxtLengthLine.maxRight
    println(s"Spacing Ratio By Left: $spacingRatioByLeft")
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val spacingRatioByRight = rightMostLine.maxRight / maxTxtLengthLine.maxRight
    println(s"Spacing Ratio By Right: $spacingRatioByRight")
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val spacingRatio         = math.max(spacingRatioByLeft, spacingRatioByRight)
    val maxTxtLengthInOutput = math.round(maxTxtLengthLine.length * spacingRatio)
    println(s"Max Text Length in page: $maxTxtLengthInOutput")
    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val documentMatrix = Array.fill(lines.length, maxTxtLengthInOutput * 2)(' ')

    lines.foreach { line =>
//      var roundOffOffsetForTheLine = 0

      line.tokens
        .flatMap(_.words)
        .foreach { token =>
          val diffInLeftFromLeftMost       = math.abs(token.left - leftMostLine.minLeft)
          val ratioOfDiffAcrossLargestLine = diffInLeftFromLeftMost / rightMostLine.maxRight
          val columnForTheToken            = math.round(maxTxtLengthInOutput * ratioOfDiffAcrossLargestLine) + 1
//          println(
//            s"Line: ${line.lineNumber}, Token: ${token.txt}, Height: ${token.height}, Left: ${token.left}, Right: ${token.right}, Top: ${token.top}, Bottom: ${token.bottom}, Column in O/P: ${columnForTheToken}"
//          )
          token.txt.toCharArray.zipWithIndex.foreach {
            case (c, index) =>
//              if (' ' == documentMatrix(line.lineNumber - 1)(columnForTheToken + index)) {
              documentMatrix(line.lineNumber - 1)(columnForTheToken + index) = c
//              } else {
//                documentMatrix(line.lineNumber - 1)(columnForTheToken + index + roundOffOffsetForTheLine) = c
//                roundOffOffsetForTheLine = roundOffOffsetForTheLine + 1
//              }
          }
//          documentMatrix(line.lineNumber - 1).update(columnForTheToken, token.txt.toCharArray)
        }
//      println(s"Left: ${line.minLeft}, Length: ${line.length}, Txt: <See Below>")
      println(documentMatrix(line.lineNumber - 1).mkString)
    }
  }

}
