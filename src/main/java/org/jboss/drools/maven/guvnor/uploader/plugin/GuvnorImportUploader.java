package org.jboss.drools.maven.guvnor.uploader.plugin;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal run
 */
public class GuvnorImportUploader extends AbstractMojo {
  /** @parameter expression="${run.debug}" */
  private String debug="false";
  /** @parameter expression="${run.importFilePath}" */
  private String importFilePath="target/import.xml";
  /** @parameter expression="${run.guvnorServer}" */
  private String guvnorServer="http://localhost:8080/jboss-brms";
  /** @parameter expression="${run.guvnorUsername}" */
  private String guvnorUsername="admin";
  /** @parameter expression="${run.guvnorPassword}" */
  private String guvnorPassword="admin";
  /** @parameter expression="${run.immediate}" */
  private String immediate="false";
  /** @parameter expression="${run.timeoutInSeconds}" */
  private String timeoutInSeconds="60";
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (null==immediate || !"true".equalsIgnoreCase(immediate)){
      if (isDebug()) System.out.println("wait mode\nHas Guvnor started?");
      Runnable runnable=new Runnable() {
        @Override
        public void run() {
          if (isDebug()) System.out.println("started thread to upload...");
          Wait.For(Integer.parseInt(timeoutInSeconds), 5, new ToHappen() {
            @Override
            public boolean hasHappened() {
              try{
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(guvnorServer + "/jboss-brms");
                get.addHeader("Authorization", "Basic " + buildCredentials());
                client.execute(get);
                if (isDebug()) System.out.println("yes! Guvnor is up!");
                upload();
                return true;
              }catch(Exception e){
                if (isDebug()) System.out.print(".");
                return false;
              }
            }
          });
        }
      };
      new Thread(runnable).start();
    }else{
      if (isDebug()) System.out.println("immediate mode");
      upload();
    }
  }
  
  public void upload() throws MojoExecutionException, MojoFailureException {
    try {
      if (isDebug()) System.out.println("uploading...");
      DefaultHttpClient httpClient = null;
      try {
        File file = new File(importFilePath);
        httpClient = new DefaultHttpClient();
        
        if (isDebug()){
          System.out.println("*** DEBUG PARAMETERS *** ");
          System.out.println("importFilePath = "+ importFilePath);
          System.out.println("guvnorServer = "+ guvnorServer);
          System.out.println("guvnorUsername = "+ guvnorUsername);
          System.out.println("guvnorPassword = "+ guvnorPassword);
          System.out.println("immediate = "+ immediate);
          System.out.println("timeoutInSeconds = "+ timeoutInSeconds);
          System.out.println("");
        }
        
        HttpPost post = new HttpPost(guvnorServer + "/org.drools.guvnor.Guvnor/backup");
        MultipartEntity data = new MultipartEntity(HttpMultipartMode.STRICT);
        if (!file.exists()) System.err.println("WARNING: file doesn't exist, upload will fail: ["+file.getAbsolutePath()+"]");
        data.addPart(file.getName(), new FileBody(file));
        post.setEntity(data);

        post.addHeader("Authorization", "Basic " + buildCredentials());

        HttpResponse response = httpClient.execute(post);
        HttpEntity responseData = response.getEntity();
        String responseContent=IOUtils.toString(responseData.getContent()); // need to read to consume content + close stream
        if (isDebug()) System.out.println("responseContent = "+ responseContent);
      } finally {
        try {
          httpClient.getConnectionManager().shutdown();
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    } catch (Exception e) {
      throw new MojoExecutionException("Unable to upload rules", e);
    }
  }
  private String buildCredentials() throws UnsupportedEncodingException{
    return new String(Base64.encodeBase64((guvnorUsername + ":" + guvnorPassword).getBytes()), "utf-8");
  }
  
  private boolean isDebug(){
    return "true".equalsIgnoreCase(debug);
  }
  
  public void setGuvnorServer(String guvnorServer) {
    this.guvnorServer = guvnorServer;
  }
  public void setGuvnorUsername(String guvnorUsername) {
    this.guvnorUsername = guvnorUsername;
  }
  public void setGuvnorPassword(String guvnorPassword) {
    this.guvnorPassword = guvnorPassword;
  }
  public void setImportFilePath(String importFilePath) {
    this.importFilePath = importFilePath;
  }
}
