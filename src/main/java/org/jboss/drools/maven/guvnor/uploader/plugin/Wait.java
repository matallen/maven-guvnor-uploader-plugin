package org.jboss.drools.maven.guvnor.uploader.plugin;

public class Wait{
  public static void For(int timeoutInSeconds, int intervalInSeconds, ToHappen toHappen) {
    long start=System.currentTimeMillis();
    long end=start+(timeoutInSeconds*1000);
    boolean timeout=false;
    while(!toHappen.hasHappened() && !timeout){
      try{
        Thread.sleep((intervalInSeconds*1000));
      }catch(InterruptedException ignor){}
//      System.out.println("[Wait] - waiting... ["+((end-System.currentTimeMillis())/1000)+"s]");
      timeout=System.currentTimeMillis()>end;
      if (timeout) System.out.println("timed out waiting.");
    }
//    if (timeout)
//      System.err.println("[Wait] timed out");
//    else
//      System.out.println("[Wait] continuing...");
  }
  public static void For(int timeoutInSeconds, ToHappen toHappen) {
    For(timeoutInSeconds, 1, toHappen);
  }
}