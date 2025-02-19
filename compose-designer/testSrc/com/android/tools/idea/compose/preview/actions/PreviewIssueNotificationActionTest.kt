/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.compose.preview.actions

import com.android.tools.adtui.compose.ComposeStatus
import com.android.tools.adtui.compose.InformationPopup
import com.android.tools.adtui.swing.findAllDescendants
import com.android.tools.idea.compose.preview.COMPOSE_PREVIEW_MANAGER
import com.android.tools.idea.compose.preview.ComposePreviewManager
import com.android.tools.idea.compose.preview.TestComposePreviewManager
import com.android.tools.idea.editors.fast.DisableReason
import com.android.tools.idea.editors.fast.FastPreviewManager
import com.android.tools.idea.editors.fast.ManualDisabledReason
import com.android.tools.idea.editors.fast.fastPreviewManager
import com.android.tools.idea.testing.AndroidProjectRule
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.testFramework.MapDataContext
import com.intellij.testFramework.TestActionEvent
import com.intellij.ui.components.ActionLink
import com.intellij.xml.util.XmlStringUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/** Use this method when [PreviewIssueNotificationAction] should not create a popup. */
@Suppress("UNUSED_PARAMETER")
private fun noPopupFactor(project: Project, dataContext: DataContext): InformationPopup =
  throw IllegalStateException("Unexpected popup created")

internal class PreviewIssueNotificationActionTest {
  @get:Rule val projectRule = AndroidProjectRule.inMemory()

  private val composePreviewManager = TestComposePreviewManager()

  // DataContext is lazy so we give projectRule time to initialize itself.
  private val context by lazy {
    MapDataContext().also {
      it.put(COMPOSE_PREVIEW_MANAGER, composePreviewManager)
      it.put(CommonDataKeys.PROJECT, projectRule.project)
    }
  }

  private val originStatus =
    ComposePreviewManager.Status(
      hasRuntimeErrors = false,
      hasSyntaxErrors = false,
      isOutOfDate = false,
      areResourcesOutOfDate = false,
      isRefreshing = false,
      interactiveMode = ComposePreviewManager.InteractiveMode.DISABLED
    )

  @Test
  fun `check simple states`() {
    val action = PreviewIssueNotificationAction(projectRule.testRootDisposable, ::noPopupFactor)
    val event = TestActionEvent.createTestEvent(context)

    action.update(event)
    assertEquals("Up-to-date (The preview is up to date)", event.presentation.asString())

    composePreviewManager.currentStatus = originStatus.copy(hasRuntimeErrors = true)
    action.update(event)
    assertEquals(
      "Render Issues (Some problems were found while rendering the preview)",
      event.presentation.asString()
    )

    composePreviewManager.currentStatus = originStatus.copy(isOutOfDate = true)
    action.update(event)
    assertEquals("Out of date (The preview is out of date)", event.presentation.asString())
    try {
      FastPreviewManager.getInstance(projectRule.project).disable(ManualDisabledReason)
      action.update(event)
      assertEquals("Out of date (The preview is out of date)", event.presentation.asString())
    } finally {
      FastPreviewManager.getInstance(projectRule.project).enable()
    }

    composePreviewManager.currentStatus = originStatus.copy(hasSyntaxErrors = true)
    action.update(event)
    assertEquals(
      "Paused (The preview will not update while your project contains syntax errors.)",
      event.presentation.asString()
    )

    composePreviewManager.currentStatus = originStatus.copy(isRefreshing = true)
    action.update(event)
    assertEquals("Loading... (The preview is updating...)", event.presentation.asString())

    composePreviewManager.currentStatus = originStatus.copy(hasRuntimeErrors = true)
    action.update(event)
    val statusInfo = getStatusInfo(projectRule.project, context)!!
    assertTrue(statusInfo.hasRefreshIcon)
    assertEquals(ComposeStatus.Presentation.Warning, statusInfo.presentation)
    assertEquals(
      "Render Issues (Some problems were found while rendering the preview)",
      event.presentation.asString()
    )
  }

  @Test
  fun `check state priorities`() {
    val action = PreviewIssueNotificationAction(projectRule.testRootDisposable, ::noPopupFactor)
    val event = TestActionEvent.createTestEvent(context)

    composePreviewManager.currentStatus =
      originStatus.copy(hasSyntaxErrors = true, hasRuntimeErrors = true, isOutOfDate = true)
    action.update(event)
    // Syntax errors take precedence over out of date when Fast Preview is Enabled
    assertEquals(
      "Paused (The preview will not update while your project contains syntax errors.)",
      event.presentation.asString()
    )

    try {
      FastPreviewManager.getInstance(projectRule.project).disable(ManualDisabledReason)

      action.update(event)
      // Syntax errors does NOT take precedence over out of date when Fast Preview is Disabled
      assertEquals("Out of date (The preview is out of date)", event.presentation.asString())
    } finally {
      FastPreviewManager.getInstance(projectRule.project).enable()
    }

    composePreviewManager.currentStatus =
      originStatus.copy(
        hasSyntaxErrors = true,
        hasRuntimeErrors = true,
        isOutOfDate = true,
        isRefreshing = true
      )
    action.update(event)
    assertEquals("Loading... (The preview is updating...)", event.presentation.asString())

    // Most other statuses take precedence over runtime errors
    composePreviewManager.currentStatus =
      originStatus.copy(
        hasSyntaxErrors = true,
        hasRuntimeErrors = true,
        isOutOfDate = true,
        isRefreshing = true
      )
    action.update(event)
    assertEquals("Loading... (The preview is updating...)", event.presentation.asString())

    composePreviewManager.currentStatus =
      originStatus.copy(
        hasRuntimeErrors = true,
        isOutOfDate = true,
      )
    try {
      FastPreviewManager.getInstance(projectRule.project).disable(ManualDisabledReason)

      action.update(event)
      // Syntax errors does NOT take precedence over out of date when Fast Preview is Disabled
      assertEquals("Out of date (The preview is out of date)", event.presentation.asString())
    } finally {
      FastPreviewManager.getInstance(projectRule.project).enable()
    }

    composePreviewManager.currentStatus =
      originStatus.copy(
        hasRuntimeErrors = true,
        hasSyntaxErrors = true,
      )
    action.update(event)
    assertEquals(
      "Paused (The preview will not update while your project contains syntax errors.)",
      event.presentation.asString()
    )
  }

