package io.github.intellij.dlanguage.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.bouncycastle.math.raw.Mod;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DlangRunAppConfiguration extends ModuleBasedConfiguration<RunConfigurationModule, Module> {

    private String workDir;
    private String additionalParams;
    private Map<String, String> envVars;

    public DlangRunAppConfiguration(final String name, final Project project, final ConfigurationFactory factory) {
        super(name, new RunConfigurationModule(project), factory);

        final Collection<Module> modules = this.getValidModules();
        if (!modules.isEmpty()) {
            //Get first valid module and use it
            this.setModule(modules.iterator().next());
        }

        workDir = project.getBasePath();
        envVars = new HashMap<>();
    }

    public String getExecutablePath() {
        final Module module = getConfigurationModule().getModule();
        if (module != null) {
            final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            final String outputPath = moduleRootManager.getModuleExtension(CompilerModuleExtension.class).getCompilerOutputUrl();
            String filename = module.getName();
            if (SystemInfo.isWindows) {
                filename += ".exe";
            }
            final File outputFile = new File(VfsUtilCore.urlToPath(outputPath), filename);
            return outputFile.getPath();
        } else {
            return "";
        }
    }

    @Override
    public Collection<Module> getValidModules() {
        final Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        final DMDRunner appRunner = new DMDRunner();

        final ArrayList<Module> res = new ArrayList<>();
        for (final Module module : modules) {
            if (appRunner.isValidModule(module)) {
                res.add(module);
            }
        }
        return res;
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DlangRunAppConfigurationEditor();
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
        return new DlangRunAppState(env, this);
    }

    @Override
    public void writeExternal(@NotNull final Element element) throws WriteExternalException {
        if (envVars == null) {
            envVars = new HashMap<>();
        }

        super.writeExternal(element);
        XmlSerializer.serializeInto(this, element);
    }

    @Override
    public void readExternal(@NotNull final Element element) throws InvalidDataException {
        super.readExternal(element);
        readModule(element);
        XmlSerializer.deserializeInto(this, element);
    }

    /* Getters and Setters. Autogenerated by IntelliJ IDEA */
    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(final String workDir) {
        this.workDir = workDir;
    }

    public String getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(final String additionalParams) {
        this.additionalParams = additionalParams;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(final Map<String, String> envVars) {
        this.envVars = envVars;
    }
}
