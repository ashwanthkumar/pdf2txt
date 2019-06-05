package in.ashwanthkumar.pdf2txt

import org.apache.pdfbox.pdmodel.PDDocument

class PDF2Txt(pdfDocument: PDDocument, extraSpacingRatio: Float = 1.05f) {
  def toText: String = {
    val stripper = new TextStripperWithPositions()
    stripper.setSortByPosition(true)
    stripper.getText(pdfDocument)
    //    println(stripper.totalLines)

    pdfDocument.close()

    //    println(s"Total Lines: ${stripper.totalLines}")
    val lines = stripper.lineToTokens.values.toList.sortBy(_.lineNumber)

    val maxTxtLengthLine = lines.maxBy(_.length)
    //    println("Max Txt Length Line")
    //    println(maxTxtLengthLine)
    //    println(s"Max Length: ${maxTxtLengthLine.length}, Token: ${maxTxtLengthLine.txt}")
    //    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val leftMostLine = lines.minBy(_.minLeft)
    //    println("Left Most Line")
    //    println(leftMostLine)
    //    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val rightMostLine = lines.maxBy(_.maxRight)
    //    println("Right Most Line")
    //    println(rightMostLine)
    //    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val leftPaddingToLineWithMostText = math.abs(leftMostLine.minLeft - maxTxtLengthLine.minLeft)
    //    println(s"leftPaddingToLineWithMostText: $leftPaddingToLineWithMostText")
    //    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    // add an extra 5% spacing to keep the content well layout
    val spacingRatio = ((rightMostLine.maxRight + leftPaddingToLineWithMostText) / maxTxtLengthLine.maxRight) * extraSpacingRatio
    //    println(s"Spacing Ratio: $spacingRatio")
    //    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val maxTxtLengthInOutput = math.round(maxTxtLengthLine.length * spacingRatio)
    //    println(s"Max Text Length in page: $maxTxtLengthInOutput")
    //    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

    val documentMatrix = Array.fill(lines.length, maxTxtLengthInOutput * 2)(' ')
    val stringBuilder  = new StringBuilder

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
      stringBuilder
        .append(documentMatrix(line.lineNumber - 1).mkString)
        .append('\n')
    }

    stringBuilder.toString()
  }
}
