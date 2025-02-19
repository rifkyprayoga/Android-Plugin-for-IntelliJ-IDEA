/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.tools.rendering;

import com.intellij.openapi.application.ApplicationManager;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be implemented by executors of rendered async actions.
 */
public interface RenderAsyncActionExecutor {
  /**
   * Number of ms that we will wait for the rendering thread to return before timing out
   */
  long DEFAULT_RENDER_THREAD_TIMEOUT_MS = Long.getLong("layoutlib.thread.timeout",
                                                       TimeUnit.SECONDS.toMillis(10));

  long DEFAULT_RENDER_THREAD_QUEUE_TIMEOUT_MS = Long.getLong("layoutlib.thread.queue.timeout",
                                                       TimeUnit.SECONDS.toMillis(
                                                         (ApplicationManager.getApplication() == null ||
                                                          ApplicationManager.getApplication().isUnitTestMode()) ? 50 : 60)
  );

  /**
   * Returns the number of render actions that this executor has executed.
   */
  long getExecutedRenderActionCount();

  /**
   * Runs an action that requires the rendering lock. Layoutlib is not thread safe so any rendering actions should be called using this
   * method.
   * <p/>
   * This method will run the passed action asynchronously and return a {@link CompletableFuture}
   *
   * @param queueingTimeout maximum timeout for this action to wait to be executed.
   * @param queueingTimeoutUnit {@link TimeUnit} for queueingTimeout.
   * @param actionTimeout maximum timeout for this action to executed once it has started running.
   * @param actionTimeoutUnit {@link TimeUnit} for actionTimeout.
   * @param renderingTopic enum representing context in which the render is happening and its priority.
   * @param callable {@link Callable} to be executed with the render action.
   * @param <T> return type of the given callable.
   */
  @NotNull <T> CompletableFuture<T> runAsyncActionWithTimeout(
    long queueingTimeout, @NotNull TimeUnit queueingTimeoutUnit,
    long actionTimeout, @NotNull TimeUnit actionTimeoutUnit,
    @NotNull RenderingTopic renderingTopic, @NotNull Callable<T> callable);

  /**
   * Runs an action that requires the rendering lock. Layoutlib is not thread safe so any rendering actions should be called using this
   * method.
   * <p/>
   * This method will run the passed action asynchronously and return a {@link CompletableFuture}
   *
   * @param actionTimeout maximum timeout for this action to executed once it has started running.
   * @param actionTimeoutUnit {@link TimeUnit} for actionTimeout.
   * @param renderingTopic enum representing context in which the render is happening and its priority.
   * @param callable {@link Callable} to be executed with the render action.
   * @param <T> return type of the given callable.
   */
  default @NotNull <T> CompletableFuture<T> runAsyncActionWithTimeout(
    long actionTimeout, @NotNull TimeUnit actionTimeoutUnit,
    @NotNull RenderingTopic renderingTopic, @NotNull Callable<T> callable) {
    return runAsyncActionWithTimeout(
      DEFAULT_RENDER_THREAD_QUEUE_TIMEOUT_MS, TimeUnit.MILLISECONDS,
      actionTimeout, actionTimeoutUnit, renderingTopic, callable);
  }

  /**
   * Runs an action that requires the rendering lock. Layoutlib is not thread safe so any rendering actions should be called using this
   * method.
   * <p/>
   * This method will run the passed action asynchronously and return a {@link CompletableFuture}
   *
   * @param actionTimeout maximum timeout for this action to executed once it has started running.
   * @param actionTimeoutUnit {@link TimeUnit} for actionTimeout.
   * @param callable {@link Callable} to be executed with the render action.
   * @param <T> return type of the given callable.
   */
  default @NotNull <T> CompletableFuture<T> runAsyncActionWithTimeout(
    long actionTimeout, @NotNull TimeUnit actionTimeoutUnit,
    @NotNull Callable<T> callable) {
    return runAsyncActionWithTimeout(
      DEFAULT_RENDER_THREAD_QUEUE_TIMEOUT_MS, TimeUnit.MILLISECONDS,
      actionTimeout, actionTimeoutUnit, RenderingTopic.NOT_SPECIFIED, callable);
  }

