package com.devonfw.devcon.common.api;

import java.nio.file.Path;

import com.devonfw.devcon.common.api.data.ProjectInfo;
import com.devonfw.devcon.common.utils.ContextPathInfo;
import com.devonfw.devcon.input.Input;
import com.devonfw.devcon.output.Output;
import com.google.common.base.Optional;

/**
 * The container of a series of related {@link Command}s
 *
 * @author ivanderk
 */
public interface CommandModule {

  /**
   *
   * @return registry
   */
  CommandRegistry getRegistry();

  /**
   * @return output
   */
  Output getOutput();

  /**
   * @return contextPathInfo
   */
  Optional<ProjectInfo> getProjectInfo();

  /**
   * @return input
   */
  Input getInput();

  /**
   * @param registry new value of {@link #getRegistry}
   */
  void setRegistry(CommandRegistry registry);

  /**
   * @param input new value of {@link #getinput}.
   */
  void setInput(Input input);

  /**
   * @param output new value of {@link #getoutput}.
   */
  void setOutput(Output output);

  /**
   * @param projectInfo
   */
  void setProjectInfo(Optional<ProjectInfo> projectInfo);

  /**
   *
   * @return ContextPathInfo
   */
  ContextPathInfo getContextPathInfo();

  /**
   *
   * @param contextPathInfo
   */
  void setContextPathInfo(ContextPathInfo contextPathInfo);

  /**
   * get {@link Command} instance
   *
   * @param module
   * @param command
   * @return
   */
  Optional<Command> getCommand(String module, String command);

  /**
   * Impl for project module
   */

  Optional<Command> getCommand(String module, String command, ProjectInfo projectInfo);

  /**
   * @param path
   * @return
   */
  Path getPath(String path);

}