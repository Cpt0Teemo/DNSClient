import java.util.Iterator;

class DnsRequest
{
  int timeout = 5;
  int max_retries = 3;
  int port = 53;
  ServerType serverType = ServerType.IP;
  String serverIp;
  String domainName;
  
  Iterator<String> requestFlags;

  public DnsRequest(Iterator<String> options){
    this.requestFlags = options;
    try {
      parser();
    }catch (Exception e)
    {
      System.out.println("Failed");
      return;
    }

    System.out.println("" + this.timeout + this.max_retries + this.port + this.serverType + this.serverIp + this.domainName);
  }

  public void parser()git
  {
    String value;
    String key;
    
    while(requestFlags.hasNext())
    {
      key = requestFlags.next();

      if(extractIntFlag(key, "-t"))
        continue;
      if(extractIntFlag(key, "-r"))
        continue;
      if(extractIntFlag(key, "-p"))
        continue;
      if(getServerType(key))
        continue;
      if(getServerIp(key))
        return;

      System.out.println("ERROR: The value " + key + " is not accepted...");
      break;
    }    
  }

  private boolean extractIntFlag(String key, String flag)
  {
    if(key.compareTo(flag) == 0) {

      if(requestFlags.hasNext()) {

        try {
          if(flag.compareTo("-t") == 0)
            this.timeout = Integer.parseInt(requestFlags.next());
          if(flag.compareTo("-r") == 0)
            this.max_retries = Integer.parseInt(requestFlags.next());
          if(flag.compareTo("-p") == 0)
            this.port = Integer.parseInt(requestFlags.next());

          return true;

        } catch (Exception e){
          System.out.println("ERROR: Incorrect " + flag + " value...");
        }
      } else {
        System.out.println("ERROR: Incorrect syntax to " + flag + "...");
      }
    }
    return false;
  }

  private boolean getServerType(String key)
  {
    if(key.compareTo("-MX") == 0) {
      this.serverType = ServerType.MX;
      return true;
    }
    else if(key.compareTo("-NS") == 0) {
      this.serverType = ServerType.NS;
      return true;
    }
    return false;
  }

  private boolean getServerIp(String key)
  {
    if(key.charAt(0) != '@') return false;

    key = key.substring(1);
    String regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    if(!key.matches(regex))
      System.out.println("ERROR: Incorrect syntax for IP address");

    this.serverIp = key;

    //TODO Check if valid domain name
    this.domainName = requestFlags.next();

    return true;
  }
}
