/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tools.idea.gradle.dsl.model.android

import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_ADD_ELEMENTS
import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_ADD_ELEMENTS_EXPECTED
import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_ADD_ELEMENTS_FROM_EXISTING
import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_ADD_ELEMENTS_FROM_EXISTING_EXPECTED
import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_EDIT_ELEMENTS
import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_EDIT_ELEMENTS_EXPECTED
import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_PARSE_ELEMENTS
import com.android.tools.idea.gradle.dsl.TestFileNameImpl.BUILD_FEATURES_MODEL_REMOVE_ELEMENTS
import com.android.tools.idea.gradle.dsl.api.android.BuildFeaturesModel
import com.android.tools.idea.gradle.dsl.model.GradleFileModelTestCase
import org.junit.Test

/**
 * Tests for [BuildFeaturesModel].
 */
class BuildFeaturesModelTest : GradleFileModelTestCase() {

  @Test
  fun testParseElements() {
    writeToBuildFile(BUILD_FEATURES_MODEL_PARSE_ELEMENTS)

    val buildModel = gradleBuildModel
    val buildFeatures = buildModel.android().buildFeatures()
    assertEquals("compose", false, buildFeatures.compose())
    assertEquals("dataBinding", false, buildFeatures.dataBinding())
    assertEquals("mlModelBinding", false, buildFeatures.mlModelBinding())
    assertEquals("viewBinding", false, buildFeatures.viewBinding())
    assertEquals("prefab", false, buildFeatures.prefab())
    assertEquals("renderScript", false, buildFeatures.renderScript())
    assertEquals("buildConfig", false, buildFeatures.buildConfig())
    assertEquals("aidl", false, buildFeatures.aidl())
  }

  @Test
  fun testEditElements() {
    writeToBuildFile(BUILD_FEATURES_MODEL_EDIT_ELEMENTS)

    val buildModel = gradleBuildModel
    var buildFeatures = buildModel.android().buildFeatures()
    assertEquals("compose", false, buildFeatures.compose())
    assertEquals("dataBinding", false, buildFeatures.dataBinding())
    assertEquals("mlModelBinding", false, buildFeatures.mlModelBinding())
    assertEquals("viewBinding", false, buildFeatures.viewBinding())
    assertEquals("prefab", false, buildFeatures.prefab())
    assertEquals("renderScript", false, buildFeatures.renderScript())
    assertEquals("buildConfig", false, buildFeatures.buildConfig())
    assertEquals("aidl", false, buildFeatures.aidl())
    buildFeatures.compose().setValue(true)
    buildFeatures.dataBinding().setValue(true)
    buildFeatures.mlModelBinding().setValue(true)
    buildFeatures.viewBinding().setValue(true)
    buildFeatures.prefab().setValue(true)
    buildFeatures.renderScript().setValue(true)
    buildFeatures.buildConfig().setValue(true)
    buildFeatures.aidl().setValue(true)
    applyChangesAndReparse(buildModel)
    verifyFileContents(myBuildFile, BUILD_FEATURES_MODEL_EDIT_ELEMENTS_EXPECTED)
    buildFeatures = buildModel.android().buildFeatures()
    assertEquals("compose", true, buildFeatures.compose())
    assertEquals("dataBinding", true, buildFeatures.dataBinding())
    assertEquals("mlModelBinding", true, buildFeatures.mlModelBinding())
    assertEquals("viewBinding", true, buildFeatures.viewBinding())
    assertEquals("prefab", true, buildFeatures.prefab())
    assertEquals("renderScript", true, buildFeatures.renderScript())
    assertEquals("buildConfig", true, buildFeatures.buildConfig())
    assertEquals("aidl", true, buildFeatures.aidl())
  }

  @Test
  fun testAddElements() {
    writeToBuildFile(BUILD_FEATURES_MODEL_ADD_ELEMENTS)

    val buildModel = gradleBuildModel
    var buildFeatures = buildModel.android().buildFeatures()
    assertMissingProperty("compose", buildFeatures.compose())
    assertMissingProperty("dataBinding", buildFeatures.dataBinding())
    assertMissingProperty("mlModelBinding", buildFeatures.mlModelBinding())
    assertMissingProperty("viewBinding", buildFeatures.viewBinding())
    assertMissingProperty("prefab", buildFeatures.prefab())
    assertMissingProperty("renderScript", buildFeatures.renderScript())
    assertMissingProperty("buildConfig", buildFeatures.buildConfig())
    assertMissingProperty("aidl", buildFeatures.aidl())
    buildFeatures.compose().setValue(false)
    buildFeatures.dataBinding().setValue(false)
    buildFeatures.mlModelBinding().setValue(false)
    buildFeatures.viewBinding().setValue(false)
    buildFeatures.prefab().setValue(false)
    buildFeatures.renderScript().setValue(false)
    buildFeatures.buildConfig().setValue(false)
    buildFeatures.aidl().setValue(false)
    applyChangesAndReparse(buildModel)
    verifyFileContents(myBuildFile, BUILD_FEATURES_MODEL_ADD_ELEMENTS_EXPECTED)
    buildFeatures = buildModel.android().buildFeatures()
    assertEquals("compose", false, buildFeatures.compose())
    assertEquals("dataBinding", false, buildFeatures.dataBinding())
    assertEquals("mlModelBinding", false, buildFeatures.mlModelBinding())
    assertEquals("viewBinding", false, buildFeatures.viewBinding())
    assertEquals("prefab", false, buildFeatures.prefab())
    assertEquals("renderScript", false, buildFeatures.renderScript())
    assertEquals("buildConfig", false, buildFeatures.buildConfig())
    assertEquals("aidl", false, buildFeatures.aidl())
  }

