package com.cd.llmplugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Panel
import java.awt.event.ActionEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.security.KeyStore
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LlmToolWindowFactory: ToolWindowFactory {
    private val chatArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()

        val panel = JPanel(BorderLayout())

        val inputField = createInputField()

        val inputScrollPanel = JBScrollPane(inputField).apply {
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        }
        val inputPanelWrapper = JPanel(BorderLayout()).apply {
            add(inputScrollPanel, BorderLayout.CENTER)
            preferredSize = Dimension(0, 60)
            maximumSize = Dimension(Int.MAX_VALUE, 120)
        }

        panel.add(inputPanelWrapper, BorderLayout.SOUTH)

        val scrollPanel = JBScrollPane(chatArea)
        panel.add(scrollPanel, BorderLayout.CENTER)

        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun appendChat(message: String) {
        chatArea.append("$message\n")
        chatArea.caretPosition = chatArea.document.length
    }

    private fun respondWithTimestamp() {
        appendChat("Bot: ${System.currentTimeMillis()}")
    }

    private fun createInputField(): JTextArea {
        val inputField = JTextArea().apply {
            lineWrap = true
            wrapStyleWord = true
            rows = 3

            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = scrollToBottom()

                override fun removeUpdate(e: DocumentEvent?) = scrollToBottom()

                override fun changedUpdate(e: DocumentEvent?) = scrollToBottom()

                private fun scrollToBottom() {
                    SwingUtilities.invokeLater {
                        caretPosition = document.length
                    }
                }
            })

            val inputMap = getInputMap(JComponent.WHEN_FOCUSED)

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendMessage")
            actionMap.put("sendMessage", object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent?) {
                    val userText = text.trim()
                    if (userText.isNotEmpty()) {
                        appendChat("You: $userText")
                        respondWithTimestamp()
                        text = ""
                    }
                }
            })

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "insertNewLine")
            actionMap.put("insertNewLine", object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent?) {
                    insert("\n", caretPosition)
                }
            })

        }

        return inputField
    }
}