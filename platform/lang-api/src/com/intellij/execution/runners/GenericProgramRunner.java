/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.execution.runners;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GenericProgramRunner<Settings extends RunnerSettings> extends BaseProgramRunner<Settings> {
  @Override
  protected void execute(@NotNull ExecutionEnvironment environment, @Nullable final Callback callback, @NotNull RunProfileState state)
    throws ExecutionException {
    ExecutionManager.getInstance(environment.getProject()).startRunProfile(new RunProfileStarter() {
      @Override
      public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return postProcess(environment, doExecute(state, environment), callback);
      }
    }, state, environment);
  }

  @Nullable
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
    //noinspection deprecation
    return doExecute(environment.getProject(), state, environment.getContentToReuse(), environment);
  }

  /**
   * @deprecated
   */
  @SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
  @Deprecated
  @Nullable
  protected RunContentDescriptor doExecute(@NotNull Project project,
                                           @NotNull RunProfileState state,
                                           @Nullable RunContentDescriptor contentToReuse,
                                           @NotNull ExecutionEnvironment environment) throws ExecutionException {
    throw new AbstractMethodError();
  }
}
