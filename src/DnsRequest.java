import java.util.Iterator;
import java.util.List;

class DnsRequest
{
  int timeout = 5;
  int max_retries = 3;
  int port = 53;
  ServerName server = ServerName.IP;

  int listSize;
  int numberOfPairs;
  
  Iterator<String> requestFlags;

  public DnsRequest(Iterator<String> options){
    this.requestFlags = options;
    parser();
  }

  public void parser()
  {
    
    String value;
    String key;
    
    while(requestFlags.hasNext())
    {
      key = (String) requestFlags.next();
      
      if(key.compareTo("-t") == 0) {
        requestFlags.next();
        
        if(requestFlags.hasNext()) {
          value = (String) requestFlags.next();
          
          try {
            
            int temp = Integer.parseInt(value);
            this.timeout = temp;
            requestFlags.next();
            
            System.out.println("T worked");
            
          } catch (Exception e){
            System.out.println("ERROR: Incorrect timeout value...");
          }
        } else {
          System.out.println("ERROR: Incorrect number of values...");
          return;
        }
      }
      
      if(key.compareTo("-r") == 0) {
        requestFlags.next();
        
        if(requestFlags.hasNext()) {
          value = (String) requestFlags.next();
          
          try {
            
            int temp = Integer.parseInt(value);
            this.max_retries = temp;
            requestFlags.next();
            
            System.out.println("R worked");
            
          } catch (Exception e){
            System.out.println("ERROR: Incorrect max_retries value...");
          }
        } else {
          System.out.println("ERROR: Incorrect number of values...");
          return;
        }
      }
      
      if(key.compareTo("-p") == 0) {
        requestFlags.next();
        
        if(requestFlags.hasNext()) {
          value = (String) requestFlags.next();
          
          try {
            
            int temp = Integer.parseInt(value);
            this.max_retries = temp;
            requestFlags.next();
            
          } catch (Exception e){
            System.out.println("ERROR: Incorrect port value...");
          }
        } else {
          System.out.println("ERROR: Incorrect number of values...");
          return;
        }
      }
    }    
  }
}
