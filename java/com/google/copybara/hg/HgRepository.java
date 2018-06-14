/*
 * Copyright (C) 2018 Google Inc.
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

package com.google.copybara.hg;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.copybara.exception.RepoException;
import com.google.copybara.shell.Command;
import com.google.copybara.shell.CommandException;
import com.google.copybara.util.CommandOutput;
import com.google.copybara.util.CommandOutputWithStatus;
import com.google.copybara.util.CommandRunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class for manipulating Hg (Mercurial) repositories
 */
public class HgRepository {


  /**
   * The location of the {@code .hg} directory.
   */
  private final Path hgDir;

  protected HgRepository(Path hgDir) {
    this.hgDir = hgDir;
  }

  /**
   * Initializes a new hg repository
   * @return the new HgRepository
   * @throws RepoException if the directory cannot be created
   */
  public HgRepository init() throws RepoException {
    try {
      Files.createDirectories(hgDir);
    }
    catch (IOException e) {
      throw new RepoException("Cannot create directory: " + e.getMessage(), e);
    }
    hg(hgDir, ImmutableList.of("init"));
    return this;
  }

  /**
   * Invokes {@code hg} in the directory given by {@code cwd} against this repository and returns
   * the {@link CommandOutput} if the command execution was successful.
   *
   * Only to be used externally for testing.
   *
   * @param cwd the directory in which to execute the command
   * @param params the argv to pass to Hg, excluding the initial {@code hg}
   */
  @VisibleForTesting
  public CommandOutput hg(Path cwd, String... params) throws RepoException {
    return hg(cwd, Arrays.asList(params));
  }

  private CommandOutput hg(Path cwd, Iterable<String> params) throws RepoException {
    try{
      return executeHg(cwd, params, -1);
    }
    catch (CommandException e) {
      throw new RepoException("Error executing 'hg': " + e.getMessage(), e);
    }
  }

  private static CommandOutputWithStatus executeHg(Path cwd, Iterable<String> params,
      int maxLogLines) throws CommandException {
    List<String> allParams = new ArrayList<>(Iterables.size(params) + 1);
    allParams.add("hg"); //TODO(jlliu): resolve Hg binary here
    Iterables.addAll(allParams, params);
    Command cmd = new Command(
        Iterables.toArray(allParams, String.class), null, cwd.toFile());
        //TODO(jlliu): have environment vars
    CommandRunner runner = new CommandRunner(cmd);
    return
        maxLogLines >= 0 ? runner.withMaxStdOutLogLines(maxLogLines).execute() : runner.execute();
  }
}