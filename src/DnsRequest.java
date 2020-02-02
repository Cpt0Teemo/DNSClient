import java.util.Iterator;

class DnsRequest
{
  int timeout = 5;
  int max_retries = 3;
  int port = 53;
  ServerType serverType = ServerType.A;
  String serverIp;
  String domainName;
  
  Iterator<String> requestFlags;

  public DnsRequest(Iterator<String> options){
    this.requestFlags = options;
    try {
      parser();
    }catch (Exception e)
    {
      System.out.println(e.getMessage());
      return;
    }

    displayInformation();
  }

  public void parser() throws Exception
  {
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

      throw new Exception("ERROR: The value " + key + " is not accepted...");
    }    
  }

  private boolean extractIntFlag(String key, String flag) throws Exception {
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
          throw new Exception("ERROR: Incorrect " + flag + " value...");
        }
      } else {
        throw new Exception("ERROR: Incorrect syntax to " + flag + "...");
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

  private boolean getServerIp(String key) throws Exception {
    if(key.charAt(0) != '@') return false;

    key = key.substring(1);
    String regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    if(!key.matches(regex))
      throw new Exception("ERROR: Incorrect syntax for IP address");

    this.serverIp = key;

    //TODO Check if valid domain name
    this.domainName = requestFlags.next();

    return true;
  }

  private void displayInformation()
  {
    System.out.println("DnsClient sending request for " + this.domainName);
    System.out.println("Server: " + this.serverIp);
    System.out.println("Request type: " + this.serverType.toString());
  }
}