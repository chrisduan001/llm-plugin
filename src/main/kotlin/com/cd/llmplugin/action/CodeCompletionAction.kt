package com.cd.llmplugin.action

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class CodeCompletionAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor = e.getData(com.intellij.openapi.actionSystem.PlatformDataKeys.EDITOR) ?: return

        showCompletion(editor)
    }

    private fun showCompletion(editor: Editor) {
        val lookup = LookupManager.getInstance(editor.project!!).showLookup(
            editor,
            LookupElementBuilder.create("Hello World").withPresentableText("Hello world")
        )
        (lookup as LookupImpl).refreshUi(true, true)
    }
}