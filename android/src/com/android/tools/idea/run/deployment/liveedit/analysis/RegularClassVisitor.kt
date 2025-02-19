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
package com.android.tools.idea.run.deployment.liveedit.analysis

import com.android.tools.idea.run.deployment.liveedit.LiveEditUpdateException
import com.android.tools.idea.run.deployment.liveedit.analysis.diffing.ClassVisitor
import com.android.tools.idea.run.deployment.liveedit.analysis.diffing.FieldDiff
import com.android.tools.idea.run.deployment.liveedit.analysis.diffing.MethodDiff
import com.android.tools.idea.run.deployment.liveedit.analysis.diffing.MethodVisitor
import com.android.tools.idea.run.deployment.liveedit.analysis.leir.EnclosingMethod
import com.android.tools.idea.run.deployment.liveedit.analysis.leir.IrAccessFlag
import com.android.tools.idea.run.deployment.liveedit.analysis.leir.IrField
import com.android.tools.idea.run.deployment.liveedit.analysis.leir.IrInstructionList
import com.android.tools.idea.run.deployment.liveedit.analysis.leir.IrMethod

// TODO: which annotations from Compose and Kotlin do we need to allow-list? Once we know, modifying other annotations should be an error.
class RegularClassVisitor(private val className: String) : ClassVisitor {
  private val changedMethods = mutableListOf<MethodDiff>()
  val modifiedMethods: List<MethodDiff> = changedMethods

  override fun visitAccess(added: Set<IrAccessFlag>, removed: Set<IrAccessFlag>) {
    if (added.isNotEmpty()) {
      val msg = "added access flag(s): " + added.joinToString(", ")
      handleUnsupportedChange(msg)
    }

    if (removed.isNotEmpty()) {
      val msg = "removed access flag(s): " + removed.joinToString(",")
      handleUnsupportedChange(msg)
    }
  }

  override fun visitSignature(old: String?, new: String?) {
    handleUnsupportedChange("signature changed from '$old' to '$new'")
  }

  override fun visitSuperName(old: String?, new: String?) {
    handleUnsupportedChange("superclass changed from '$old' to '$new'")
  }

  override fun visitInterfaces(added: Set<String>, removed: Set<String>) {
    handleUnsupportedChange("interfaces changed; added '$added' and removed'$removed'")
  }

  override fun visitVersion(old: Int, new: Int) {
    handleUnsupportedChange("version changed from '$old' to '$new'")
  }

  override fun visitEnclosingMethod(old: EnclosingMethod?, new: EnclosingMethod?) {
    handleUnsupportedChange("enclosing method changed from '$old' to '$new'")
  }

  override fun visitMethods(added: List<IrMethod>, removed: List<IrMethod>, modified: List<MethodDiff>) {
    if (added.isNotEmpty()) {
      val msg = "added method(s): " + added.joinToString(", ") { it.name + it.desc }
      handleUnsupportedChange(msg)
    }

    if (removed.isNotEmpty()) {
      val msg = "removed method(s): " + removed.joinToString(", ") { it.name + it.desc }
      handleUnsupportedChange(msg)
    }

    for (method in modified) {
      if (method.name == "<init>") {
        handleUnsupportedChange("modified constructor $className${method.desc.trimEnd('V')}")
      }

      if (method.name == "<clinit>") {
        // We can probably parse the instruction list to get a better error message here, but this is fine for now.
        handleUnsupportedChange("modified static initializer (did a field's initial value change?)")
      }

      val visitor = RegularMethodVisitor(className, method.name, method.desc)
      method.accept(visitor)
      if (visitor.hasNonSourceInfoChanges) changedMethods.add(method)
    }
  }

  override fun visitFields(added: List<IrField>, removed: List<IrField>, modified: List<FieldDiff>) {
    if (added.isNotEmpty()) {
      val msg = "added field(s): " + added.joinToString(", ") { it.name }
      handleUnsupportedChange(msg)
    }

    if (removed.isNotEmpty()) {
      val msg = "removed field(s): " + removed.joinToString(", ") { it.name }
      handleUnsupportedChange(msg)
    }

    // TODO: if we want, we can traverse the field diff to build a more detailed message.
    if (modified.isNotEmpty()) {
      val msg = "modified field(s): " + modified.joinToString(", ") { it.name }
      handleUnsupportedChange(msg)
    }
  }

  private fun handleUnsupportedChange(message: String) {
    throw LiveEditUpdateException(
      LiveEditUpdateException.Error.UNSUPPORTED_SRC_CHANGE_UNRECOVERABLE, "in ${className.replace('/', '.')}, $message", null, null)
  }
}

// No override for visiting try/catch or locals, because those are all supported changes.
private class RegularMethodVisitor(val className: String, val methodName: String, val methodDesc: String) : MethodVisitor {
  var hasNonSourceInfoChanges: Boolean = false
    private set

  override fun visitInstructions(old: IrInstructionList, new: IrInstructionList) {
    // Filter out the debugging info that compose modifies on every change
    hasNonSourceInfoChanges = !onlyHasSourceInfoChanges(old, new)
  }

  override fun visitAccess(added: Set<IrAccessFlag>, removed: Set<IrAccessFlag>) {
    handleUnsupportedChange("interfaces changed; added '$added' and removed'$removed'")
  }

  override fun visitSignature(old: String?, new: String?) {
    handleUnsupportedChange("signature changed from '$old' to '$new'")
  }

  private fun handleUnsupportedChange(message: String) {
    throw LiveEditUpdateException(
      LiveEditUpdateException.Error.UNSUPPORTED_SRC_CHANGE_UNRECOVERABLE,
      "in ${className.replace('/', '.')}.$methodName$methodDesc, $message", null, null)
  }
}