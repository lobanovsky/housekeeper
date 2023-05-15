package ru.housekeeper.docs

import org.apache.poi.xwpf.usermodel.BreakType
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream

@Service
class DocFileService {

    private val breakLine = "Важно отметить, что собственником средств на Спецсчете остаются собственники помещений в МКД. Владелец Спецсчета осуществляет только управление Спецсчетом по поручению собственников."

    fun doIt(rootPath:String, lines: List<String>, path: String, fileName: String) {

        val document = XWPFDocument()
        val paragraph = document.createParagraph()
        val run = paragraph.createRun()
        for (line in lines) {
            run.setText(line)
            run.addBreak()
            if (line == breakLine) {
                run.addBreak(BreakType.PAGE)
            }
        }
        run.fontSize = 8
        run.fontFamily = "Times New Roman"
        val fos = FileOutputStream(File("$rootPath${path}/${fileName}.docx"))
        document.write(fos)
        fos.close()

//        val document = XWPFDocument()
//        //title
//        val title = document.createParagraph()
//        title.alignment = ParagraphAlignment.CENTER
//        val titleRun = title.createRun()
//        titleRun.isBold = true
//        titleRun.setText(lines[0])
//        titleRun.fontSize = 8
//        titleRun.fontFamily = "Times New Roman"
//        //body
//        val body =document.createParagraph()
//        body.alignment = ParagraphAlignment.LEFT
//        val bodyRun = body.createRun()
//        for (line in lines.drop(1)) {
//            bodyRun.setText(line)
//            bodyRun.addBreak()
//        }
//        bodyRun.fontSize = 8
//        bodyRun.fontFamily = "Times New Roman"
//        val fos = FileOutputStream(File("$rootPath${path}/${fileName}.docx"))
//        document.write(fos)
//        fos.close()
    }
}