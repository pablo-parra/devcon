package com.devonfw.devcon.modules.sencha;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.devonfw.devcon.common.api.annotations.CmdModuleRegistry;
import com.devonfw.devcon.common.api.annotations.Command;
import com.devonfw.devcon.common.api.annotations.InputType;
import com.devonfw.devcon.common.api.annotations.Parameter;
import com.devonfw.devcon.common.api.annotations.Parameters;
import com.devonfw.devcon.common.api.data.ContextType;
import com.devonfw.devcon.common.api.data.DistributionInfo;
import com.devonfw.devcon.common.api.data.InputTypeNames;
import com.devonfw.devcon.common.api.data.ProjectType;
import com.devonfw.devcon.common.impl.AbstractCommandModule;
import com.devonfw.devcon.common.utils.Constants;
import com.devonfw.devcon.common.utils.Utils;
import com.google.common.base.Optional;

/**
 * Module to automate tasks related to devon4sencha projects (Ext JS)
 *
 * @author ivanderk
 */
@CmdModuleRegistry(name = "sencha", description = "Commands related with Ext JS6/Devon4Sencha projects")
public class Sencha extends AbstractCommandModule {

  private static final String DOT_GIT = ".git";

  @SuppressWarnings("javadoc")
  @Command(name = "run", description = "compiles in DEBUG mode and then runs the internal Sencha web server (\"app watch\")", context = ContextType.PROJECT)
  public void run() throws Exception {

    // TODO ivanderk Implementatin for MacOSX & Unix
    // if (!SystemUtils.IS_OS_WINDOWS) {
    // getOutput().showMessage("This task is currently only supported on Windows");
    // return;
    // }

    if (!this.projectInfo.isPresent()) {
      getOutput().showError("Not in a project or -path param not pointing to a project");
      return;
    }

    if (this.projectInfo.get().getProjecType().equals(ProjectType.DEVON4SENCHA)) {
      try {

        ProcessBuilder processBuilder = new ProcessBuilder("sencha", "app", "watch");
        processBuilder.directory(this.projectInfo.get().getPath().toFile());

        Process process = processBuilder.start();

        final InputStream isError = process.getErrorStream();
        final InputStream isOutput = process.getInputStream();

        Utils.processOutput(isError, isOutput, getOutput());

        getOutput().showMessage(" Sencha App Watch Started");

      } catch (Exception e) {
        getOutput().showError("An error occured during executing Sencha Cmd");
        throw e;
      }

    } else {
      getOutput().showMessage("Not a Sencha project (or does not have a corresponding devon.json file)");
    }
  }

  @SuppressWarnings("javadoc")
  @Command(name = "copyworkspace", description = "Copies a new Sencha workspace from a Devon dist to a particular path")
  @Parameters(values = {
  @Parameter(name = "workspace", description = "a location for the workspace (Current directory if not provided)", optional = true, inputType = @InputType(name = InputTypeNames.PATH)),
  @Parameter(name = "distpath", description = "location of the Devonfw Dist (Current directory if not provided)", optional = true, inputType = @InputType(name = InputTypeNames.PATH)) })
  public void copyworkspace(String workspace, String distpath) throws Exception {

    workspace = workspace.isEmpty()
        ? this.contextPathInfo.getCurrentWorkingDirectory().toString() + File.separatorChar + "devon4sencha"
        : workspace;

    Optional<DistributionInfo> distInfo = distpath.isEmpty() ? this.contextPathInfo.getDistributionRoot()
        : this.contextPathInfo.getDistributionRoot(distpath);

    if (!distInfo.isPresent()) {
      getOutput().showError("Not in a valid Devonfw Distribution...");
      return;
    }

    Path devon4sencha = distInfo.get().getPath().resolve("workspaces/examples/devon4sencha");
    if (devon4sencha.toFile().exists()) {

      getOutput().showMessage("Copying Devon4sencha workspace...");
      FileUtils.copyDirectory(devon4sencha.toFile(), getPath(workspace).toFile());
      getOutput().showMessage("Devon4sencha workspace created in %s: ", workspace);
    } else {
      getOutput().showError("Devonfw Distribution does not contain the Devon4sencha workspace");
      return;
    }
  }

