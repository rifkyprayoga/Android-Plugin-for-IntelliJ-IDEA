/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.android.tools.idea.actions

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.ui.EditorNotifications

object HideAndroidBannerAction : AnAction("Hide Android Banners") {
  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project
    val editor = event.getData(PlatformCoreDataKeys.FILE_EDITOR)
    if (project != null && editor != null) {
      PropertiesComponent.getInstance().setValue(
        "PROJECT_STRUCTURE_NOTIFICATION_HIDE_ACTION_TIMESTAMP",
        System.currentTimeMillis().toString()
      )
      EditorNotifications.getInstance(project).updateAllNotifications()
    }
  }
}