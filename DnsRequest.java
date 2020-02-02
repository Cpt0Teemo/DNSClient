class DnsRequest
{
  int timeout = 5;
  int max_retries = 3;
  int port = 53;
  ServerName server = ServerName.IP;

  String[] requestFlags;

  public DnsRequest(String[] options){
    this.requestFlags = options;
  }

  public void parser()
  {



  }
}
