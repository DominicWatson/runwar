package runwar.undertow;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jboss.logging.Logger;

import io.undertow.server.handlers.resource.FileResource;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;

public class ResourceManager extends FileResourceManager {

	private static Logger log = Logger.getLogger("RunwarLogger");
	private File[] cfmlDirsFiles;
	private String[] cfmlDirs;
	private File WEBINF = null;

	public ResourceManager(File base, long transferMinSize, String cfmlDirList) {
		super(base, transferMinSize);
		this.cfmlDirs = cfmlDirList.split(",");
		this.cfmlDirsFiles = new File[cfmlDirs.length];
		for(int x =0; x < cfmlDirs.length; x++){
			File dir = new File(cfmlDirs[x]);
			cfmlDirsFiles[x] = dir;
			if(!dir.exists()) {
				log.error("Does not exist, cannot serve content: " + dir.getAbsolutePath());					
			} else {
				log.info("Serving content from " + dir.getAbsolutePath());					
			}
		}
	}
	
	public ResourceManager(File base, long transferMinSize, String cfmlDirList, File file) {
		super(base, transferMinSize);
		this.cfmlDirs = cfmlDirList.split(",");
		this.cfmlDirsFiles = new File[cfmlDirs.length];
		this.WEBINF = file;
		if(!this.WEBINF.exists()) {
			throw new RuntimeException("The specified WEB-INF does not exist: " + this.WEBINF.getAbsolutePath());
		}
		for(int x =0; x < cfmlDirs.length; x++){
			File dir = new File(cfmlDirs[x]);
			cfmlDirsFiles[x] = dir;
			if(!dir.exists()) {
				log.error("Does not exist, cannot serve content: " + dir.getAbsolutePath());					
			} else {
				log.info("Serving content from " + dir.getAbsolutePath());					
			}
		}
	}
	
	public Resource getResource(String contextPath) {
	    //log.debug("requested:"+contextPath);
		try {
			 if(WEBINF != null && contextPath.equals("/WEB-INF")) {
				return new FileResource(WEBINF, this, contextPath);
			 }
			 else if(!contextPath.startsWith("/WEB-INF")) {
				for (int x = 0; x < cfmlDirs.length; x++) {
					File reqFile = new File(getBase(), contextPath);
					//System.out.println("requested ==" + reqFile.getAbsolutePath() + " exists:" + reqFile.exists());
					if (reqFile.exists()) {
						return new FileResource(reqFile, this, contextPath);
					} else {
						String absPath = cfmlDirsFiles[x].getCanonicalPath();
						reqFile = new File(cfmlDirsFiles[x] + contextPath.replace(absPath, ""));
						if (reqFile.exists()) {
							return new FileResource(reqFile, this, contextPath);
						}
					}
				}
			 }
			return super.getResource(contextPath);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return null;
	}

}