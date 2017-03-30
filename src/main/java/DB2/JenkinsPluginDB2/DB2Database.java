package DB2.JenkinsPluginDB2;
import hudson.Launcher;
import hudson.Util;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link DB2Database} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #username})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 * 
 * @author Alex Hsiao
 */

public class DB2Database extends Builder implements SimpleBuildStep{
	
	private final String username;
    private final String password;
    private final String ip;
    private final String port;
    private final String dbname;
    private final String dbscript;
    
    private String ClassNotFound_log;
    private String SQLException_log;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public DB2Database(String username, String password, String ip, String port, String dbname, String dbscript) {
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.dbname = dbname;
        this.dbscript = dbscript;
    }
    
    /**
     * We'll use this from the {@code config.jelly}.
     */
    public String getUsername() {
    	return username;
    }
    
    public String getPassword(){
    	return password;
    }
    
    public String getIp(){
    	return ip;
    }
    
    public String getPort(){
    	return port;
    }
    
    public String getDbname(){
    	return dbname;
    }
    
    public String getDbscript(){
    	return dbscript;
    }
    
    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        // This is where you 'build' the project.
        // This also shows how you can consult the global configuration of the builder
    	
    	EnvVars env = build.getEnvironment(listener);
    	
    	String s_username = Util.replaceMacro(username, env);
    	String s_password = Util.replaceMacro(password, env);
    	String s_ip = Util.replaceMacro(ip, env);
    	String s_port = Util.replaceMacro(port, env);
    	String s_dbname = Util.replaceMacro(dbname, env);
    	String s_dbscript = Util.replaceMacro(dbscript, env);
    	
    	try { 
        	Class.forName("com.ibm.db2.jcc.DB2Driver");
            Connection conn = DriverManager.getConnection("jdbc:db2://" + s_ip + ":" + s_port + "/" + s_dbname, s_username, s_password);
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(s_dbscript);
            
            stmt.close();
          } 
          catch(ClassNotFoundException e) {
        	  ClassNotFound_log = "DriverClassNotFound :" + e.toString();
          }
          catch(SQLException x) { 
        	  SQLException_log = "Exception :" + x.toString(); 
          } 
    	
    	listener.getLogger().println();
    	listener.getLogger().println("Username: " + s_username);
		listener.getLogger().println("jdbc:db2://" + s_ip + ":" + s_port + "/" + s_dbname);
		listener.getLogger().println("Script: " + s_dbscript);
		
		if (ClassNotFound_log == null)
			listener.getLogger().println("Connection Success!");
		else
			listener.getLogger().println(ClassNotFound_log);
		
		if (SQLException_log == null)
			listener.getLogger().println("Script Execute Success!");
		else
			listener.getLogger().println(SQLException_log);
		
		listener.getLogger().println();
    }
    
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }
        
        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Connect-To-DB2";
        }
    }
}