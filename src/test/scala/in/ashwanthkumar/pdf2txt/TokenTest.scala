package in.ashwanthkumar.pdf2txt

import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.text.TextPosition
import org.apache.pdfbox.util.Matrix
import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper, empty, have}
import org.mockito.Mockito._

class TokenTest extends FlatSpec {
  "Token.State" should "record and reset the state on endOfCurrentWord" in {
    val txtPosition                = mockTextPosition("Hello")
    val state                      = Token.State(Nil, Nil)
    val stateAfterEndOfCurrentWord = state.add(txtPosition).endOfCurrentWord
    stateAfterEndOfCurrentWord.positionsSoFar should have size 0
    stateAfterEndOfCurrentWord.words should have size 1
  }

  it should ""

  def mockTextPosition(txt: String): TextPosition = {
    new TextPosition(
      1,
      0.0f,
      0.0f,
      new Matrix(),
      0.0f,
      0.0f,
      0.0f,
      0.0f,
      0.0f,
      txt,
      Array(),
      mock(classOf[PDFont]),
      0.0f,
      0
    )
  }
}