  @SuppressWarnings("javadoc")
  @Command(name = "workspace", description = "Creates a new Sencha workspace (cloned from the Devon4Sencha repo on Github)", proxyParams = true)
  @Parameters(values = {
  @Parameter(name = "path", description = "a location for the workspace (Current directory if not provided)", optional = true, inputType = @InputType(name = InputTypeNames.PATH)),
  @Parameter(name = "username", description = "a Github user with permissions to access the Devon4Sencha repo", inputType = @InputType(name = InputTypeNames.GENERIC)),
  @Parameter(name = "password", description = "the password of the user", inputType = @InputType(name = InputTypeNames.PASSWORD)) })
  public void workspace(String path, String username, String password) throws Exception {

    path = path.isEmpty()
        ? this.contextPathInfo.getCurrentWorkingDirectory().toString() + File.separatorChar + "devon4sencha"
        : path + File.separatorChar + "devon4sencha";
    try {

      File folder = new File(path);
      if (!folder.exists()) {
        folder.mkdirs();
      }

      getOutput().showMessage("Cloning from " + Constants.SENCHA_REPO_URL + " to " + path);

      Git result = Git.cloneRepository().setURI(Constants.SENCHA_REPO_URL).setDirectory(folder)
          .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();

      getOutput().showMessage("Cloned Devon4Sencha workspace: %s", result.getRepository().getDirectory().toString());
    } catch (TransportException te) {
      File dotGit = new File(path + File.separator + DOT_GIT);
      if (dotGit.exists()) {
        FileUtils.deleteDirectory(dotGit);
      }
      getOutput().showError(
          "Connection error. Please verify your github credentials. Also if you work behind a proxy verify it's configuration or use the -proxyHost and -proxyPort parameters");
      throw te;

    } catch (Exception e) {
      getOutput().showError("Getting the Devon4Sencha code from Github: %s", e.getMessage());
      throw e;
    }

  }

  /**
   * @param appDir Location of Sencha Ext JS6 Application
   * @throws Exception Exception thrown by the Sencha build command
   */
  @Command(name = "build", description = "Builds a Sencha Ext JS6 project in a workspace", context = ContextType.PROJECT)
  public void build() throws Exception {

    if (!this.projectInfo.isPresent()) {
      getOutput().showError("Not in a project or -path param not pointing to a project");
      return;
    }
    try {

      ProcessBuilder processBuilder = new ProcessBuilder("sencha", "app", "build");
      processBuilder.directory(this.projectInfo.get().getPath().toFile());

      Process process = processBuilder.start();

      final InputStream isError = process.getErrorStream();
      final InputStream isOutput = process.getInputStream();

      Utils.processErrorAndOutPut(isError, isOutput, getOutput());

      // Wait to get exit value
      int pStatus = 0;
      try {
        pStatus = process.waitFor();
      } catch (InterruptedException e) {
        getOutput().showError("An error occured while executing build command", e.getMessage());
        throw e;
      }

      if (pStatus == 0) {

        getOutput().showMessage(" Sencha Build Successful");
      } else {
        getOutput().showError(" Sencha Build Failed");
      }
    } catch (Exception e) {
      getOutput().showError("An error occured while executing build command", e.getMessage());
      throw e;
    }
  }

  /**
   * @param appname Name of Sencha Ext JS6 app
   * @param workspacepath Path to Sencha Workspace (currentDir if not given)
   * @throws Exception Exception thrown by Sencha generate app Command
   */
  @Command(name = "create", description = "Creates a new Sencha Ext JS6 app", context = ContextType.NONE)
  @Parameters(values = {
  @Parameter(name = "appname", description = "Name of Sencha Ext JS6 app", inputType = @InputType(name = InputTypeNames.GENERIC)),
  @Parameter(name = "workspacepath", description = "Path to Sencha Workspace (currentDir if not given)", optional = true, inputType = @InputType(name = InputTypeNames.PATH)) })
  public void create(String appname, String workspacepath) throws Exception {

    try {

      workspacepath =
          workspacepath.isEmpty() ? getContextPathInfo().getCurrentWorkingDirectory().toString() : workspacepath;

      File starterTemplate = new File(workspacepath + File.separator + Constants.SENCHA_APP_STARTER_TEMPLATE);

      if (!starterTemplate.exists()) {
        getOutput().showError(starterTemplate.toString()
            + " not found. Please verify that you are creating the app in a Sencha workspace.");
        return;
      }

      File senchaApp = new File(workspacepath + File.separator + appname);

      if (!senchaApp.exists()) {

        ProcessBuilder processBuilder = new ProcessBuilder("sencha", "generate", "app", "-ext", "--starter",
            Constants.SENCHA_APP_STARTER_TEMPLATE, appname, senchaApp.toString());

        processBuilder.directory(new File(workspacepath));

        Process process = processBuilder.start();

        final InputStream isError = process.getErrorStream();
        final InputStream isOutput = process.getInputStream();

        Utils.processErrorAndOutPut(isError, isOutput, getOutput());

        // Wait to get exit value
        int pStatus = 0;
        try {
          pStatus = process.waitFor();
        } catch (InterruptedException e) {
          getOutput().showError("An error occured while executing create command", e.getMessage());
          throw e;
        }

        if (pStatus == 0) {
          getOutput().showMessage("Adding devon.json file...");
          Utils.addDevonJsonFile(senchaApp.toPath(), ProjectType.DEVON4SENCHA);
          getOutput().showMessage("Sencha Ext JS6 app Created");
        } else {
          getOutput().showError("The app " + senchaApp.toString() + " already exists.");
        }
      }
    } catch (Exception e) {
      getOutput().showError("An error occured while executing create command", e.getMessage());
      throw e;
    }
  }

}