package com.cd.llmplugin.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayProperties
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import kotlin.concurrent.schedule

class LlmEditorListener: EditorFactoryListener {
    private val editorInlays = WeakHashMap<Editor, Inlay<*>?>()

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val timer = Timer()
        val project = editor.project ?: return

        editor.document.addDocumentListener(object : DocumentListener {
            private var delayedTask: TimerTask? = null

            override fun documentChanged(event: DocumentEvent) {
                delayedTask?.cancel()
                removeExistingInlay(editor)

                delayedTask = timer.schedule(500) {
                    val currentFileContent = editor.document.text
                    val completionSuggestion = provideCompletionData(currentFileContent)

                    ApplicationManager.getApplication().invokeLater {
                        val offset = editor.caretModel.offset
                        val renderer = CustomRenderer(completionSuggestion)
                        val inlay = editor.inlayModel.addInlineElement(offset, renderer)
                        editorInlays[editor] = inlay
                    }
                }
            }
        })

        editor.contentComponent.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_TAB) {
                    val inlay = editorInlays[editor]
                    if (inlay != null) {
                        confirmInlay(editor, inlay, project)
                        e.consume()
                    }
                }
            }
        })
    }


    private fun confirmInlay(editor: Editor, inlay: Inlay<*>, project: Project) {
        val suggestion = (inlay.renderer as CustomRenderer).suggestion
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(inlay.offset, suggestion)
            editor.caretModel.moveToOffset(inlay.offset + suggestion.length)
        }
        inlay.dispose()
        editorInlays.remove(editor)
    }

    private fun removeExistingInlay(editor: Editor) {
        editorInlays[editor]?.dispose()
        editorInlays.remove(editor)
    }

    private fun provideCompletionData(content: String): String {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return "\n// No content available"

        val randomLines = lines.shuffled().take(10)
        return "\n// Suggested: \n" + randomLines.joinToString(separator = "\n")
    }

    companion object {
        private val editorInlays = WeakHashMap<Editor, Inlay<*>>()

        fun confirmExistingInlay(editor: Editor, project: Project) {

        }
    }
}