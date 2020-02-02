import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class DnsClient{

  public static void main(String[] args) {
    int sizeOfOptions = args.length;

    // Command line input requires minimum 2 variables maximum 10 variables
    if(sizeOfOptions < 2 || sizeOfOptions > 10){
      System.out.println("ERROR: Incorrect number of inputs...");
      System.exit(0);
    }
    
    Iterator<String> arguments = Arrays.stream(args).iterator();
        
    DnsRequest request = new DnsRequest(arguments);
    
    
  }
}
