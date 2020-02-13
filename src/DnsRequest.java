import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Iterator;

class DnsRequest
{

  private int id;
  private int timeout = 5;
  private int max_retries = 3;
  private int port = 53;
  private ServerType serverType = ServerType.A;
  private String dnsServerIp;
  private String domainName;

  public int getId() { return id; }

  public int getTimeout() {
    return timeout;
  }

  public int getMax_retries() {
    return max_retries;
  }

  public int getPort() {
    return port;
  }

  public ServerType getServerType() {
    return serverType;
  }

  public byte[] getServerIpBytes() {
    byte[] ip = new byte[4];
    String[] bytes = this.dnsServerIp.split("\\.");
    for(int i = 0; i<4; i++)
      ip[i] = (byte) Integer.parseInt(bytes[i]);
    return ip;
  }

  public String getDomainName() {
    return domainName;
  }
  
  private Iterator<String> requestFlags;

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

  private boolean extractIntFlag(String key, String flag) throws Exception
  {
    if(key.compareTo(flag) == 0) {

      if(requestFlags.hasNext()) {

        try {
          if(flag.toLowerCase().compareTo("-t") == 0)
            this.timeout = Integer.parseInt(requestFlags.next());
          if(flag.toLowerCase().compareTo("-r") == 0)
            this.max_retries = Integer.parseInt(requestFlags.next());
          if(flag.toLowerCase().compareTo("-p") == 0)
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
    if(key.toUpperCase().compareTo("-MX") == 0) {
      this.serverType = ServerType.MX;
      return true;
    }
    else if(key.toUpperCase().compareTo("-NS") == 0) {
      this.serverType = ServerType.NS;
      return true;
    }
    return false;
  }

  private boolean getServerIp(String key) throws Exception
  {
    if(key.charAt(0) != '@') return false;

    key = key.substring(1);
    String regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    if(!key.matches(regex))
      throw new Exception("ERROR: Incorrect syntax for IP address");

    this.dnsServerIp = key;

    //TODO Check if valid domain name
    this.domainName = requestFlags.next();

    return true;
  }

  private void displayInformation()
  {
    System.out.println("DnsClient sending request for " + this.domainName);
    System.out.println("Server: " + this.dnsServerIp);
    System.out.println("Request type: " + this.serverType.toString());
  }

  public byte[] createDnsRequest() throws Exception
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    addDnsHeader(dos);
    addDnsBody(dos);
    return baos.toByteArray();
  }

  private DataOutputStream addDnsHeader(DataOutputStream dos) throws Exception
  {
    this.id = (int) (Math.random() * 65535);
    //ID
    dos.writeByte((this.id >> 8) & 0xFF );
    dos.writeByte( this.id & 0xFF);
    //Query and recursive set to true
    dos.writeShort(0x0100);
    //QDCOUNT
    dos.writeShort(0x0001);
    //ANCOUNT
    dos.writeShort(0x0000);
    //NSCOUNT
    dos.writeShort(0x0000);
    //ARCOUNT
    dos.writeShort(0x0000);
    return dos;
  }

  private DataOutputStream addDnsBody(DataOutputStream dos) throws Exception
  {
    //Add domain name
    //TODO think of offset
    String[] labels = this.domainName.split("\\.");
    for (String label: labels)
    {
      dos.writeByte(label.length());
      for (char character: label.toCharArray())
      {
        dos.writeByte(character);
      }
    }
    //Mark the end of the labels
    dos.writeByte(0x00);

    //QTYPE
    switch (this.serverType)
    {
      case A:
        dos.writeShort(0x0001);
        break;
      case MX:
        dos.writeShort(0x000f);
        break;
      case NS:
        dos.writeShort(0x0002);
        break;
    }

    //QCLASS
    dos.writeShort(0x0001);

    return dos;
  }

}
