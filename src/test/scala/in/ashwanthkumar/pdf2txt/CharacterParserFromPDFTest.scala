package in.ashwanthkumar.pdf2txt

import org.apache.pdfbox.pdmodel.PDDocument
import org.scalatest.FlatSpec

class CharacterParserFromPDFTest extends FlatSpec {
  "TextStripperWithPositions" should "process 3 colum layout without any lines separating them" in {
    val pdfDocument =
      CharacterParserFromPDF.readFile(getClass.getResourceAsStream("/pdfs/3_column_layout_with_header_footer.pdf"))
    val document = pdf2txt(pdfDocument)
    println(document)
  }

  it should "process 4 column layout with lines separating them" in {
    val pdfDocument =
      CharacterParserFromPDF.readFile(
        getClass.getResourceAsStream("/pdfs/4_column_layout_with_markers_header_footer.pdf")
      )
    val document = pdf2txt(pdfDocument)
    println(document)
  }

  it should "process page wide tables" in {
    val pdfDocument =
      CharacterParserFromPDF.readFile(getClass.getResourceAsStream("/pdfs/large_table_with_header.pdf"))
    val document = pdf2txt(pdfDocument)
    println(document)
  }

  it should "process text with bullets" in {
    val pdfDocument = CharacterParserFromPDF.readFile(getClass.getResourceAsStream("/pdfs/text_with_bullets.pdf"))
    val document    = pdf2txt(pdfDocument)
    println(document)

  }

  def pdf2txt(pdfDocument: PDDocument): String = {
    new PDF2Txt(pdfDocument).toText
  }

}
