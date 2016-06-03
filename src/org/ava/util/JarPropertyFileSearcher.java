package org.ava.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Constantin
 * @since 2016-03-20
 * @version 1
 */
public class JarPropertyFileSearcher extends SimpleFileVisitor<Path> {

	public class JarPropertyFilepathPair {
		
		private Path jarFilepath;
		private Path propertyFilepath;
		
		public JarPropertyFilepathPair(Path jarFilepath, Path propertyFilepath) {
			this.jarFilepath = jarFilepath;
			this.propertyFilepath = propertyFilepath;
		}
		
		public Path getJarFilepath() { return jarFilepath; }
		public Path getPropertyFilepath() { return propertyFilepath; }	
	} 
	// end inner class JarPropertyFilePair
	
	/** List containing found jar files. */
	private List<Path> jarFiles;
	
	/** List containing found property files. */
	private List<Path> propertyFiles;
	
	/** List containing pairs of jar and property files with the same name. */
	private List<JarPropertyFilepathPair> filePairs;
	
	private PathMatcher jarFileMatcher;
	private PathMatcher propertiesFileMatcher;
	
	public JarPropertyFileSearcher() {
		jarFiles = new ArrayList<Path>();
		propertyFiles = new ArrayList<Path>();
		filePairs = new ArrayList<JarPropertyFilepathPair>();
		
		jarFileMatcher = FileSystems.getDefault().getPathMatcher("glob:*.jar");
		propertiesFileMatcher = FileSystems.getDefault().getPathMatcher("glob:*.properties");
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		//System.out.println(file.getFileName());
		if(jarFileMatcher.matches(file.getFileName())) {
			jarFiles.add(file);
		}
		
		if(propertiesFileMatcher.matches(file.getFileName())) {
			propertyFiles.add(file);
		}
		
		return FileVisitResult.CONTINUE;
	}
	
	@Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        System.err.println(exc);
        return FileVisitResult.CONTINUE;
    }
	
	public List<JarPropertyFilepathPair> getJarPropertyFilepathPairs() {
		for(Path jarPath : jarFiles) {
			String tmp = jarPath.getFileName().toString();
			String jarPathAsString = tmp.substring(0, tmp.length() - ".jar".length());
			
			for(Path propertiesPath : propertyFiles) {
				tmp = propertiesPath.getFileName().toString();
				String propertiesPathAsString = tmp.substring(0, tmp.length() - ".properties".length());
				
				if(jarPathAsString.equals(propertiesPathAsString)) {
					filePairs.add(new JarPropertyFilepathPair(jarPath, propertiesPath));
				}
			}
		}
		return filePairs;
	}
	
}
