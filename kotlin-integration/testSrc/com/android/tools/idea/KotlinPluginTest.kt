/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea

import com.google.common.truth.Truth.assertThat
import com.intellij.testFramework.ApplicationRule
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import org.junit.ClassRule
import org.junit.Test

class KotlinPluginTest {

  companion object {
    @ClassRule
    @JvmField
    val appRule = ApplicationRule()
  }

  @Test
  fun testKotlinVersion() {
    val kotlinLayout = KotlinPluginLayout.instance

    // The 'standalone' compiler version is defined by Kotlin/kotlinc/build.txt.
    // This version is the default for new projects created in the IDE. So, it should generally be a release version.
    val standaloneCompilerVersion = kotlinLayout.standaloneCompilerVersion
    assertThat(standaloneCompilerVersion.isRelease).isTrue()

    // The 'IDE' compiler version is defined by kotlin-plugin.jar!/META-INF/compiler.version.
    // This version corresponds to the Kotlin compiler used for IDE analysis.
    val ideCompilerVersion = kotlinLayout.ideCompilerVersion
    assertThat(ideCompilerVersion.isSnapshot).isFalse()
    assertThat(ideCompilerVersion.kotlinVersion).isAtLeast(standaloneCompilerVersion.kotlinVersion)
  }
}