  private fun InformationPopup.labelsDescription(): String =
    popupComponent
      .findAllDescendants(JLabel::class.java)
      .map { XmlStringUtil.stripHtml(it.text) }
      .joinToString("\n")

  private fun InformationPopup.linksDescription(): String =
    popupComponent
      .findAllDescendants(ActionLink::class.java)
      .map { it.text.replace("\\(.*\\)".toRegex(), "(SHORTCUT)") }
      .joinToString("\n")

  @Test
  fun `check InformationPopup states`() {
    val fastPreviewManager = projectRule.project.fastPreviewManager
    // Default state check
    run {
      val popup = defaultCreateInformationPopup(projectRule.project, context)!!
      assertEquals("The preview is up to date", popup.labelsDescription())
      assertEquals("Build & Refresh (SHORTCUT)", popup.linksDescription())
    }

    run {
      composePreviewManager.currentStatus = originStatus.copy(isOutOfDate = true)
      val popup = defaultCreateInformationPopup(projectRule.project, context)!!
      assertEquals("The preview is out of date", popup.labelsDescription())
      assertEquals("Build & Refresh (SHORTCUT)", popup.linksDescription())
    }

    // Verify popup for an error that auto disabled the Fast Preview
    run {
      fastPreviewManager.disable(DisableReason("error"))
      try {
        composePreviewManager.currentStatus = originStatus.copy(isOutOfDate = true)
        val popup = defaultCreateInformationPopup(projectRule.project, context)!!
        assertEquals(
          "The code might contain errors or might not work with Preview Live Edit.",
          popup.labelsDescription()
        )
        assertEquals(
          """
          Build & Refresh (SHORTCUT)
          Re-enable
          Do not disable automatically
          View Details
        """
            .trimIndent(),
          popup.linksDescription()
        )
      } finally {
        fastPreviewManager.enable()
      }
    }

    // Verify popup when the preview is out of date and the USER has disabled Fast Preview
    run {
      fastPreviewManager.disable(ManualDisabledReason)
      try {
        composePreviewManager.currentStatus = originStatus.copy(isOutOfDate = true)
        val popup = defaultCreateInformationPopup(projectRule.project, context)!!
        assertEquals("The preview is out of date", popup.labelsDescription())
        assertEquals("Build & Refresh (SHORTCUT)", popup.linksDescription())
      } finally {
        fastPreviewManager.enable()
      }
    }

    // Verify refresh status
    run {
      composePreviewManager.currentStatus =
        originStatus.copy(
          isRefreshing = true,
          isOutOfDate =
            true // Leaving out of date to true to verify it does not take precedence over refresh
        )
      val popup = defaultCreateInformationPopup(projectRule.project, context)!!
      assertEquals("The preview is updating...", popup.labelsDescription())
      assertEquals("Build & Refresh (SHORTCUT)", popup.linksDescription())
    }

    // Verify syntax error status
    run {
      composePreviewManager.currentStatus =
        originStatus.copy(
          hasSyntaxErrors = true,
          isOutOfDate =
            true // Leaving out of date to true to verify it does not take precedence over refresh
        )
      val popup = defaultCreateInformationPopup(projectRule.project, context)!!
      assertEquals(
        "The preview will not update while your project contains syntax errors.",
        popup.labelsDescription()
      )
      assertEquals(
        """
        Build & Refresh (SHORTCUT)
        View Problems"""
          .trimIndent(),
        popup.linksDescription()
      )
    }

    // Verify render issues status
    run {
      composePreviewManager.currentStatus = originStatus.copy(hasRuntimeErrors = true)
      val popup = defaultCreateInformationPopup(projectRule.project, context)!!
      assertEquals(
        "Some problems were found while rendering the preview",
        popup.labelsDescription()
      )
      assertEquals(
        """
        Build & Refresh (SHORTCUT)
        View Problems"""
          .trimIndent(),
        popup.linksDescription()
      )
    }
  }

  @Test
  fun `test popup is triggered`() {
    val fakePopup =
      object : InformationPopup {
        override val popupComponent: JComponent = object : JComponent() {}
        override var onMouseEnteredCallback: () -> Unit = {}
        override fun hidePopup() {}
        override fun showPopup(disposableParent: Disposable, event: InputEvent) {}
        override fun isVisible(): Boolean = false
        override fun dispose() {}
      }

    var popupRequested = 0
    val action =
      PreviewIssueNotificationAction(projectRule.testRootDisposable) { _, _ ->
        popupRequested++
        fakePopup
      }
    val event =
      TestActionEvent.createTestEvent(
        action,
        context,
        MouseEvent(JPanel(), 0, 0, 0, 0, 0, 1, true, MouseEvent.BUTTON1)
      )
    action.update(event)
    assertEquals(0, popupRequested)
    action.actionPerformed(event)
    assertEquals(1, popupRequested)
  }
  
  private fun Presentation.asString() = "$text ($description)" 
}