  @Test
  fun testAddElementsFromExisting() {
    writeToBuildFile(BUILD_FEATURES_MODEL_ADD_ELEMENTS_FROM_EXISTING)

    val buildModel = gradleBuildModel
    var buildFeatures = buildModel.android().buildFeatures()
    assertMissingProperty("compose", buildFeatures.compose())
    assertMissingProperty("dataBinding", buildFeatures.dataBinding())
    assertMissingProperty("mlModelBinding", buildFeatures.mlModelBinding())
    assertMissingProperty("viewBinding", buildFeatures.viewBinding())
    assertMissingProperty("prefab", buildFeatures.prefab())
    assertMissingProperty("renderScript", buildFeatures.renderScript())
    assertMissingProperty("buildConfig", buildFeatures.buildConfig())
    assertMissingProperty("aidl", buildFeatures.aidl())
    buildFeatures.compose().setValue(false)
    buildFeatures.dataBinding().setValue(false)
    buildFeatures.mlModelBinding().setValue(false)
    buildFeatures.viewBinding().setValue(false)
    buildFeatures.prefab().setValue(false)
    buildFeatures.renderScript().setValue(false)
    buildFeatures.buildConfig().setValue(false)
    buildFeatures.aidl().setValue(false)
    applyChangesAndReparse(buildModel)
    verifyFileContents(myBuildFile, BUILD_FEATURES_MODEL_ADD_ELEMENTS_FROM_EXISTING_EXPECTED)
    buildFeatures = buildModel.android().buildFeatures()
    assertEquals("compose", false, buildFeatures.compose())
    assertEquals("dataBinding", false, buildFeatures.dataBinding())
    assertEquals("mlModelBinding", false, buildFeatures.mlModelBinding())
    assertEquals("viewBinding", false, buildFeatures.viewBinding())
    assertEquals("prefab", false, buildFeatures.prefab())
    assertEquals("renderScript", false, buildFeatures.renderScript())
    assertEquals("buildConfig", false, buildFeatures.buildConfig())
    assertEquals("aidl", false, buildFeatures.aidl())
  }

  @Test
  fun testRemoveElements() {
    writeToBuildFile(BUILD_FEATURES_MODEL_REMOVE_ELEMENTS)

    val buildModel = gradleBuildModel
    var buildFeatures = buildModel.android().buildFeatures()
    checkForValidPsiElement(buildFeatures, BuildFeaturesModelImpl::class.java)
    assertEquals("compose", false, buildFeatures.compose())
    assertEquals("dataBinding", false, buildFeatures.dataBinding())
    assertEquals("mlModelBinding", false, buildFeatures.mlModelBinding())
    assertEquals("viewBinding", false, buildFeatures.viewBinding())
    assertEquals("prefab", false, buildFeatures.prefab())
    assertEquals("renderScript", false, buildFeatures.renderScript())
    assertEquals("buildConfig", false, buildFeatures.buildConfig())
    assertEquals("aidl", false, buildFeatures.aidl())
    buildFeatures.compose().delete()
    buildFeatures.dataBinding().delete()
    buildFeatures.mlModelBinding().delete()
    buildFeatures.viewBinding().delete()
    buildFeatures.prefab().delete()
    buildFeatures.renderScript().delete()
    buildFeatures.buildConfig().delete()
    buildFeatures.aidl().delete()
    applyChangesAndReparse(buildModel)
    verifyFileContents(myBuildFile, "")
    buildFeatures = buildModel.android().buildFeatures()
    checkForInvalidPsiElement(buildFeatures, BuildFeaturesModelImpl::class.java)
    assertMissingProperty("compose", buildFeatures.compose())
    assertMissingProperty("dataBinding", buildFeatures.dataBinding())
    assertMissingProperty("mlModelBinding", buildFeatures.mlModelBinding())
    assertMissingProperty("viewBinding", buildFeatures.viewBinding())
    assertMissingProperty("prefab", buildFeatures.prefab())
    assertMissingProperty("renderScript", buildFeatures.renderScript())
    assertMissingProperty("buildConfig", buildFeatures.buildConfig())
    assertMissingProperty("aidl", buildFeatures.aidl())
  }
}