package ru.tsn.housekeeper.docs

import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class SimpleDocFileService {

    fun doIt(lines: List<String>): ByteArrayOutputStream {

        val document = XWPFDocument()
        //title
        val title = document.createParagraph()
        title.alignment = ParagraphAlignment.CENTER
        val titleRun = title.createRun()
        titleRun.isBold = true
        titleRun.setText(lines[0])
        titleRun.fontSize = 8
        titleRun.fontFamily = "Times New Roman"
        //body
        val body = document.createParagraph()
        body.alignment = ParagraphAlignment.LEFT
        val bodyRun = body.createRun()
        for (line in lines.drop(1)) {
            bodyRun.setText(line)
            bodyRun.addBreak()
        }
        bodyRun.fontSize = 8
        bodyRun.fontFamily = "Times New Roman"
        val baos = ByteArrayOutputStream()
        document.write(baos)
        baos.close()
        return baos
    }
}