  /**
   * Runs an action that requires the rendering lock. Layoutlib is not thread safe so any rendering actions should be called using this
   * method.
   * <p/>
   * This method will run the passed action asynchronously and return a {@link CompletableFuture}
   */
  default @NotNull <T> CompletableFuture<T> runAsyncAction(@NotNull Callable<T> callable) {
    return runAsyncActionWithTimeout(
      DEFAULT_RENDER_THREAD_QUEUE_TIMEOUT_MS, TimeUnit.MILLISECONDS,
      DEFAULT_RENDER_THREAD_TIMEOUT_MS, TimeUnit.MILLISECONDS,
      RenderingTopic.NOT_SPECIFIED, callable);
  }

  /**
   * Runs an action that requires the rendering lock. Layoutlib is not thread safe so any rendering actions should be called using this
   * method.
   * <p/>
   * This method will run the passed action asynchronously and return a {@link CompletableFuture}
   */
  default @NotNull <T> CompletableFuture<T> runAsyncAction(@NotNull RenderingTopic renderingTopic,
                                                           @NotNull Callable<T> callable) {
    return runAsyncActionWithTimeout(
      DEFAULT_RENDER_THREAD_QUEUE_TIMEOUT_MS, TimeUnit.MILLISECONDS,
      DEFAULT_RENDER_THREAD_TIMEOUT_MS, TimeUnit.MILLISECONDS,
      renderingTopic, callable);
  }

  /**
   * Runs an action that requires the rendering lock. Layoutlib is not thread safe so any rendering actions should be called using this
   * method.
   * <p/>
   * This method will run the passed action asynchronously
   */
  @NotNull
  default CompletableFuture<Void> runAsyncAction(@NotNull Runnable runnable) {
    return runAsyncAction(RenderingTopic.NOT_SPECIFIED, () -> {
      runnable.run();
      return null;
    });
  }

  /**
   * Runs an action that requires the rendering lock. Layoutlib is not thread safe so any rendering actions should be called using this
   * method.
   * <p/>
   * This method will run the passed action asynchronously
   */
  @NotNull
  default CompletableFuture<Void> runAsyncAction(@NotNull RenderingTopic renderingTopic, @NotNull Runnable runnable) {
    return runAsyncAction(renderingTopic, () -> {
      runnable.run();
      return null;
    });
  }

  /**
   * Cancels all pending actions of the given topics.
   * <p>
   * Returns the number of cancelled actions.
   */
  int cancelActionsByTopic(List<RenderingTopic> topicsToCancel, boolean mayInterruptIfRunning);

  /**
   * Cancels all pending actions with rendering priority lower than or equal to minPriority,
   * regardless of their topic.
   * <p>
   * Returns the number of cancelled actions.
   */
  default int cancelLowerPriorityActions(int minPriority, boolean mayInterruptIfRunning) {
    return cancelActionsByTopic(
      Arrays.stream(RenderingTopic.values()).filter(
        (topic) -> topic.getPriority() <= minPriority
      ).collect(Collectors.toList()),
      mayInterruptIfRunning
    );
  }

  /**
   * Enum representing the context or tool in which a render is happening and
   * the priority that the RenderExecutor should apply to run the action.
   */
  enum RenderingTopic {
    // Topic used for actions related with disposing or freeing resources.
    CLEAN("Clean", 200),
    // Topic used by default when the tool/context doesn't specify one.
    NOT_SPECIFIED("Not specified", 100),
    COMPOSE_PREVIEW("Compose preview", 100),
    VISUAL_LINT("Visual lint", 1);

    private final String myName;
    private final int myPriority;

    RenderingTopic(String name, int priority) {
      myName = name;
      myPriority = priority;
    }

    public String getName() {
      return myName;
    }

    public int getPriority() {
      return myPriority;
    }
  }
}
