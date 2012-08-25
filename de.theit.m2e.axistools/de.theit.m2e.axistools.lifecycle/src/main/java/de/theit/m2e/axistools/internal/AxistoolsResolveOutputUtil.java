package de.theit.m2e.axistools.internal;

import java.io.File;

import org.apache.maven.plugin.MojoExecution;

/**
 * Resolves output folder for generated sources.
 * For axistools it is outputDirectory, for axis2 it is outputDirectory/src
 * 
 * @author xendan
 */
public class AxistoolsResolveOutputUtil {

    private static final String AXIS2 = "axis2-wsdl2code-maven-plugin";

    /**
     * Return output folders for maven plugin.
     * 
     * @param dirs - folders from outputDirectory parameter
     * @param mojoExecution - maven plugin
     * @return - parameter folders if plugin is axis, folders + "/src" if plugin is axis2 
     */
    public static File[] getOutputFolders(File[] dirs, MojoExecution mojoExecution) {
        if (isAxis2(mojoExecution)) {
            File[] resultDirs = new File[dirs.length];
            for (int i = 0; i < dirs.length; i++) {
                resultDirs[i] = getAxis2OutputFolder(dirs[i]);
            }
            return resultDirs;
        }
        return dirs;
    }

    private static boolean isAxis2(MojoExecution mojoExecution) {
        return AXIS2.equals(mojoExecution.getArtifactId());
    }

    /**
     * Return output folder for maven plugin.
     * 
     * @param dir - folder from outputDirectory parameter
     * @param mojoExecution - maven plugin
     * @return - parameter folders if plugin is axis, folders + "/src" if plugin is axis2 
     */
    public static File getOutputFolder(File dir, MojoExecution mojoExecution) {
        if (isAxis2(mojoExecution)) {
            return getAxis2OutputFolder(dir);
        }
        return dir;
    }
    
    private static File getAxis2OutputFolder(File outputDirectory) {
        return new File(outputDirectory, "src");
    }
}
